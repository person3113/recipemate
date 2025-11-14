# 레시피 및 공동구매 목록 정렬 기능 개선 계획

## 1. 개요

### 1.1. 목표
- 레시피와 공동구매 목록 페이지의 정렬 UI를 커뮤니티 게시판처럼 **토글 방식의 링크**로 변경합니다.
- 이를 통해 사이트 전체의 사용자 경험(UX)을 통일하고, 필터링 조건이 유지된 상태에서 직관적인 정렬이 가능하도록 개선합니다.

### 1.2. 현재 상태 및 문제점
- **현재 상태**: `<form>` 내부에 `<select>` 드롭다운 메뉴를 사용하여 정렬 기준과 방향을 선택하고, '검색' 버튼을 눌러야만 적용됩니다.
- **문제점**:
    - 정렬만 변경하고 싶을 때도 불필요하게 폼 전체를 다시 제출하는 인상을 줍니다.
    - 커뮤니티 게시판의 정렬 방식(`<a>` 태그 링크)과 달라 일관성이 부족합니다.
    - 정렬 방향(오름차순/내림차순)을 토글하는 직관적인 경험을 제공하지 못합니다.

### 1.3. 개선 방향
- 기존 `<select>` 태그를 제거하고, 각 정렬 기준에 해당하는 `<a>` 태그 링크를 노출시킵니다.
- 각 링크는 클릭 시 현재 필터링 조건을 모두 유지한 채 페이지를 새로고침하여 결과를 즉시 재정렬합니다.
- 현재 활성화된 정렬 기준과 방향을 시각적으로 명확히 표시합니다. (e.g., 굵은 글씨, 아이콘)

---

## 2. 기술 분석 결과

코드베이스 분석 결과, 두 기능 모두 백엔드에서는 이미 동적 정렬을 완벽하게 지원하고 있습니다. 따라서 **주요 변경 사항은 프론트엔드(Thymeleaf)와 컨트롤러의 모델 전달 로직에 집중**됩니다.

- **레시피 (`Recipe`)**:
    - `RecipeController`는 `sort`와 `direction` 요청 파라미터를 받습니다.
    - `RecipeService`에서 QueryDSL을 통해 `latest`(최신순), `name`(이름순), `popularity`(인기순) 정렬을 처리합니다.
- **공동구매 (`GroupBuy`)**:
    - `GroupBuyController`는 `SearchCondition` 객체로 `sortBy`와 `direction` 파라미터를 받습니다.
    - `GroupBuyRepositoryImpl`에서 QueryDSL을 통해 `latest`(최신순), `deadline`(마감임박순), `participants`(참여자순), `price`(가격순) 정렬을 처리합니다.

---

## 3. 구현 계획

### 3.1. Phase 1: 레시피 목록 페이지 개선

#### 3.1.1. `RecipeController` 수정
`searchRecipesPage` 메서드에 현재 정렬 상태를 `Model`에 담아 View로 전달하는 코드를 추가합니다.

**파일**: `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`
```java
// public String searchRecipesPage(...) 메서드 내부 끝부분에 추가

// ... 데이터 조회 로직 후 ...

// View에서 현재 정렬 상태를 사용하기 위해 모델에 추가
model.addAttribute("sort", sort);
model.addAttribute("direction", direction);

return "recipes/list";
```

#### 3.1.2. `recipes/list.html` 수정
정렬 `<select>` 태그를 제거하고, `<a>` 태그 기반의 정렬 링크를 추가합니다.

**파일**: `src/main/resources/templates/recipes/list.html`

1.  **기존 `<select>` 태그 2개 삭제**: 검색 폼 안에 있는 `sort`와 `direction` `<div>`를 삭제합니다.
    ```html
    <!-- 아래 두 블록을 삭제 -->
    <div class="col-md-2">
        <select name="sort" class="form-select">
            ...
        </select>
    </div>
    <div class="col-md-2">
        <select name="direction" class="form-select">
            ...
        </select>
    </div>
    ```

2.  **정렬 링크 추가**: 검색 폼(`</div class="card">`)과 "랜덤 레시피 보기" 버튼(`</div class="mb-4">`) 사이에 아래 코드를 추가합니다.
    ```html
    <!-- Sort Options -->
    <div class="d-flex justify-content-end align-items-center gap-3 mb-3">
        <a th:href="@{/recipes(keyword=${keyword}, ingredients=${ingredients}, category=${category}, sort='latest', dir=(${sort == 'latest' && direction == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${sort == 'latest' || sort == null} ? 'fw-bold text-primary' : 'text-muted'">
            최신순
            <i th:if="${sort == 'latest' || sort == null}" class="bi" th:classappend="${direction == 'desc' || direction == null} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
        <a th:href="@{/recipes(keyword=${keyword}, ingredients=${ingredients}, category=${category}, sort='name', dir=(${sort == 'name' && direction == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${sort == 'name'} ? 'fw-bold text-primary' : 'text-muted'">
            이름순
            <i th:if="${sort == 'name'}" class="bi" th:classappend="${direction == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
        <a th:href="@{/recipes(keyword=${keyword}, ingredients=${ingredients}, category=${category}, sort='popularity', dir=(${sort == 'popularity' && direction == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${sort == 'popularity'} ? 'fw-bold text-primary' : 'text-muted'">
            인기순
            <i th:if="${sort == 'popularity'}" class="bi" th:classappend="${direction == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
    </div>
    ```

