# 게시글 조회수 중복 증가 문제 해결 계획

## 1. 문제 분석

- **현상:** 게시글 상세 페이지를 새로고침할 때마다 조회수가 계속해서 증가합니다.
- **원인:** `PostService.getPostDetail` 메서드가 CQS(Command Query Separation, 명령-조회 분리) 원칙을 위반하고 있습니다. 현재 이 메서드는 게시글 데이터를 조회하는 동시에(`Query`) 조회수를 증가시키는(`Command`) 두 가지 역할을 모두 수행합니다. 이로 인해 페이지를 요청할 때마다 조회수가 무조건적으로 증가하게 됩니다.

## 2. 해결 전략

CQS 원칙에 따라 로직을 리팩터링하고, `HttpSession`을 이용해 사용자의 조회 기록을 관리하여 중복 조회를 방지합니다.

## 3. 리팩터링 계획

### 3.1. `PostService.java` 수정

1.  **조회수 증가 메서드 분리:**
    -   `post.increaseViewCount()` 로직을 `getPostDetail` 메서드에서 분리하여, 새로운 `public void increasePostViewCount(Long postId)` 메서드를 생성합니다.
    -   이 신규 메서드는 `@Transactional` 어노테이션을 사용하여 트랜잭션 내에서 실행되도록 합니다.
    -   `getPostDetail` 메서드에서는 조회수 증가 로직을 완전히 제거하고, `@Transactional(readOnly = true)`를 적용하여 순수한 조회용 메서드로 변경합니다.

    ```java
    // AS-IS: PostService.java
    @Transactional
    public PostDto.Response getPostDetail(Long postId) {
        Post post = // ... post 조회 ...
        post.increaseViewCount(); // 문제의 원인
        return PostDto.Response.from(post);
    }

    // TO-BE: PostService.java
    @Transactional(readOnly = true)
    public PostDto.Response getPostDetail(Long postId) {
        Post post = // ... post 조회 ...
        return PostDto.Response.from(post);
    }

    @Transactional
    public void increasePostViewCount(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        post.increaseViewCount();
    }
    ```

### 3.2. `PostController.java` 수정

1.  **`HttpSession`을 이용한 중복 조회 방지 로직 추가:**
    -   `detailPage` 메서드가 `HttpSession` 객체를 파라미터로 받도록 수정합니다.
    -   세션에서 `viewedPostIds`라는 이름의 `Set<Long>` 타입 속성을 가져옵니다.
    -   만약 세션에 `viewedPostIds`가 없으면 새로 생성합니다.
    -   현재 조회하려는 `postId`가 `viewedPostIds`에 포함되어 있지 **않을** 경우에만 다음을 실행합니다.
        1.  새로 만든 `postService.increasePostViewCount(postId)`를 호출하여 조회수를 증가시킵니다.
        2.  `postId`를 `viewedPostIds`에 추가합니다.
        3.  업데이트된 `viewedPostIds`를 다시 세션에 저장합니다.
    -   이 로직이 실행된 후, 기존처럼 `postService.getPostDetail(...)`을 호출하여 화면에 필요한 데이터를 조회합니다.

    ```java
    // TO-BE: PostController.java
    @GetMapping("/{postId}")
    public String detailPage(@PathVariable Long postId, Model model, HttpSession session) {
        // 세션에서 조회한 게시글 ID 목록 가져오기
        Set<Long> viewedPostIds = (Set<Long>) session.getAttribute("viewedPostIds");
        if (viewedPostIds == null) {
            viewedPostIds = new HashSet<>();
        }

        // 현재 게시글을 처음 조회하는 경우, 조회수 증가 처리
        if (!viewedPostIds.contains(postId)) {
            postService.increasePostViewCount(postId);
            viewedPostIds.add(postId);
            session.setAttribute("viewedPostIds", viewedPostIds);
        }

        PostDto.Response postResponse = postService.getPostDetail(postId);
        model.addAttribute("post", postResponse);
        // ... 기타 로직 ...
        return "community-posts/detail";
    }
    ```

## 4. 기대 효과

-   새로고침 시 조회수가 중복으로 증가하는 버그가 해결됩니다.
-   CQS 원칙을 준수하게 되어 코드의 책임과 역할이 명확해지고, 유지보수성이 향상됩니다.
-   조회(Query)와 상태 변경(Command)이 분리되어 향후 로직 확장 및 테스트가 용이해집니다.
