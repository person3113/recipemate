# 마이페이지 '내 커뮤니티 활동' 탭 추가 계획 (보완)

## 1. 목표
마이페이지에 '커뮤니티' 탭을 추가하여, 사용자가 **자신이 작성한 게시글**, **작성한 댓글**, 그리고 **좋아요한 글**을 모아볼 수 있는 기능을 구현한다. 이는 `docs/project_plan/7-4_커뮤니티탭.md`의 기획을 구체화하고, 기존 코드베이스의 효율적인 조회 방식을 적용하여 개선하는 것을 목표로 한다.

## 2. 주요 변경 및 추가 파일 목록

### Backend
- **Controller (수정)**: `src/main/java/com/recipemate/domain/user/UserController.java`
- **Service (수정)**: `src/main/java/com/recipemate/domain/user/UserService.java`
- **Repository (수정)**:
    - `src/main/java/com/recipemate/domain/post/PostRepository.java`
    - `src/main/java/com/recipemate/domain/comment/CommentRepository.java`
    - `src/main/java/com/recipemate/domain/like/PostLikeRepository.java`
- **DTO (신규 생성)**:
    - `src/main/java/com/recipemate/domain/user/dto/MyPostDto.java`
    - `src/main/java/com/recipemate/domain/user/dto/MyCommentDto.java`
    - `src/main/java/com/recipemate/domain/user/dto/MyLikedPostDto.java`

### Frontend
- **Template (수정)**: `src/main/resources/templates/user/my-page.html`
- **Template (신규 생성)**: `src/main/resources/templates/user/my-community-activity.html`

## 3. 세부 구현 계획

### 3.1. Backend

#### 1. Repository 수정
- 각 Repository에 사용자를 기준으로 페이징 처리된 데이터를 조회하는 메서드를 추가한다.
  ```java
  // PostRepository.java
  // N+1 문제를 방지하기 위해 DTO로 직접 조회
  @Query("SELECT new com.recipemate.domain.user.dto.MyPostDto(p.id, p.title, p.createdAt, p.viewCount, (SELECT COUNT(c) FROM Comment c WHERE c.post = p), (SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = p)) FROM Post p WHERE p.author = :user")
  Page<MyPostDto> findPostsByUserWithCounts(@Param("user") User user, Pageable pageable);

  // CommentRepository.java
  Page<Comment> findByAuthor(User author, Pageable pageable);

  // PostLikeRepository.java
  Page<PostLike> findByUser(User user, Pageable pageable);
  ```

#### 2. DTO 생성
- 각 탭에 필요한 최소한의 데이터만 전달하기 위한 DTO를 생성한다.
  ```java
  // MyPostDto.java
  @Getter
  @AllArgsConstructor
  public class MyPostDto {
      private Long id;
      private String title;
      private LocalDateTime createdAt;
      private int viewCount;
      private Long commentCount; // COUNT 쿼리 결과는 Long 타입
      private Long likeCount;    // COUNT 쿼리 결과는 Long 타입
  }

  // MyCommentDto.java
  @Getter
  @AllArgsConstructor
  public class MyCommentDto {
      private Long postId;
      private String postTitle;
      private String content;
      private LocalDateTime createdAt;
  }

  // MyLikedPostDto.java (MyPostDto와 구조가 동일하여 재사용 가능성 검토)
  // 우선 명확한 구분을 위해 별도 생성
  @Getter
  @AllArgsConstructor
  public class MyLikedPostDto {
      private Long id;
      private String title;
      private LocalDateTime createdAt;
      private int viewCount;
      private Long commentCount;
      private Long likeCount;
  }
  ```

