# `post` 이미지 수정 버그 분석 및 해결 방안

## 1. 문제 상황

커뮤니티 게시글(`post`) 수정 시, 기존에 업로드된 이미지를 삭제하려고 하면 오류가 발생하며 변경 사항이 적용되지 않습니다.

- **전제 조건**: 이미지가 2장 이상 업로드된 게시글이 존재합니다.
- **재현 과정**:
    1. 해당 게시글의 '수정' 페이지로 이동합니다.
    2. 기존 이미지 중 하나를 삭제합니다.
    3. '수정 완료' 버튼을 누릅니다.
- **기대 결과**: 삭제된 이미지는 사라지고, 게시글 내용이 정상적으로 수정됩니다.
- **실제 결과**: 변경 사항이 전혀 적용되지 않습니다. 서버 로그에는 `IllegalArgumentException: Invalid Cloudinary URL`와 `StringIndexOutOfBoundsException` 오류가 기록됩니다.

## 2. 로그 분석 및 원인

제공된 로그(`tmp_log10.md`)를 분석한 결과, 문제의 원인은 **클라이언트(프론트엔드)에서 서버로 잘못된 이미지 URL 데이터를 전송**하기 때문입니다.

### 상세 분석

1.  **오류 로그 확인**: 로그에는 다음과 같은 오류가 반복적으로 나타납니다.
    ```
    ERROR c.r.global.util.ImageUploadUtil: Failed to extract public_id from URL: h_600
    java.lang.StringIndexOutOfBoundsException: Range [7, 5) out of bounds for length 5
    ...
    ERROR c.r.global.util.ImageUploadUtil: Failed to extract public_id from URL: q_auto
    java.lang.StringIndexOutOfBoundsException: Range [7, 6) out of bounds for length 6
    ```
    이 로그는 이미지의 `public_id`를 추출하는 `extractPublicIdFromUrl` 메서드가 `h_600`이나 `q_auto`와 같은 완전하지 않은 문자열을 입력으로 받고 있음을 명확히 보여줍니다.

2.  **잘못된 데이터 전송**: Cloudinary 이미지 URL은 `.../upload/w_800,h_600,c_limit,q_auto,f_auto/v.../recipemate/.../image.png`와 같은 구조를 가집니다. 여기서 `w_800,h_600...` 부분은 이미지 변환 옵션입니다.
    오류 로그는 클라이언트 측 JavaScript가 이미지 삭제를 처리할 때, **전체 이미지 URL**을 보내는 대신, URL에 포함된 **변환 옵션 문자열(`w_800,h_600,c_limit,q_auto,f_auto`)을 파싱하여 개별 조각들을 삭제할 이미지 목록에 포함시켜** 서버로 보내고 있음을 시사합니다.

3.  **서버 측 실패 및 트랜잭션 롤백**:
    - 서버의 `ImageUploadUtil.deleteImages` 메서드는 `["https://.../actual_image.png", "h_600", "q_auto", ...]` 와 같이 올바른 URL과 쓰레기 값들이 섞인 리스트를 받게 됩니다.
    - `extractPublicIdFromUrl` 메서드는 `h_600`과 같은 유효하지 않은 URL 형식의 문자열을 처리하다가 `StringIndexOutOfBoundsException`을 발생시킵니다.
    - 이 예외로 인해 전체 `updatePost` 메서드의 트랜잭션이 롤백(Rollback)됩니다.
    - 결과적으로 이미지 삭제, 텍스트 수정 등 모든 변경 사항이 데이터베이스에 반영되지 않고 원상 복구됩니다.

## 3. 해결 방안

근본적인 원인이 클라이언트에 있으므로 **프론트엔드 JavaScript 코드를 수정**해야 합니다.

- **수정 대상**: 게시글 수정 페이지에서 '이미지 삭제' 버튼 클릭 시 동작하는 JavaScript 로직.
- **수정 내용**: 해당 로직이 삭제할 이미지의 **전체 URL (`src` 속성 값)**을 정확히 가져와 서버에 전송하도록 수정해야 합니다. 현재처럼 URL의 특정 부분을 분리하거나 파싱하여 서버에 보내는 로직이 있다면 제거해야 합니다.

## 4. 기존 `groupbuy` 버그와의 비교

과거에 분석된 `groupbuy_image_update_bug_analysis.md`의 버그와 증상은 유사하지만 근본 원인이 다릅니다.

- **과거 `groupbuy` 버그**: 서버의 `extractPublicIdFromUrl` 메서드가 변환 옵션이 포함된 *유효한 URL*을 올바르게 파싱하지 못하는 **서버 측 로직 결함**이었습니다.
- **현재 `post` 버그**: 클라이언트가 서버에 *유효하지 않은 문자열*을 삭제 요청으로 보내는 **클라이언트 측 데이터 오류**입니다.

따라서 과거의 서버 측 해결책만으로는 이 문제를 해결할 수 없으며, 반드시 클라이언트 측 코드 수정이 필요합니다.

## 5. 기존 `groupbuy` 버그의 실제 해결 방식 분석

유사한 버그가 있었던 `groupbuy` 도메인의 코드를 분석한 결과, 다음과 같은 방식으로 문제가 해결되었습니다.

- **핵심 아이디어**: 프론트엔드에서 삭제할 이미지 URL 목록을 **JSON 배열 형태의 문자열**로 만들어 서버에 하나의 파라미터로 전송합니다. 서버는 이 JSON 문자열을 파싱하여 `List<String>`으로 변환합니다.

