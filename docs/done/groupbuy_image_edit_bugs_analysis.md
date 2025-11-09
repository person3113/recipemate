# 공구 이미지 수정 기능 버그 분석

## 🐛 버그 #2: 프론트엔드에서 잘못된 삭제 데이터 전송

### 증상
기존 이미지 1개 삭제 시, 서버에 다음과 같이 **이미지 URL이 조각나서 전송됨**:

```
deletedImages: [
  "https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800",
  "h_600",
  "c_limit",
  "q_auto",
  "f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png"
]
```

### 로그 분석

```
2025-11-09T11:42:52.343  INFO  Image deleted from Cloudinary: w_800 (result: not found)
2025-11-09T11:42:52.344  ERROR Failed to extract public_id from URL: h_600
java.lang.IllegalArgumentException: Invalid Cloudinary URL: h_600

2025-11-09T11:42:52.368  ERROR Failed to extract public_id from URL: c_limit
java.lang.IllegalArgumentException: Invalid Cloudinary URL: c_limit

2025-11-09T11:42:52.398  INFO  Deleted 5 images from group buy 545  // ❌ 원래 1개인데 5개로 인식!
```

### 근본 원인

**파일:** `form.html` (Line 376-411)

#### 문제: URL이 쉼표로 split됨

**Cloudinary URL 구조:**
```
https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png
                                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                                      쉼표가 포함된 변환 파라미터!
```

**현재 프론트엔드 코드:**
```javascript
// ❌ 문제 코드
function markImageForDeletion(button, imageUrl) {
    if (!deletedImagesArray.includes(imageUrl)) {
        deletedImagesArray.push(imageUrl);
    }
    
    // Hidden input에 배열을 다중 값으로 추가
    deletedImagesArray.forEach(url => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'deletedImages';
        input.value = url;  // ✅ 여기서는 문제 없음
        form.appendChild(input);
    });
    
    // ...
}
```

**하지만 서버에서 받을 때:**
```java
@ModelAttribute UpdateGroupBuyRequest request

// UpdateGroupBuyRequest.java
private List<String> deletedImages;
```

**Spring의 요청 파라미터 바인딩:**
- `deletedImages=url1,url2,url3` 형태로 전송되면 **쉼표를 구분자로 인식**
- URL 내부의 쉼표(`w_800,h_600`)도 구분자로 처리됨!

### 해결 방법

#### 방법 1: Controller에서 @RequestParam 사용 (권장)

**현재:**
```java
@PostMapping("/{id}/edit")
public String updateGroupBuy(
    @PathVariable Long id,
    @ModelAttribute UpdateGroupBuyRequest request  // ❌ 문제!
)
```

**수정:**
```java
@PostMapping("/{id}/edit")
public String updateGroupBuy(
    @PathVariable Long id,
    @ModelAttribute UpdateGroupBuyRequest request,
    @RequestParam(value = "deletedImages", required = false) List<String> deletedImages  // ✅ 명시적 바인딩
) {
    // 명시적으로 바인딩된 deletedImages 사용
    request.setDeletedImages(deletedImages);
}
```

#### 방법 2: 프론트엔드에서 JSON으로 전송

```javascript
// form submit 시 JSON으로 변환
const formData = new FormData(form);
formData.set('deletedImagesJson', JSON.stringify(deletedImagesArray));
```

#### 방법 3: URL 인코딩 (임시 방편)

```javascript
function markImageForDeletion(button, imageUrl) {
    const encodedUrl = encodeURIComponent(imageUrl);  // ✅ 쉼표 escape
    deletedImagesArray.push(encodedUrl);
}
```

---

## 🐛 버그 #3: 토탈 이미지 개수 검증 누락

### 증상
기존 이미지 1개 + 새 이미지 3개 = 총 4개가 저장됨 (최대 3개 제한 위반)

