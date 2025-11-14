# 게시글/댓글 좋아요 기능 추가 계획

> 본 문서는 게시글과 댓글에 '좋아요' 기능을 추가하기 위한 기술적인 설계와 구현 계획을 다룹니다.

## 1. 목표

- 사용자는 게시글과 댓글에 '좋아요'를 누를 수 있습니다.
- '좋아요'는 한 번만 누를 수 있으며, 다시 누르면 취소됩니다 (토글 방식).
- 게시글과 댓글에는 총 '좋아요' 개수가 표시됩니다.
- 사용자가 이미 '좋아요'를 눌렀는지 여부를 확인할 수 있습니다.

## 2. 기술 설계

기존 `Post`, `Comment`, `User` 엔티티와의 관계를 고려하여, '좋아요' 기능을 위한 새로운 도메인을 추가합니다. 유지보수성과 명확성을 위해 `Post`와 `Comment`의 '좋아요'를 별도의 엔티티로 관리합니다.

### 2.1. 데이터베이스 스키마 및 엔티티

#### `PostLike` 엔티티

게시글에 대한 '좋아요' 정보를 저장합니다.

- **패키지 경로**: `com.recipemate.domain.like.entity.PostLike`
- **테이블명**: `post_likes`

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "post_likes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "post_like_uk",
            columnNames = {"user_id", "post_id"}
        )
    }
)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
```

#### `CommentLike` 엔티티

댓글에 대한 '좋아요' 정보를 저장합니다.

- **패키지 경로**: `com.recipemate.domain.like.entity.CommentLike`
- **테이블명**: `comment_likes`

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "comment_likes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "comment_like_uk",
            columnNames = {"user_id", "comment_id"}
        )
    }
)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    public CommentLike(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }
}
```

### 2.2. Repository

- **`PostLikeRepository`**: `PostLike` 엔티티에 대한 데이터베이스 작업을 수행합니다.
  - `findByUserAndPost(User user, Post post)`: 사용자와 게시글로 '좋아요' 존재 여부 확인
  - `countByPost(Post post)`: 게시글의 총 '좋아요' 수 계산
- **`CommentLikeRepository`**: `CommentLike` 엔티티에 대한 데이터베이스 작업을 수행합니다.
  - `findByUserAndComment(User user, Comment comment)`: 사용자와 댓글로 '좋아요' 존재 여부 확인
  - `countByComment(Comment comment)`: 댓글의 총 '좋아요' 수 계산

### 2.3. Service

`LikeService`는 '좋아요' 관련 비즈니스 로직을 담당합니다. 서비스는 토글 작업 후, 업데이트된 '좋아요' 상태와 개수를 담은 DTO를 반환하도록 설계합니다.

```java
// LikeResponseDto 예시
public record LikeResponseDto(boolean isLiked, long likeCount) {}
```

- **`togglePostLike(Long userId, Long postId)`**:
  1. `userId`와 `postId`로 `User`와 `Post`를 조회합니다.
  2. `postLikeRepository.findByUserAndPost()`를 통해 기존 '좋아요' 내역을 조회합니다.
  3. '좋아요'가 존재하면 `delete()`를 통해 삭제 (Unlike).
  4. '좋아요'가 없으면 새로운 `PostLike` 객체를 생성하여 `save()` (Like).
  5. 작업 후 `LikeResponseDto`에 현재 '좋아요' 상태와 총 '좋아요' 수를 담아 반환합니다.

- **`toggleCommentLike(Long userId, Long commentId)`**:
  - `togglePostLike`과 동일한 로직으로 댓글 '좋아요'를 처리합니다.

### 2.4. Controller

`LikeController`는 '좋아요' 기능을 위한 엔드포인트를 제공합니다. HTMX 연동을 위해 JSON 대신 HTML 조각(Fragment)을 반환하도록 설계합니다.

- `POST /likes/post/{postId}`
- `POST /likes/comment/{commentId}`

## 3. 기존 기능 수정

'좋아요' 정보를 클라이언트에 전달하기 위해 기존 DTO와 서비스를 수정해야 합니다.

### 3.1. DTO 수정

`PostResponseDto`와 `CommentResponseDto` (또는 유사한 역할을 하는 DTO)에 다음 필드를 추가합니다.

```java
// 예시: PostResponseDto
private long likeCount; // 총 좋아요 수
private boolean isLiked; // 현재 사용자가 좋아요를 눌렀는지 여부
```

### 3.2. 서비스 수정

- **`PostService`**:
  - 게시글 목록 또는 상세 정보를 조회하는 메서드에서 `PostLikeRepository`를 사용합니다.
  - 각 게시글에 대해 `likeCount`를 계산합니다.
  - 현재 요청을 보낸 사용자를 기준으로 `isLiked` 값을 설정합니다.
- **`CommentService`**:
  - `PostService`와 마찬가지로 댓글 조회 시 `CommentLikeRepository`를 사용하여 `likeCount`와 `isLiked` 값을 설정합니다.

## 4. 프론트엔드 (Thymeleaf) 수정 계획

이 프로젝트는 HTMX를 적극적으로 활용하므로, 페이지 새로고침 없이 '좋아요' 기능을 구현하기 위해 HTMX의 접근 방식을 따릅니다.

### 4.1. `LikeController` 수정

`LikeController`의 엔드포인트가 '좋아요' 버튼 UI를 담은 HTML 조각(Fragment)을 직접 반환하도록 수정합니다.

