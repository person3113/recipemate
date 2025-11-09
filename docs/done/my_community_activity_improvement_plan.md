# '내 커뮤니티 활동' 기능 개선 계획

현재 구현된 '내 커뮤니티 활동' 기능에 대한 추가 개선 요구사항을 반영한 구체적인 실행 계획입니다.

## 1. 개선 목표
- **삭제된 콘텐츠 조회**: 사용자가 자신의 '내 커뮤니티 활동' 페이지에서는 삭제 처리한 게시글과 댓글의 내용을 확인할 수 있도록 합니다.
- **삭제 상태 명시**: 삭제된 콘텐츠(게시글/댓글)는 UI에 명확히 '삭제됨'으로 표시한다.
  - 참고) 해당 게시글 상세페이지에 가거나 댓글의 수정/삭제와 같은 추가 액션은 불가능하도록 제한합니다.
- **연관 콘텐츠 처리**: 원본 게시글이 삭제된 경우, 해당 게시글에 달린 내 댓글은 '삭제된 게시글의 댓글'임을 명시합니다.
- **계층 구조 표시**: 댓글과 대댓글을 시각적으로 구분하여 가독성을 높입니다.

```text
- http://localhost:8080/users/me/community?tab=comments 에서 삭제된 댓글은 화면에 안 나오는데. 다 포함해서 나오도록. 
	- 삭제된 댓글은 삭제됨 정도의 간단한 표시만 하고. 삭제된 내용의 댓글 내용은 여기서는 볼 수 있도록.
	- 이는 http://localhost:8080/users/me/community?tab=posts 에서도 삭제된 자기 게시글 내용만은 볼 수 있도록.
	- 단 삭제된 댓글이나 게시글은 수정/삭제 안 되니까 버튼 표시하지 말고. 백엔드에서도 에러를 내든지. error코드 등으로.
- 게시글이 삭제됐을 때 해당 게시글 댓글은 -> 해당 게시글이 삭제되었다는 간단한 표시 나타내기
- 댓글과 대댓글은 간단히 구분하는 표시 나타내기. 
```

## 2. 주요 변경 및 추가 파일 목록

### Backend
- **Entity (수정 제안)**: `src/main/java/com/recipemate/domain/comment/entity/Comment.java`
- **Service (수정)**: `src/main/java/com/recipemate/domain/user/service/UserService.java`, `PostService`, `CommentService` (해당 파일들이 존재한다고 가정)
- **Repository (수정)**:
    - `src/main/java/com/recipemate/domain/post/repository/PostRepository.java`
    - `src/main/java/com/recipemate/domain/comment/repository/CommentRepository.java`
- **DTO (수정)**:
    - `src/main/java/com/recipemate/domain/user/dto/MyPostDto.java`
    - `src/main/java/com/recipemate/domain/user/dto/MyCommentDto.java`

### Frontend
- **Template (수정)**: `src/main/resources/templates/user/my-community-activity.html`

## 3. 세부 개선 계획

### 3.1. Backend

#### 1. Entity 수정 (제안)
- **`Comment.java`**: 현재 `markAsDeleted()` 메서드는 댓글 내용을 "삭제된 댓글입니다"로 덮어쓰고 있습니다. 이는 원본 내용을 보존하려는 요구사항과 충돌합니다. 이 로직을 제거하여 `deletedAt` 플래그만 설정하도록 변경하는 것을 강력히 권장합니다. 표시 로직은 프론트엔드에서 담당하는 것이 바람직합니다.
  ```java
  // Comment.java - 수정 제안
  public void markAsDeleted() {
      // this.content = "삭제된 댓글입니다"; // 이 라인을 제거하거나 주석 처리
      this.delete(); // BaseEntity의 delete() 메서드 호출 (soft delete)
  }
  ```

#### 2. Repository 수정
- '내 커뮤니티 활동' 페이지에서는 삭제된 데이터를 포함하여 조회해야 하므로, `deletedAt IS NULL` 조건을 제거한 새로운 조회 메서드를 추가합니다.
  ```java
  // PostRepository.java
  @Query("SELECT new com.recipemate.domain.user.dto.MyPostDto(...) FROM Post p WHERE p.author = :user")
  Page<MyPostDto> findPostsByUserForMyActivity(@Param("user") User user, Pageable pageable);

  // CommentRepository.java
  @Query("SELECT c FROM Comment c JOIN FETCH c.post p WHERE c.author = :author AND c.post IS NOT NULL ORDER BY c.createdAt DESC")
  Page<Comment> findByAuthorForMyActivity(@Param("author") User author, Pageable pageable);
  ```
  - **참고**: DTO 프로젝션 쿼리(`MyPostDto`) 내부의 집계 함수(COUNT)는 그대로 유지하되, 메인 WHERE 절에서 `p.deletedAt IS NULL` 조건만 제거합니다.

#### 3. DTO 수정
- 삭제 및 계층 상태를 프론트엔드로 전달하기 위해 DTO 필드를 확장합니다.
  ```java
  // MyPostDto.java
  @Getter
  @AllArgsConstructor
  public class MyPostDto {
      // 기존 필드...
      private LocalDateTime deletedAt; // 삭제 여부 확인용
  }

  // MyCommentDto.java
  @Getter
  @AllArgsConstructor
  public class MyCommentDto {
      // 기존 필드...
      private LocalDateTime deletedAt;      // 댓글 삭제 여부
      private LocalDateTime postDeletedAt;  // 원본 게시글 삭제 여부
      private boolean isReply;              // 대댓글 여부
  }
  ```