### 로그 분석
```
2025-11-09T11:36:00.984  INFO  Successfully uploaded 3 out of 3 images (parallel)
2025-11-09T11:36:00.984  INFO  Uploaded 3 new images for group buy 545

DB 결과:
130 | display_order=0 | https://...joem7qjuzva4r30gixrf.png | 545
131 | display_order=1 | https://...crbuqc6ezjhtyzo1aypy.png | 545
132 | display_order=2 | https://...o5uyeui7jdgnnpjk10zl.png | 545
133 | display_order=3 | https://...kirototbfwyermpuhedj.png | 545
```

### 근본 원인

**파일:** `GroupBuyService.java` (updateGroupBuy 메서드)

#### 문제: 수정 시 총 이미지 개수 검증 없음

```java
@Transactional
public GroupBuyResponse updateGroupBuy(Long userId, Long groupBuyId, UpdateGroupBuyRequest request) {
    // ... 권한 검증 ...
    
    // ❌ 총 이미지 개수 검증이 없음!
    
    // 4-2. 삭제할 이미지 처리
    if (deletedImages != null && !deletedImages.isEmpty()) {
        // ...
    }
    
    // 4-3. 새 이미지 업로드
    if (request.getImages() != null && !request.getImages().isEmpty()) {
        newImageUrls = imageUploadUtil.uploadImages(validFiles);  // ❌ 무조건 업로드!
    }
}
```

**프론트엔드 검증:**
```javascript
// 548-556줄: 새 이미지만 검증
if (files.length > MAX_IMAGE_COUNT) {
    alert(`이미지는 최대 ${MAX_IMAGE_COUNT}장까지 업로드할 수 있습니다.`);
    return;
}
// ❌ 기존 이미지 + 새 이미지 합산 검증 없음!
```

### 해결 방법

#### 백엔드 검증 추가

```java
@Transactional
public GroupBuyResponse updateGroupBuy(Long userId, Long groupBuyId, UpdateGroupBuyRequest request) {
    // ... 권한 검증 ...
    
    // 4-1. 기존 이미지 조회
    List<GroupBuyImage> currentImages = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy);
    
    // 4-2. 삭제할 이미지 처리
    int remainingImageCount = currentImages.size();
    if (deletedImages != null && !deletedImages.isEmpty()) {
        // ... 삭제 로직 ...
        remainingImageCount -= deletedImages.size();
    }
    
    // 4-3. 새 이미지 업로드 전 검증 ✅
    List<String> newImageUrls = new ArrayList<>();
    if (request.getImages() != null && !request.getImages().isEmpty()) {
        List<MultipartFile> validFiles = request.getImages().stream()
            .filter(file -> !file.isEmpty())
            .toList();
        
        // ✅ 총 이미지 개수 검증
        if (remainingImageCount + validFiles.size() > 3) {
            throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
        }
        
        if (!validFiles.isEmpty()) {
            newImageUrls = imageUploadUtil.uploadImages(validFiles);
        }
    }
}
```

#### 프론트엔드 검증 추가

```javascript
// form.html 548줄 수정
imageFilesInput.addEventListener('change', async function(e) {
    const files = Array.from(e.target.files);
    
    // ✅ 기존 이미지 개수 계산
    const existingImages = document.querySelectorAll('.existing-image-card:not([style*="opacity: 0.5"])');
    const existingCount = existingImages.length;
    
    // ✅ 총 개수 검증
    if (existingCount + files.length > MAX_IMAGE_COUNT) {
        alert(`총 이미지는 최대 ${MAX_IMAGE_COUNT}장까지 가능합니다. 현재 ${existingCount}개 존재, ${files.length}개 추가 시도`);
        e.target.value = '';
        return;
    }
    
    // ... 나머지 검증 로직 ...
});
```

---

## 🧪 기대 동작

### 시나리오 1: 이미지 삭제 + 추가 (정상 케이스)
- **초기 상태:** 이미지 2개
- **작업:** 1개 삭제 + 2개 추가
- **기대 결과:** 총 3개 (display_order: 0, 1, 2)