3.  **페이지네이션 링크 수정**: 페이지 이동 시 정렬 상태가 유지되도록 `sort`와 `direction` 파라미터를 추가합니다. (기존 코드에 이미 반영되어 있어 수정 불필요)

### 3.2. Phase 2: 공동구매 목록 페이지 개선

#### 3.2.1. `GroupBuyController` 수정 (선택 사항)
`GroupBuyController`는 이미 `searchCondition` 객체를 모델에 전달하고 있어 필수적인 수정은 없으나, View에서의 명확성을 위해 현재 정렬 상태를 별도 변수로 전달하는 것을 권장합니다.

**파일**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
```java
// public String listPage(...) 메서드 내부 끝부분에 추가

// ... 데이터 조회 로직 후 ...

// View에서 명확하게 사용할 수 있도록 현재 정렬 정보 추가
model.addAttribute("currentSort", searchCondition.getSortBy());
model.addAttribute("currentDir", searchCondition.getDirection());

return "group-purchases/list";
```

#### 3.2.2. `group-purchases/list.html` 수정
레시피와 동일하게 정렬 UI를 변경합니다.

**파일**: `src/main/resources/templates/group-purchases/list.html`

1.  **기존 `<select>` 태그 2개 삭제**: 검색 폼 안에 있는 `sortBy`와 `direction` `<div>`를 삭제합니다.
    ```html
    <!-- 아래 두 블록을 삭제 -->
    <div class="col-md-3">
        <select name="sortBy" class="form-select">
            ...
        </select>
    </div>
    <div class="col-md-2">
        <select name="direction" class="form-select">
            ...
        </select>
    </div>
    ```

2.  **정렬 링크 추가**: 검색 폼(`</div class="card">`)과 "새 공동구매 만들기" 버튼(`</div class="mb-4">`) 사이에 아래 코드를 추가합니다.
    ```html
    <!-- Sort Options -->
    <div class="d-flex justify-content-end align-items-center gap-3 mb-3">
        <a th:href="@{/group-purchases/list(keyword=${searchCondition.keyword}, ingredients=${searchCondition.ingredients}, category=${searchCondition.category?.name()}, status=${searchCondition.status?.name()}, recipeOnly=${searchCondition.recipeOnly}, sortBy='latest', direction=(${currentSort == 'latest' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${currentSort == 'latest' || currentSort == null} ? 'fw-bold text-primary' : 'text-muted'">
            최신순
            <i th:if="${currentSort == 'latest' || currentSort == null}" class="bi" th:classappend="${currentDir == 'desc' || currentDir == null} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
        <a th:href="@{/group-purchases/list(keyword=${searchCondition.keyword}, ingredients=${searchCondition.ingredients}, category=${searchCondition.category?.name()}, status=${searchCondition.status?.name()}, recipeOnly=${searchCondition.recipeOnly}, sortBy='deadline', direction=(${currentSort == 'deadline' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${currentSort == 'deadline'} ? 'fw-bold text-primary' : 'text-muted'">
            마감임박순
            <i th:if="${currentSort == 'deadline'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
        <a th:href="@{/group-purchases/list(keyword=${searchCondition.keyword}, ingredients=${searchCondition.ingredients}, category=${searchCondition.category?.name()}, status=${searchCondition.status?.name()}, recipeOnly=${searchCondition.recipeOnly}, sortBy='price', direction=(${currentSort == 'price' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${currentSort == 'price'} ? 'fw-bold text-primary' : 'text-muted'">
            가격순
            <i th:if="${currentSort == 'price'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
        <a th:href="@{/group-purchases/list(keyword=${searchCondition.keyword}, ingredients=${searchCondition.ingredients}, category=${searchCondition.category?.name()}, status=${searchCondition.status?.name()}, recipeOnly=${searchCondition.recipeOnly}, sortBy='participants', direction=(${currentSort == 'participants' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
           class="text-decoration-none" th:classappend="${currentSort == 'participants'} ? 'fw-bold text-primary' : 'text-muted'">
            참여자순
            <i th:if="${currentSort == 'participants'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
        </a>
    </div>
    ```

3.  **페이지네이션 링크 확인**: 페이지 이동 시 모든 필터와 정렬 조건이 유지되는지 확인합니다. (기존 코드가 `searchCondition`의 모든 필드를 사용하므로 별도 수정은 필요 없을 것으로 예상됩니다.)

---

## 4. 기대 효과

- **사용자 경험 통일**: 사이트 내 모든 목록(커뮤니티, 레시피, 공동구매)에서 일관된 정렬 인터페이스를 제공합니다.
- **직관성 및 편의성 증대**: 클릭 한 번으로 정렬 기준과 방향을 쉽게 변경할 수 있습니다.
- **UI 로직 단순화**: 복잡한 폼 의존성을 제거하고 단순한 링크 방식으로 UI 로직을 개선합니다.