#### 4. Service 로직 수정
- `UserService`에서 새로운 Repository 메서드를 호출하고, 확장된 DTO를 생성하도록 로직을 수정합니다.
  ```java
  // UserService.java

  public Page<MyPostDto> findMyPosts(User user, Pageable pageable) {
      // findPostsByUserWithCounts -> findPostsByUserForMyActivity 호출
      return postRepository.findPostsByUserForMyActivity(user, pageable);
  }

  public Page<MyCommentDto> findMyComments(User user, Pageable pageable) {
      // findByAuthorAndPostIsNotNull -> findByAuthorForMyActivity 호출
      Page<Comment> comments = commentRepository.findByAuthorForMyActivity(user, pageable);
      return comments.map(comment -> new MyCommentDto(
          comment.getPost().getId(),
          comment.getPost().getTitle(),
          comment.getContent(),
          comment.getCreatedAt(),
          comment.getDeletedAt(), // 댓글의 삭제 시간
          comment.getPost().getDeletedAt(), // 게시글의 삭제 시간
          comment.getParent() != null // 부모 댓글이 있으면 대댓글
      ));
  }
  ```

#### 5. 수정/삭제 방어 로직 추가
- `PostService` 및 `CommentService` (또는 관련 서비스)의 수정/삭제 메서드 시작 부분에 이미 삭제된 항목인지 확인하는 로직을 추가합니다.
  ```java
  // 예시: PostService.java
  public void updatePost(Long postId, /*...dto...*/) {
      Post post = postRepository.findById(postId).orElseThrow(...);
      if (post.getDeletedAt() != null) {
          throw new CustomException(ErrorCode.ALREADY_DELETED_POST); // 예시 에러 코드
      }
      // ...수정 로직...
  }
  ```

### 3.2. Frontend

#### 1. `my-community-activity.html` 템플릿 수정
- DTO에 추가된 필드를 사용하여 조건부 렌더링을 적용합니다.

**내가 쓴 글 (`posts` 탭):**
```html
<a th:each="post : ${posts}"
   th:href="${post.deletedAt == null} ? @{/community-posts/{id}(id=${post.id})} : '#'"
   class="list-group-item list-group-item-action"
   th:classappend="${post.deletedAt != null} ? 'text-muted bg-light' : ''">
    <div class="d-flex w-100 justify-content-between align-items-start">
        <div class="flex-grow-1">
            <h6 class="mb-1 fw-semibold">
                <span th:if="${post.deletedAt != null}" class="badge bg-secondary me-2">삭제됨</span>
                <span th:text="${post.title}">게시글 제목</span>
            </h6>
            <!-- ... -->
        </div>
        <!-- ... -->
    </div>
</a>
```

**내가 쓴 댓글 (`comments` 탭):**
```html
<div th:each="comment : ${comments}"
     class="list-group-item"
     th:classappend="${comment.isReply} ? 'ps-5' : ''"> <!-- 대댓글 들여쓰기 -->

    <!-- 원본 게시글이 삭제된 경우 -->
    <div th:if="${comment.postDeletedAt != null}">
        <p class="mb-1 fst-italic text-danger">
            <i class="bi bi-exclamation-circle"></i>
            삭제된 게시글에 작성한 댓글입니다.
        </p>
        <p class="mb-2 text-muted" th:text="${comment.content}">댓글 내용</p>
    </div>

    <!-- 원본 게시글이 존재하는 경우 -->
    <a th:if="${comment.postDeletedAt == null}"
       th:href="@{/community-posts/{id}(id=${comment.postId})}"
       class="text-decoration-none text-dark">
        <p class="mb-1 text-muted small">
            <i class="bi bi-file-text"></i>
            <span th:text="${comment.postTitle}">게시글 제목</span>
        </p>
        <p class="mb-2" th:classappend="${comment.deletedAt != null} ? 'text-muted fst-italic' : ''">
            <span th:if="${comment.isReply}" class="me-2">↳</span> <!-- 대댓글 아이콘 -->
            <span th:if="${comment.deletedAt != null}" class="badge bg-secondary me-2">삭제됨</span>
            <span th:text="${comment.content}">댓글 내용</span>
        </p>
    </a>

    <small class="text-muted">
        <i class="bi bi-calendar3"></i>
        <span th:text="${#temporals.format(comment.createdAt, 'yyyy-MM-dd HH:mm')}"></span>
    </small>
</div>
```

## 4. 작업 순서 제안
1.  **Backend (Bottom-up)**
    1.  `Comment` 엔티티의 `markAsDeleted` 메서드 수정 (내용 변경 로직 제거).
    2.  `MyPostDto`, `MyCommentDto`에 필드 추가.
    3.  `PostRepository`, `CommentRepository`에 삭제된 항목을 포함하여 조회하는 새 메서드 추가.
    4.  `UserService`가 새 Repository 메서드를 사용하고 확장된 DTO를 반환하도록 수정.
    5.  `PostService`, `CommentService`에 수정/삭제 방어 로직 추가.
2.  **Frontend**: `my-community-activity.html`을 수정하여 DTO의 새 필드를 사용한 조건부 렌더링 적용.
3.  **통합 테스트**: 각 탭에서 삭제된 게시글/댓글이 의도대로 표시되는지, 링크와 버튼이 올바르게 활성/비활성되는지 확인.
