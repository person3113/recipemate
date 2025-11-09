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