```java
@Controller
public class LikeController {

    private final LikeService likeService;

    // ... 생성자 ...

    @PostMapping("/likes/post/{postId}")
    public String togglePostLike(@PathVariable Long postId, @AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        // 1. '좋아요' 토글 로직 호출
        LikeResponseDto response = likeService.togglePostLike(userPrincipal.getId(), postId);

        // 2. 모델에 업데이트된 상태 추가
        model.addAttribute("isLiked", response.isLiked());
        model.addAttribute("likeCount", response.likeCount());
        model.addAttribute("targetType", "POST");
        model.addAttribute("targetId", postId);

        // 3. 렌더링할 프래그먼트 반환
        return "fragments/like :: like-button";
    }

    @PostMapping("/likes/comment/{commentId}")
    public String toggleCommentLike(@PathVariable Long commentId, @AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        LikeResponseDto response = likeService.toggleCommentLike(userPrincipal.getId(), commentId);
        model.addAttribute("isLiked", response.isLiked());
        model.addAttribute("likeCount", response.likeCount());
        model.addAttribute("targetType", "COMMENT");
        model.addAttribute("targetId", commentId);
        return "fragments/like :: like-button";
    }
}
```

### 4.2. '좋아요' 버튼 프래그먼트 생성

재사용 가능한 '좋아요' 버튼 프래그먼트를 `src/main/resources/templates/fragments/like.html` 경로에 생성합니다.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<div th:fragment="like-button(isLiked, likeCount, targetType, targetId)"
     th:id="${#strings.toLowerCase(targetType)} + '-like-' + ${targetId}"
     class="like-container d-flex align-items-center gap-2">

    <!-- 로그인한 사용자만 버튼 활성화 -->
    <button sec:authorize="isAuthenticated()"
            type="button"
            class="btn btn-sm like-button"
            th:classappend="${isLiked} ? 'btn-danger' : 'btn-outline-danger'"
            th:hx-post="@{'/likes/' + ${#strings.toLowerCase(targetType)} + '/' + ${targetId}}"
            hx-target="this"
            hx-swap="outerHTML">
        <i class="bi" th:classappend="${isLiked} ? 'bi-heart-fill' : 'bi-heart'"></i>
        <span th:text="${isLiked} ? '좋아요 취소' : '좋아요'">좋아요</span>
    </button>

    <!-- 로그인하지 않은 사용자는 비활성화된 버튼 표시 -->
    <a sec:authorize="!isAuthenticated()"
       th:href="@{/login}"
       class="btn btn-sm btn-outline-danger disabled"
       aria-disabled="true">
        <i class="bi bi-heart"></i>
        <span>좋아요</span>
    </a>

    <span class="like-count fw-bold" th:text="${likeCount}">0</span>
</div>

</html>
```

### 4.3. 기존 템플릿에 프래그먼트 적용

#### `community-posts/detail.html` 수정

게시글 상세 페이지의 `card-footer` 내부에 '좋아요' 버튼 프래그먼트를 추가합니다.

```html
<!-- 기존 코드 ... -->
<div class="card-footer bg-white py-3">
    <div class="d-flex justify-content-between align-items-center">
        <!-- 좋아요 버튼 추가 -->
        <div th:replace="~{fragments/like :: like-button(isLiked=${post.isLiked}, likeCount=${post.likeCount}, targetType='POST', targetId=${post.id})}"></div>

        <!-- Author Actions -->
        <!-- ... 기존 수정/삭제 버튼 ... -->
    </div>
</div>
<!-- ... -->
```

#### `fragments/comments.html` 수정

댓글(`comment-item`) 템플릿 내부에 '좋아요' 버튼을 추가합니다. '답글' 버튼 옆에 배치합니다.

```html
<!-- ... 기존 댓글 내용 ... -->
<!-- 대댓글 버튼과 좋아요 버튼을 함께 배치 -->
<div th:if="${!comment.isDeleted}" class="mt-2 d-flex align-items-center gap-3">
    <button sec:authorize="isAuthenticated()"
            type="button"
            class="btn btn-sm btn-link text-decoration-none p-0"
            onclick="toggleReplyForm(this)"
            th:attr="data-comment-id=${comment.id}">
        <i class="bi bi-reply me-1"></i>
        답글
    </button>

    <!-- 댓글 좋아요 버튼 추가 -->
    <div th:replace="~{fragments/like :: like-button(isLiked=${comment.isLiked}, likeCount=${comment.likeCount}, targetType='COMMENT', targetId=${comment.id})}"></div>
</div>
<!-- ... -->
```

### 4.4. CSS 스타일 추가 (선택 사항)

`src/main/resources/static/css/styles.css` 파일에 '좋아요' 버튼에 대한 간단한 스타일을 추가하여 사용자 경험을 개선할 수 있습니다.

```css
.like-button .bi-heart-fill {
    /* '좋아요' 상태일 때 하트 아이콘 색상 (이미 btn-danger로 처리됨) */
}

.like-button.btn-outline-danger:hover {
    /* 마우스 오버 시 아이콘과 텍스트 색상 변경 (Bootstrap 기본 동작) */
}
```

## 5. 구현 단계

1.  `like` 도메인 패키지 생성 (`entity`, `repository`, `service`, `controller`, `dto`).
2.  `PostLike`, `CommentLike` 엔티티 및 `PostLikeRepository`, `CommentLikeRepository` 인터페이스 정의.
3.  `LikeService`와 `LikeController` 구현 (HTML 프래그먼트 반환 방식).
4.  `fragments/like.html` 프래그먼트 파일 생성.
5.  `Post` 및 `Comment` 관련 DTO에 `likeCount`, `isLiked` 필드 추가.
6.  `PostService` 및 `CommentService`를 수정하여 '좋아요' 관련 데이터를 DTO에 채워 넣도록 로직 변경.
7.  `community-posts/detail.html`과 `fragments/comments.html`에 '좋아요' 프래그먼트 적용.
8.  기능 테스트 및 UI 검증.