#### 3. Service 로직 추가
- `UserService`에 각 탭에 필요한 데이터를 조회하는 비즈니스 로직을 구현한다.
  ```java
  // UserService.java
  public Page<MyPostDto> findMyPosts(User user, Pageable pageable) {
      return postRepository.findPostsByUserWithCounts(user, pageable);
  }

  public Page<MyCommentDto> findMyComments(User user, Pageable pageable) {
      Page<Comment> comments = commentRepository.findByAuthor(user, pageable);
      return comments.map(comment -> new MyCommentDto(
          comment.getPost().getId(),
          comment.getPost().getTitle(),
          comment.getContent(),
          comment.getCreatedAt()
      ));
  }

  public Page<MyLikedPostDto> findMyLikedPosts(User user, Pageable pageable) {
      Page<PostLike> likedPosts = postLikeRepository.findByUser(user, pageable);
      // PostRepository의 PostWithCountsDto 조회 로직을 활용하여 N+1 방지
      List<Long> postIds = likedPosts.getContent().stream().map(pl -> pl.getPost().getId()).toList();
      
      // postRepository에 id 리스트로 PostWithCountsDto를 조회하는 메서드 추가 필요
      // 예: List<PostWithCountsDto> findAllWithCountsByIdIn(List<Long> postIds);
      // 위 메서드 결과를 Page<MyLikedPostDto>로 변환하여 반환
      // (구현의 복잡도를 고려하여, 우선 간단한 방식으로 구현 후 리팩토링도 가능)
      
      // 간단한 버전:
      return likedPosts.map(postLike -> {
          Post post = postLike.getPost();
          // 이 방식은 N+1 발생 가능성이 매우 높음
          long commentCount = commentRepository.countByPostId(post.getId());
          long likeCount = postLikeRepository.countByPost(post);
          return new MyLikedPostDto(
              post.getId(),
              post.getTitle(),
              post.getCreatedAt(),
              post.getViewCount(),
              commentCount,
              likeCount
          );
      });
  }
  ```
  **참고**: `findMyLikedPosts`의 N+1 문제는 `PostRepository`에 `List<PostWithCountsDto> findAllWithCountsByIdIn(List<Long> postIds)` 같은 메서드를 추가하여 한 번의 쿼리로 관련 데이터를 모두 가져온 후, 서비스단에서 DTO로 조립하여 해결하는 것이 가장 이상적이다.

#### 4. Controller 엔드포인트 추가
- `UserController`에 '내 커뮤니티 활동' 페이지와 3개의 탭을 처리할 엔드포인트를 추가한다.
  ```java
  // UserController.java
  @GetMapping("/my-community")
  public String getMyCommunityActivityPage(@RequestParam(defaultValue = "posts") String tab, Model model, @LoginUser User user, Pageable pageable) {
      if ("posts".equals(tab)) {
          model.addAttribute("posts", userService.findMyPosts(user, pageable));
      } else if ("comments".equals(tab)) {
          model.addAttribute("comments", userService.findMyComments(user, pageable));
      } else if ("likes".equals(tab)) {
          model.addAttribute("likedPosts", userService.findMyLikedPosts(user, pageable));
      }
      model.addAttribute("currentTab", tab);
      return "user/my-community-activity";
  }
  ```

### 3.2. Frontend

#### 1. `my-page.html` 수정
- 마이페이지의 바로가기 목록에 '내 커뮤니티 활동' 링크를 추가한다.
  ```html
  <!-- my-page.html -->
  ...
  <a href="/user/participations" class="list-group-item list-group-item-action">참여중인 공구</a>
  <a href="/user/my-community" class="list-group-item list-group-item-action">내 커뮤니티 활동</a>
  <a href="/user/bookmarks" class="list-group-item list-group-item-action">내 찜 목록</a>
  ...
  ```

#### 2. `my-community-activity.html` 신규 생성
- 3개의 탭 UI를 포함한 새로운 템플릿 파일을 생성한다.
- `th:if`를 사용하여 선택된 탭에 따라 적절한 목록을 렌더링한다.

```html
<!-- user/my-community-activity.html -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org" th:replace="~{layout/base :: layout(~{::title}, ~{::section})}">
<head>
    <title>내 커뮤니티 활동</title>
</head>
<body>
<section>
    <div class="container">
        <h2>내 커뮤니티 활동</h2>

        <!-- 탭 네비게이션 -->
        <ul class="nav nav-tabs mb-3">
            <li class="nav-item">
                <a class="nav-link" th:classappend="${currentTab == 'posts' ? 'active' : ''}" th:href="@{/user/my-community(tab='posts')}">내가 쓴 글</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" th:classappend="${currentTab == 'comments' ? 'active' : ''}" th:href="@{/user/my-community(tab='comments')}">내가 쓴 댓글</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" th:classappend="${currentTab == 'likes' ? 'active' : ''}" th:href="@{/user/my-community(tab='likes')}">좋아요한 글</a>
            </li>
        </ul>

        <!-- 내가 쓴 글 목록 -->
        <div th:if="${currentTab == 'posts'}">
            <!-- post-list fragment 재사용 고려 -->
        </div>

        <!-- 내가 쓴 댓글 목록 -->
        <div th:if="${currentTab == 'comments'}">
            <!-- 댓글 목록 UI -->
        </div>

        <!-- 좋아요한 글 목록 -->
        <div th:if="${currentTab == 'likes'}">
            <!-- post-list fragment 재사용 고려 -->
        </div>
    </div>
</section>
</body>
</html>
```

## 4. 작업 순서 제안
1.  **Backend (Bottom-up)**: Repository -> DTO -> Service -> Controller 순서로 개발.
2.  **N+1 최적화**: `findMyLikedPosts` 서비스 메서드의 성능 최적화 방안을 논의하고 적용한다.
3.  **Frontend**: `my-community-activity.html` 템플릿 생성 및 `my-page.html` 링크 추가.
