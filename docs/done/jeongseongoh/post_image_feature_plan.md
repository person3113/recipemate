# 커뮤니티 게시글 이미지 업로드 기능 추가 계획

`groupbuy` 도메인의 이미지 처리 방식을 참고하여 `post` 도메인에 이미지 업로드 기능을 추가합니다. 최대 3장의 이미지를 업로드할 수 있으며, 각 파일은 5MB 미만의 JPG/PNG 형식이어야 합니다.

## 1. 백엔드 구현 계획

### 1.1. Entity 및 Repository

-   **`PostImage.java`**: 이미 `GroupBuyImage`와 유사하게 구현되어 있어 별도 수정이 필요 없습니다. (`post`, `imageUrl`, `displayOrder` 필드 확인 완료)
-   **`Post.java`**: `Post` 엔티티에 이미지 목록을 관리하기 위해 `@OneToMany` 관계를 추가합니다.
    ```java
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();
    ```
-   **`PostImageRepository.java`**: `GroupBuyImageRepository`를 참고하여 `PostImage`를 다루는 JpaRepository를 생성합니다.
    -   `findByPostOrderByDisplayOrderAsc(Post post)`
    -   `deleteByPost(Post post)`
    -   `findByPostIdInOrderByPostIdAndDisplayOrder(List<Long> postIds)` (N+1 문제 해결용)

### 1.2. DTO 수정

-   **`PostRequestDto.java` (가칭, 생성/수정용 DTO):**
    -   `List<MultipartFile> imageFiles`: 새로운 이미지 파일을 받기 위한 필드를 추가합니다.
    -   `List<String> deletedImages`: 수정 시 삭제할 기존 이미지 URL 목록을 받기 위한 필드를 추가합니다.
-   **`PostResponseDto.java` (가칭, 조회용 DTO):**
    -   `List<String> imageUrls`: 게시글에 첨부된 이미지 URL 목록을 반환하기 위한 필드를 추가합니다.

### 1.3. `PostService` 수정

`ImageUploadUtil`과 `PostImageRepository`를 주입받아 사용합니다.

-   **`createPost(..., PostRequestDto request)`:**
    1.  `imageUploadUtil.uploadImages(request.getImageFiles())`를 호출하여 이미지를 Cloudinary에 업로드합니다.
    2.  반환된 URL 목록을 사용하여 `PostImage` 엔티티를 생성하고 `displayOrder`를 설정합니다.
    3.  생성된 `PostImage` 목록을 `Post` 엔티티에 연결하고 함께 저장합니다.

-   **`updatePost(Long postId, ..., PostRequestDto request)`:**
    1.  **이미지 삭제**: `request.getDeletedImages()` 목록이 있으면 `imageUploadUtil.deleteImages()`를 호출하고, DB에서도 해당 `PostImage`를 삭제합니다.
    2.  **이미지 추가**: `request.getImageFiles()` 목록이 있으면 `imageUploadUtil.uploadImages()`를 호출하여 새 이미지를 업로드하고 `PostImage` 엔티티를 생성하여 저장합니다.
    3.  **순서 재정렬**: 남아있는 전체 이미지의 `displayOrder`를 0부터 다시 설정합니다.

-   **`deletePost(Long postId, ...)`:**
    1.  게시글에 연결된 모든 `PostImage`를 조회합니다.
    2.  `imageUploadUtil.deleteImages()`를 호출하여 Cloudinary에서 모든 이미지를 삭제합니다.
    3.  `Post`가 삭제될 때 `PostImage`도 함께 삭제되도록 `CascadeType.ALL`과 `orphanRemoval=true`를 활용합니다.

-   **`getPostDetails(Long postId)` 및 `getPostList(...)`:**
    1.  게시글을 조회할 때 `PostImage`도 함께 조회합니다. (N+1 방지를 위해 `fetch join` 또는 `batch_fetch_size` 사용)
    2.  조회된 이미지 URL을 `PostResponseDto`의 `imageUrls` 필드에 담아 반환합니다.

### 1.4. `PostController` 수정

-   게시글 생성(`POST /community-posts`) 및 수정(`POST /community-posts/{id}`) 메서드가 `multipart/form-data`를 처리하도록 변경합니다.
-   `@RequestPart` 어노테이션을 사용하여 DTO와 `MultipartFile`을 함께 받도록 시그니처를 수정합니다.

## 2. 프론트엔드 구현 계획 (`community-posts/form.html`)

`group-purchases/form.html`의 이미지 처리 UI/UX를 참고하여 구현합니다.

1.  **Form 수정**:
    -   `<form>` 태그에 `enctype="multipart/form-data"` 속성을 추가합니다.

2.  **이미지 업로드 필드 추가**:
    -   새 이미지를 업로드할 수 있는 `<input type="file" name="imageFiles" multiple>` 필드를 추가합니다.
    -   파일 형식(`accept="image/jpeg,image/png"`)과 개수/용량 제한에 대한 안내 문구를 포함합니다.

3.  **기존 이미지 표시 및 삭제 (수정 모드)**:
    -   게시글 수정 시, 기존 이미지들을 미리보기로 보여줍니다.
    -   각 이미지마다 '삭제' 버튼을 추가합니다.
    -   삭제 버튼 클릭 시, 해당 이미지 URL을 저장할 `<input type="hidden" name="deletedImages">` 필드를 사용합니다.
    -   JavaScript를 사용하여 삭제 버튼 클릭 시 UI를 비활성화하고 hidden input에 URL을 추가하는 로직을 구현합니다.

4.  **새 이미지 미리보기**:
    -   사용자가 새 이미지 파일을 선택하면, JavaScript를 통해 파일 유효성(개수, 크기, 타입)을 검사하고 미리보기를 동적으로 생성합니다.
    -   미리보기에는 각 이미지를 개별적으로 제거할 수 있는 버튼을 포함합니다.

5.  **스크립트 적용**:
    -   `group-purchases/form.html`에 있는 이미지 미리보기, 삭제 처리, 유효성 검사 관련 JavaScript 코드를 `community-posts/form.html`에 맞게 수정하여 적용합니다.