- **`GroupBuyController.java`의 수정 내용**:
    ```java
    @PostMapping("/{purchaseId}")
    public String updateGroupBuy(
        // ...
        @Valid @ModelAttribute("updateGroupBuyRequest") UpdateGroupBuyRequest request,
        // 1. 삭제할 이미지 목록을 단일 String으로 받음
        @RequestParam(required = false) String deletedImages,
        // ...
    ) {
        // ...

        // 2. deletedImages JSON 파싱
        List<String> deletedImagesList = new ArrayList<>();
        if (deletedImages != null && !deletedImages.isBlank()) {
            try {
                // 3. ObjectMapper를 이용해 JSON 문자열을 List<String>으로 변환
                deletedImagesList = objectMapper.readValue(
                    deletedImages,
                    new TypeReference<List<String>>() {}
                );
            } catch (Exception e) {
                // 파싱 실패 처리
            }
        }
        
        // 4. 서비스 레이어로 파싱된 리스트 전달
        groupBuyService.updateGroupBuy(user.getId(), purchaseId, request, deletedImagesList);
        
        // ...
    }
    ```

이 방식은 URL에 포함된 쉼표(`,`)가 Spring의 파라미터 바인딩 과정에서 문제를 일으키는 것을 원천적으로 차단하는 효과적인 해결책입니다.

## 6. `post` 버그에 대한 구체적인 해결 방안

현재 `PostController`와 관련 서비스 코드는 `deletedImages`를 처리하는 로직이 아예 누락되어 있습니다. `groupbuy`의 해결 방식을 참고하여 다음과 같이 수정해야 합니다.

### 6.1. 프론트엔드 (`form.html`)

게시글 수정 폼의 JavaScript에서 삭제하기로 선택된 이미지들의 URL을 배열에 담고, 폼 제출 직전에 `JSON.stringify()`를 사용하여 문자열로 변환한 후, `deletedImages`라는 이름의 hidden input에 값을 설정해야 합니다.

```javascript
// 예시: form.html의 폼 제출 관련 스크립트
const form = document.getElementById('post-form');
const deletedImagesArray = []; // 사용자가 삭제 버튼을 누를 때마다 이 배열에 URL 추가

// 삭제 버튼 클릭 이벤트 핸들러 (예시)
function handleDeleteImage(button, imageUrl) {
    // UI 처리: 이미지를 흐리게 만들거나 숨김
    const imageCard = button.closest('.image-card');
    imageCard.style.display = 'none';

    // 삭제할 이미지 URL 배열에 추가
    if (!deletedImagesArray.includes(imageUrl)) {
        deletedImagesArray.push(imageUrl);
    }
}

form.addEventListener('submit', function(e) {
    // 기존 hidden input 제거 (중복 방지)
    document.querySelectorAll('input[name="deletedImages"]').forEach(input => input.remove());

    // 배열을 JSON 문자열로 변환하여 hidden input으로 추가
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'deletedImages';
    input.value = JSON.stringify(deletedImagesArray);
    form.appendChild(input);
});
```

### 6.2. 백엔드

#### 1. `UpdatePostRequest.java` DTO 수정
`deletedImages`를 컨트롤러에서 서비스 레이어로 전달하기 위해 필드를 추가합니다. (서비스 메서드 시그니처를 직접 수정해도 무방합니다.)

```java
// UpdatePostRequest.java
@Getter
@Setter
public class UpdatePostRequest {
    // ... 기존 필드
    private List<String> deletedImages; // 서비스 레이어 전달용
}
```

#### 2. `PostController.java` 수정
`groupbuy`와 동일하게 `deletedImages` 파라미터를 받고 JSON을 파싱하여 서비스로 전달합니다.

```java
// PostController.java
// ObjectMapper 주입 필요
private final ObjectMapper objectMapper;

@PostMapping("/{postId}")
public String updatePost(
        // ...
        @Valid @ModelAttribute UpdatePostRequest request,
        @RequestParam(required = false) String deletedImages, // 1. 파라미터 추가
        // ...
) {
    // ... (유효성 검증)

    // 2. JSON 파싱
    List<String> deletedImagesList = new ArrayList<>();
    if (deletedImages != null && !deletedImages.isBlank()) {
        try {
            deletedImagesList = objectMapper.readValue(deletedImages, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("게시글 수정 중 deletedImages JSON 파싱 실패: {}", deletedImages);
            // 오류를 FlashAttribute로 전달하며 리다이렉트
        }
    }
    // DTO에 설정하거나 서비스 메서드에 직접 전달
    request.setDeletedImages(deletedImagesList);

    // ...
    postService.updatePost(user.getId(), postId, request); // 3. 수정된 request 전달
    // ...
}
```
**참고**: `PostController`에 `ObjectMapper`를 생성자 주입 받아야 합니다.

#### 3. `PostService.java` 수정
`updatePost` 메서드가 `deletedImages` 목록을 받아 이미지를 삭제하는 로직을 수행하도록 수정합니다.

```java
// PostService.java
@Transactional
public void updatePost(Long userId, Long postId, UpdatePostRequest request) {
    Post post = // ... 게시글 조회 및 권한 검증

    // ... 내용 업데이트 로직

    // 1. 이미지 삭제 로직 추가
    List<String> deletedImages = request.getDeletedImages();
    if (deletedImages != null && !deletedImages.isEmpty()) {
        // ImageUploadUtil을 사용하여 Cloudinary 및 DB에서 이미지 삭제
        imageUploadUtil.deleteImages(deletedImages, ImageType.POST); // ImageType enum 등 활용
        
        // PostImage 엔티티 삭제
        postImageRepository.deleteByPostAndImageUrlIn(post, deletedImages);
    }

    // 2. 새 이미지 추가 로직
    // ...
}
```
위와 같이 프론트엔드와 백엔드 양쪽을 모두 수정하면 `post`의 이미지 수정 버그를 근본적으로 해결할 수 있습니다.