### 시나리오 2: 최대 개수 초과 (에러 케이스)
- **초기 상태:** 이미지 1개
- **작업:** 3개 추가 시도
- **기대 결과:** 프론트엔드에서 에러 메시지 + 업로드 차단

### 시나리오 3: 전체 삭제 + 신규 추가
- **초기 상태:** 이미지 3개
- **작업:** 3개 모두 삭제 + 2개 추가
- **기대 결과:** 총 2개 (display_order: 0, 1)

### 시나리오 4: 삭제만 수행
- **초기 상태:** 이미지 3개
- **작업:** 1개만 삭제
- **기대 결과:** 총 2개 (display_order: 0, 1로 재정렬)

---

## 📝 관련 파일

- `GroupBuyService.java:291-334` (updateGroupBuy 메서드)
- `GroupBuyImage.java:48` (updateDisplayOrder 메서드 추가됨)
- `GroupBuyController.java` (수정 필요)
- `form.html:376-625` (프론트엔드 이미지 처리 로직)
- `ImageUploadUtil.java:174-239` (extractPublicIdFromUrl 메서드)

---
원본 로그
```text
- [] 공구 이미지 수정할 때. 기존 이미지(1개) + 새 이미지(3개) 추가 포함해서 토탈 3개 제한 에러 처리가 정상적인지 확인해보기-> 결과 로그. 
2025-11-09T11:35:57.684+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-2] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-10-03 200653.png
2025-11-09T11:35:57.684+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-1] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-10-02 103300.png
2025-11-09T11:35:57.686+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-10-03 201617.png
2025-11-09T11:36:00.471+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-2] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 2787ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762655760/recipemate/group-purchases/yvornme1xmdh6r1e5mnt.png
2025-11-09T11:36:00.844+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-1] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 3160ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762655760/recipemate/group-purchases/vpftvipgvnlg299mowua.png
2025-11-09T11:36:00.984+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 3300ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762655760/recipemate/group-purchases/x1pntzeelornxbswwy4l.png
2025-11-09T11:36:00.984+09:00  INFO 12876 --- [RecipeMate] [nio-8080-exec-9] c.r.global.util.ImageUploadUtil          : Successfully uploaded 3 out of 3 images (parallel)
2025-11-09T11:36:00.984+09:00  INFO 12876 --- [RecipeMate] [nio-8080-exec-9] c.r.d.groupbuy.service.GroupBuyService   : Uploaded 3 new images for group buy 545

Hibernate: 
    insert 
    into
        group_buy_images
        (created_at, deleted_at, display_order, group_buy_id, image_url, updated_at, id) 
    values
        (?, ?, ?, ?, ?, ?, default)
2025-11-09T11:36:01.020+09:00  WARN 12876 --- [RecipeMate] [nio-8080-exec-9] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 23505, SQLState: 23505
2025-11-09T11:36:01.021+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-9] o.h.engine.jdbc.spi.SqlExceptionHelper   : Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:
insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default) [23505-232]
Unexpected error: could not execute statement [Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:
insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default) [23505-232]] [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]; SQL [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]; constraint [PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5]
org.springframework.dao.DataIntegrityViolationException: could not execute statement [Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:
insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default) [23505-232]] [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]; SQL [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]; constraint [PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5]
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:294)
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:256)
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.translateExceptionIfPossible(HibernateJpaDialect.java:241)
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.translateExceptionIfPossible(AbstractEntityManagerFactoryBean.java:560)
	at org.springframework.dao.support.ChainedPersistenceExceptionTranslator.translateExceptionIfPossible(ChainedPersistenceExceptionTranslator.java:61)
	at org.springframework.dao.support.DataAccessUtils.translateIfNecessary(DataAccessUtils.java:343)
	at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:160)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)

Caused by: org.hibernate.exception.ConstraintViolationException: could not execute statement [Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:
insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default) [23505-232]] [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]
Caused by: org.hibernate.exception.ConstraintViolationException: could not execute statement [Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:

	at org.hibernate.dialect.H2Dialect.lambda$buildSQLExceptionConversionDelegate$3(H2Dialect.java:759)
	at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:58)
	at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:108)
	at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:197)
	at org.hibernate.id.insert.GetGeneratedKeysDelegate.performMutation(GetGeneratedKeysDelegate.java:116)
	at org.hibernate.engine.jdbc.mutation.internal.MutationExecutorSingleNonBatched.performNonBatchedOperations(MutationExecutorSingleNonBatched.java:47)
	at org.hibernate.engine.jdbc.mutation.internal.AbstractMutationExecutor.execute(AbstractMutationExecutor.java:55)
	at org.hibernate.persister.entity.mutation.InsertCoordinatorStandard.doStaticInserts(InsertCoordinatorStandard.java:194)

Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:
insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default) [23505-232]
	at org.h2.message.DbException.getJdbcSQLException(DbException.java:520)
Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:

	at org.h2.message.DbException.getJdbcSQLException(DbException.java:489)
	at org.h2.message.DbException.get(DbException.java:223)
	at org.h2.message.DbException.get(DbException.java:199)
	at org.h2.index.Index.getDuplicateKeyException(Index.java:523)
	at org.h2.mvstore.db.MVSecondaryIndex.checkUnique(MVSecondaryIndex.java:223)
	at org.h2.mvstore.db.MVSecondaryIndex.add(MVSecondaryIndex.java:184)
	at org.h2.mvstore.db.MVTable.addRow(MVTable.java:517)
	at org.h2.command.dml.Insert.insertRows(Insert.java:174)
2025-11-09T11:36:01.036+09:00  WARN 12876 --- [RecipeMate] [nio-8080-exec-9] .m.m.a.ExceptionHandlerExceptionResolver : Resolved [org.springframework.dao.DataIntegrityViolationException: could not execute statement [Unique index or primary key violation: "PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5 ON PUBLIC.GROUP_BUY_IMAGES(GROUP_BUY_ID NULLS FIRST, DISPLAY_ORDER NULLS FIRST) VALUES ( /* key:66 */ CAST(545 AS BIGINT), 1)"; SQL statement:<EOL>insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default) [23505-232]] [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]; SQL [insert into group_buy_images (created_at,deleted_at,display_order,group_buy_id,image_url,updated_at,id) values (?,?,?,?,?,?,default)]; constraint [PUBLIC.UK_GROUP_BUY_IMAGE_ORDER_INDEX_5]]





- [] 아마 정확하진 않은데. 기존 공구 수정할 때. 기존 이미지 2개 있었나? 그 중 하나 삭제버튼 클릭 + 새 이미지 2개 추가. 하고 수정하기 버튼 눌렀는데. 뭔가 에러 나고 다시 보니까 이미지가 4개로 들어가있음.
db 보면 
130	2025-11-09 11:39:16.077843	null	2025-11-09 11:39:16.077843	0	https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762655955/recipemate/group-purchases/joem7qjuzva4r30gixrf.png	545
131	2025-11-09 11:39:16.098731	null	2025-11-09 11:39:16.098731	1	https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png	545
132	2025-11-09 11:42:54.596376	null	2025-11-09 11:42:54.596376	2	https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762656174/recipemate/group-purchases/o5uyeui7jdgnnpjk10zl.png	545
133	2025-11-09 11:42:54.600637	null	2025-11-09 11:42:54.600637	3	https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762656173/recipemate/group-purchases/kirototbfwyermpuhedj.png	545

2025-11-09T11:42:52.343+09:00  INFO 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Image deleted from Cloudinary: w_800 (result: not found)
2025-11-09T11:42:52.344+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: h_600

java.lang.IllegalArgumentException: Invalid Cloudinary URL: h_600
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.2.12.jar:6.2.12]

2025-11-09T11:42:52.363+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: h_600

java.lang.IllegalArgumentException: Invalid Cloudinary URL: h_600
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:239) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.2.12.jar:6.2.12]

aused by: java.lang.IllegalArgumentException: Invalid Cloudinary URL: h_600
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	... 121 common frames omitted

2025-11-09T11:42:52.368+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: c_limit

java.lang.IllegalArgumentException: Invalid Cloudinary URL: c_limit
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-ao

2025-11-09T11:42:52.373+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: c_limit

java.lang.IllegalArgumentException: Invalid Cloudinary URL: c_limit
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:239) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-

aused by: java.lang.IllegalArgumentException: Invalid Cloudinary URL: c_limit
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	... 121 common frames omitted

2025-11-09T11:42:52.378+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: q_auto

java.lang.IllegalArgumentException: Invalid Cloudinary URL: q_auto
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.2

aused by: java.lang.IllegalArgumentException: Invalid Cloudinary URL: q_auto
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	... 121 common frames omitted

2025-11-09T11:42:52.387+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to extract public_id from URL: f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png

java.lang.IllegalArgumentException: Invalid Cloudinary URL: f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring

2025-11-09T11:42:52.392+09:00 ERROR 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Failed to delete image from Cloudinary: f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png

java.lang.IllegalArgumentException: Invalid Cloudinary URL: f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:239) ~[main/:na]
	at com.recipemate.global.util.ImageUploadUtil.deleteImages(ImageUploadUtil.java:177) ~[main/:na]
	at com.recipemate.domain.groupbuy.service.GroupBuyService.updateGroupBuy(GroupBuyService.java:294) ~[main/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:360) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.2.12.jar:6.2.12]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spr
Caused by: java.lang.IllegalArgumentException: Invalid Cloudinary URL: f_auto/v1762655955/recipemate/group-purchases/crbuqc6ezjhtyzo1aypy.png
	at com.recipemate.global.util.ImageUploadUtil.extractPublicIdFromUrl(ImageUploadUtil.java:199) ~[main/:na]
	... 121 common frames omitted

2025-11-09T11:42:52.398+09:00  INFO 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.d.groupbuy.service.GroupBuyService   : Deleted 5 images from group buy 545
Hibernate: 
    select
        gbi1_0.id,
        gbi1_0.created_at,
        gbi1_0.deleted_at,
        gbi1_0.display_order,
        gbi1_0.group_buy_id,
        gbi1_0.image_url,
        gbi1_0.updated_at 
    from
        group_buy_images gbi1_0 
    where
        gbi1_0.group_buy_id=? 
    order by
        gbi1_0.display_order
2025-11-09T11:42:52.402+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-10-02 103244.png
2025-11-09T11:42:52.402+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-2] c.r.global.util.ImageUploadUtil          : Uploading image to Cloudinary: 스크린샷 2025-10-02 103300.png
2025-11-09T11:42:54.032+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-2] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 1630ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762656173/recipemate/group-purchases/kirototbfwyermpuhedj.png
2025-11-09T11:42:54.591+09:00  INFO 12876 --- [RecipeMate] [pool-2-thread-3] c.r.global.util.ImageUploadUtil          : Image uploaded successfully in 2189ms: https://res.cloudinary.com/dt9xgsr2z/image/upload/w_800,h_600,c_limit,q_auto,f_auto/v1762656174/recipemate/group-purchases/o5uyeui7jdgnnpjk10zl.png
2025-11-09T11:42:54.592+09:00  INFO 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Successfully uploaded 2 out of 2 images (parallel)
2025-11-09T11:42:54.593+09:00  INFO 12876 --- [RecipeMate] [nio-8080-exec-2] c.r.d.groupbuy.service.GroupBuyService   : Uploaded 2 new images for group buy 545

```