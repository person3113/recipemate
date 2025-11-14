# 커뮤니티 게시글 정렬 기능 구현 계획 (v3 - 최종)

## 1. 개요 및 최종 목표

- **목표**: 커뮤니티 게시글 목록 페이지에서 **필터링 조건(카테고리, 검색어)을 유지**하면서, 별도의 **정렬 링크(토글)를 통해 게시글 순서를 변경**하는 기능을 구현합니다.
- **상호작용 모델**:
  1. 사용자는 카테고리 탭을 클릭하거나 검색어를 입력하여 게시글을 **필터링**합니다.
  2. 필터링된 결과 내에서 '최신순', '조회수순' 등의 정렬 링크를 클릭하여 결과를 **재정렬**합니다.
  3. 정렬 링크 클릭 시, 페이지가 **새로고침**되어도 괜찮지만, 기존 필터링 조건은 반드시 유지되어야 합니다.

이전의 `htmx`를 사용한 동적 업데이트 계획은 요구사항보다 복잡하므로 철회하고, 이 최종 목표에 부합하는 더 간단한 방식으로 구현합니다.

---

## 2. 기술적 분석 및 구현 방안

페이지 전체를 새로고침하는 단순 링크 방식으로 구현합니다. 이 방식은 `htmx` 없이 순수 Thymeleaf와 Spring MVC만으로 구현 가능하며, 요구사항을 완벽히 만족시킵니다.

### 2.1. 백엔드 변경 (`PostController.java`)

`listPage` 메서드가 필터링(`category`, `keyword`)과 정렬(`sort`, `dir`) 파라미터를 모두 받아서 처리하도록 합니다.

- **`@RequestParam`**: `category`, `keyword`, `page`, `size`, `sort`, `dir` 파라미터를 모두 받습니다.
- **정렬 로직**: `sort`와 `dir` 파라미터를 기반으로 `Sort` 객체를 동적으로 생성합니다.
- **데이터 조회**: 생성된 `Pageable` 객체와 필터링 파라미터를 `PostService`로 전달하여 데이터를 조회합니다.
- **Model 데이터 전달**: View에서 현재 필터링 및 정렬 상태를 사용하고 유지할 수 있도록, 받은 모든 파라미터를 다시 `Model`에 담아 전달합니다. (`currentCategory`, `keyword`, `currentSort`, `currentDir`)

**예상 코드 (`PostController.java`):**
```java
@GetMapping("/list")
public String listPage(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "latest") String sort, // 정렬 기준
        @RequestParam(defaultValue = "desc") String dir,   // 정렬 방향
        Model model
) {
    // 1. 정렬 정보 생성
    Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    String sortProperty;
    switch (sort) {
        case "views":    sortProperty = "viewCount";   break;
        case "likes":    sortProperty = "likeCount";   break; // JPQL 쿼리 alias 기준
        case "comments": sortProperty = "commentCount"; break; // JPQL 쿼리 alias 기준
        default:         sortProperty = "createdAt";   break;
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

    // 2. 데이터 조회 (필터링 + 정렬)
    // PostCategory 변환 로직은 기존 코드 유지
    PostCategory postCategory = null;
    if (category != null && !category.isEmpty()) {
        try {
            postCategory = PostCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) { /* 무시 */ }
    }
    Page<PostResponse> posts = postService.getPostList(postCategory, keyword, pageable);

    // 3. View에 상태 전달
    model.addAttribute("posts", posts);
    model.addAttribute("currentCategory", category);
    model.addAttribute("keyword", keyword);
    model.addAttribute("currentSort", sort);
    model.addAttribute("currentDir", dir);

    return "community-posts/list";
}
```

### 2.2. 프론트엔드 변경 (`list.html`)

정렬 링크와 페이지네이션 링크가 현재 필터링 조건(`category`, `keyword`)을 항상 포함하도록 `th:href`를 수정합니다.

1.  **정렬 UI 추가**:
    - 검색 폼과 게시글 목록 사이에 정렬 링크들을 배치합니다.
    - 각 링크는 `th:href`를 통해 **현재 필터링 조건(`currentCategory`, `keyword`)을 유지**하면서 `sort`와 `dir` 파라미터를 설정합니다.
    - `th:classappend`를 사용하여 현재 활성화된 정렬 옵션을 시각적으로 강조하고, `th:if`로 정렬 방향 아이콘을 표시합니다.

2.  **페이지네이션 링크 수정**:
    - 페이지 이동 시에도 **현재 필터링과 정렬 조건이 모두 유지**되도록 페이지네이션 링크의 `th:href`에 `category`, `keyword`, `sort`, `dir` 파라미터를 모두 추가합니다.

**예상 코드 (`list.html`):**
```html
<!-- 검색 폼과 게시글 목록 사이에 추가 -->
<div class="d-flex justify-content-end align-items-center gap-3 mb-3">
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='latest', dir=(${currentSort == 'latest' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'latest'} ? 'fw-bold text-primary' : 'text-muted'">
        최신순
        <i th:if="${currentSort == 'latest'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='views', dir=(${currentSort == 'views' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'views'} ? 'fw-bold text-primary' : 'text-muted'">
        조회수순
        <i th:if="${currentSort == 'views'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='likes', dir=(${currentSort == 'likes' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'likes'} ? 'fw-bold text-primary' : 'text-muted'">
        좋아요순
        <i th:if="${currentSort == 'likes'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='comments', dir=(${currentSort == 'comments' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'comments'} ? 'fw-bold text-primary' : 'text-muted'">
        댓글많은순
        <i th:if="${currentSort == 'comments'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
</div>

<!-- ... 게시글 목록 ... -->

<!-- 페이지네이션 링크 수정 -->
<nav th:if="${posts != null and !posts.isEmpty() and posts.totalPages > 1}" class="mt-4">
    <ul class="pagination justify-content-center">
        <!-- Previous/Next 링크에도 모든 파라미터 추가 필요 -->
        <li th:each="i : ${#numbers.sequence(0, posts.totalPages - 1)}"
            class="page-item" th:classappend="${i == posts.number} ? 'active'">
            <a class="page-link"
               th:href="@{/community-posts/list(page=${i}, size=${posts.size}, category=${currentCategory}, keyword=${keyword}, sort=${currentSort}, dir=${currentDir})}"
               th:text="${i + 1}">1</a>
        </li>
    </ul>
</nav>
```

## 3. 데이터 계층 (`PostRepository.java`)

이전 계획과 동일합니다. `likeCount`, `commentCount`로 정렬하기 위해 JPQL 쿼리에서 해당 값들에 alias가 부여되어 있는지 확인이 필요합니다. Spring Data JPA가 `Pageable`의 정렬 정보를 해석하여 `ORDER BY` 절을 동적으로 생성해 줄 것입니다.
