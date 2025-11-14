# 커뮤니티 게시글 목록에 댓글 수 표시 기능 추가 계획

## 1. 목표

커뮤니티 게시글 목록 페이지(`/community-posts/list`)의 각 게시글에 조회수, **댓글 수**, 좋아요 수를 함께 표시합니다.
- **표시 순서:** 👁️ (조회수) 💬 (댓글 수) ❤️ (좋아요 수)

## 2. 관련 파일

- `PostController.java`: 게시글 목록 페이지 요청을 처리하는 컨트롤러
- `PostService.java`: 게시글 목록을 가져오는 비즈니스 로직을 담은 서비스
- `PostResponse.java`: View로 데이터를 전달하는 DTO
- `PostRepository.java`: 게시글 데이터 조회를 담당하는 레포지토리
- `list.html`: 게시글 목록을 표시하는 Thymeleaf 템플릿

## 3. 실행 계획

### 3.1. `PostResponse` DTO 수정

`PostResponse` DTO에 댓글 수를 담을 `commentCount` 필드를 추가합니다.

- **파일:** `src/main/java/com/recipemate/domain/post/dto/PostResponse.java`
- **변경 사항:**
    ```java
    // 기존 필드 ...
    private Integer viewCount;
    private long likeCount;
    private long commentCount; // 추가
    // ...
    ```

### 3.2. `PostRepository` 및 `PostService` 수정

`PostService`가 게시글 목록을 조회할 때 댓글 수도 함께 가져오도록 `PostRepository`의 쿼리를 수정하고, `PostService`의 로직을 변경합니다.

- **파일:** `src/main/java/com/recipemate/domain/post/repository/PostRepository.java`
- **변경 사항:** 게시글을 조회하는 JPQL 쿼리에 댓글 수를 계산하는 서브쿼리 또는 조인을 추가합니다. `Post`와 `Comment` 엔티티의 관계를 활용합니다.

- **파일:** `src/main/java/com/recipemate/domain/post/service/PostService.java`
- **변경 사항:** `PostRepository`에서 가져온 댓글 수 데이터를 `PostResponse` DTO의 `commentCount` 필드에 매핑합니다. 기존의 `viewCount`, `likeCount` 처리 방식을 참고하여 일관성 있게 구현합니다.

### 3.3. `list.html` 템플릿 수정

Thymeleaf 템플릿을 수정하여 댓글 수를 화면에 표시합니다.

- **파일:** `src/main/resources/templates/community-posts/list.html`
- **변경 사항:**
    - 게시글 정보를 반복하는 부분에 `post.commentCount` 값을 출력하는 코드를 추가합니다.
    - 아이콘과 함께 `조회수 - 댓글 수 - 좋아요 수` 순서로 표시되도록 HTML 구조를 조정합니다.

    ```html
    <!-- 예시 코드 -->
    <span>👁️ <span th:text="${post.viewCount}"></span></span>
    <span>💬 <span th:text="${post.commentCount}"></span></span> <!-- 추가될 부분 -->
    <span>❤️ <span th:text="${post.likeCount}"></span></span>
    ```

### 4.성능 해결 방안

N+1 문제를 피하고 효율적으로 댓글 수를 가져오기 위해, `PostRepository`에서 게시물 목록을 조회하는 JPQL 쿼리 자체에 댓글 수를 포함시키는 것이 가장 좋은 해결책입니다. `LEFT JOIN`과 `GROUP BY`를 사용하거나, `SELECT` 절에 스칼라 서브쿼리(scalar subquery)를 사용하여 한 번의 쿼리로 게시물 목록과 각 게시물의 댓글 수를 함께 조회하도록 구현합니다.

## 5. 예상 결과

위 계획대로 진행하면, 커뮤니티 게시글 목록에서 각 게시글의 조회수, 댓글 수, 좋아요 수를 N+1 문제 없이 효율적으로 한 번에 조회하여 확인할 수 있게 됩니다.