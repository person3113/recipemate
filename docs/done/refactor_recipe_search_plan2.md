# 레시피 검색 기능 개선 계획 (v2)

## 1. 기존 문제 분석 (v1 구현 이후)

v1 구현 이후 다음과 같은 문제점들이 발견되었습니다.

1.  **이름순 정렬 오류**: `name`(이름순) 정렬 시, 특정 항목(`Bubble & Squeak`)이 항상 최상단에 노출됩니다. 이는 데이터베이스의 `title` 필드에 `' Bubble & Squeak'`와 같이 눈에 보이지 않는 선행 공백(leading whitespace)이 포함된 채로 저장되었기 때문입니다. 데이터베이스는 정렬 시 공백 문자를 알파벳보다 우선하므로, 해당 항목이 비정상적으로 앞에 위치하게 됩니다.
2.  **단방향 정렬**: 모든 정렬 옵션이 오름차순 또는 내림차순으로 고정되어 있어 사용자가 정렬 순서를 변경할 수 없습니다.
3.  **분리된 검색 로직**: 레시피명 검색과 재료명 검색이 별개의 로직으로 처리되어, 두 조건을 동시에 만족하는 **AND 검색**이 불가능합니다.
4.  **재료 검색 시 필터 미적용**: 재료명으로 검색할 경우, 카테고리 필터가 적용되지 않는 버그가 있습니다.
5.  **필터 조건 표시 미흡**: 검색 결과 페이지 상단에 현재 적용된 필터 조건 중 일부(예: 카테고리)만 표시되어, 사용자가 어떤 조건으로 검색했는지 명확히 알기 어렵습니다.

## 2. 개선 계획 (v2)

### 2.1. 목표

- **정확한 정렬**: 정렬 로직을 수정하여 모든 조건에서 의도한 대로 정확하게 정렬되도록 합니다.
- **양방향 정렬**: 모든 정렬 옵션에 대해 **오름차순/내림차순**을 선택할 수 있도록 기능을 확장합니다.
- **통합 검색**: 레시피명과 재료명 검색을 하나의 로직으로 통합하여, 모든 검색 조건이 **AND**로 처리되도록 합니다.
- **필터링 로직 수정**: 재료 검색 시에도 카테고리 등 다른 필터가 정상적으로 적용되도록 버그를 수정합니다.
- **UI 개선**: 적용된 모든 검색 및 필터 조건을 명확하게 표시하도록 UI를 개선합니다.

### 2.2. 세부 구현 계획

#### 0단계: 데이터 정제 (완료 및 예방 조치)

정렬 문제는 데이터 정제를 통해 해결되었습니다. 향후 동일한 문제의 재발을 방지하기 위해 예방 조치를 코드에 반영합니다.

1.  **완료된 작업: 기존 데이터 마이그레이션**
    - `UPDATE recipe SET title = TRIM(title);` SQL 쿼리를 h2-console에서 직접 실행하여 기존 데이터의 선행 공백을 모두 제거했습니다.
    - **결과**: 이름순 정렬이 정상적으로 동작함을 확인했습니다.

2.  **향후 예방 조치: 데이터 저장 로직 수정**
    - **필요성**: 외부 API 데이터는 언제든지 비정형적인 공백을 포함할 수 있으므로, 데이터를 저장하는 시점에서 항상 정제하는 로직이 필수적입니다.
    - **작업**: `RecipeSyncService`에서 외부 API로부터 레시피 정보를 받아와 `Recipe` 엔티티를 생성하거나 업데이트하는 모든 지점에서, `title` 값을 저장하기 전에 `trim()` 메서드를 적용하여 앞뒤 공백을 제거합니다.
    - **예시**: `recipe.setTitle(meal.getName().trim());`

#### 1단계: 검색 로직 통합 및 필터 버그 수정

1.  **`RecipeController` 수정**:
    - `searchRecipesPage` 메서드 내 분기문(`if (ingredients != null)`)을 제거합니다.
    - `keyword`, `ingredients`, `category` 등 모든 파라미터를 `recipeService.findRecipes` 메서드에 한 번에 전달하도록 변경합니다.

2.  **`RecipeService` 수정**:
    - `findRecipesByIngredients` 메서드를 삭제하고, 해당 로직을 `findRecipes` 메서드로 통합합니다.
    - `findRecipes` 메서드 시그니처를 `findRecipes(String keyword, List<String> ingredients, String category, ...)` 와 같이 변경합니다.
    - **BooleanBuilder** 로직을 수정하여 `keyword`와 `ingredients`가 모두 존재할 경우, 두 조건이 **AND**로 묶이도록 구현합니다.
        - 재료 검색은 `recipe.ingredients`를 조인하여 `recipeIngredient.name.contains(...)` 조건을 추가합니다.
    - 카테고리 필터(`builder.and(recipe.category.eq(category))`)가 재료 검색과 함께 적용될 수 있도록 쿼리 빌더 로직을 수정합니다.

#### 2단계: 양방향 정렬 기능 추가

1.  **`RecipeController` 수정**:
    - `searchRecipesPage` 메서드에 정렬 방향을 결정하는 `direction` 파라미터(기본값: `desc`)를 추가합니다.
        ```java
        @RequestParam(defaultValue = "desc") String direction
        ```
    - `sort`와 `direction` 파라미터를 `Pageable` 객체에 담아 `RecipeService`로 전달합니다. Spring Data의 `PageRequest.of`를 활용합니다.
        ```java
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortOrder;
        if ("name".equals(sort)) {
            sortOrder = Sort.by(sortDirection, "title");
        } else if ("popularity".equals(sort)) {
            // 인기순 정렬은 복잡하므로 서비스 레이어에서 별도 처리
            sortOrder = Sort.unsorted(); // Pageable에는 정렬 없음을 명시
        } else {
            sortOrder = Sort.by(sortDirection, "lastSyncedAt");
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        ```

2.  **`RecipeService` 수정**:
    - `findRecipes` 메서드에서 `Pageable` 객체의 정렬 정보를 사용하도록 변경합니다.
    - **인기순(popularity) 정렬**:
        - 기존의 복잡한 `JPAQuery` 대신, `COUNT` 결과를 DTO나 별도의 표현식을 통해 정렬 키로 사용하도록 개선합니다.
        - `orderBy` 절에 2차 정렬 기준(예: 이름순, 최신순)을 추가하여 `Bubble & Squeak` 문제와 같은 비결정적 순서를 해결합니다.
            ```java
            // 예시: 인기순 > 최신순 > 이름순
            .orderBy(groupBuy.id.count().desc(), recipe.lastSyncedAt.desc(), recipe.title.asc())
            ```
        - `direction` 파라미터에 따라 `desc()` 또는 `asc()`를 동적으로 적용합니다.

3.  **`recipes/list.html` 수정**:
    - 정렬 드롭다운 옆에 오름차순/내림차순을 선택할 수 있는 버튼 또는 아이콘을 추가합니다.
    - 현재 정렬 상태(예: "이름순 (오름차순)")를 명확히 표시합니다.

#### 3단계: 검색 조건 표시 UI 개선

1.  **`RecipeController` 수정**:
    - `searchRecipesPage` 메서드에서 `keyword`, `ingredients`, `category`, `sort`, `direction` 등 모든 검색 관련 파라미터를 `model`에 추가합니다.

2.  **`recipes/list.html` 수정**:
    - 현재 필터 상태를 보여주는 `div.alert` 영역을 수정합니다.
    - `th:if`를 사용하여 `keyword`, `ingredients`, `category` 등 각 조건이 존재할 경우에만 해당 필터 정보를 표시하도록 구현합니다.
        ```html
        <div class="alert alert-primary mb-4" th:if="${keyword != null or ingredients != null or category != null}">
            <!-- 키워드 필터 -->
            <div th:if="${keyword}">
                <i class="bi bi-search me-2"></i>
                키워드: <strong th:text="${keyword}"></strong>
            </div>
            <!-- 재료 필터 -->
            <div th:if="${ingredients}">
                <i class="bi bi-egg-fried me-2"></i>
                재료: <strong th:text="${ingredients}"></strong>
            </div>
            <!-- 카테고리 필터 -->
            <div th:if="${category}">
                <i class="bi bi-tag-fill me-2"></i>
                카테고리: <strong th:text="${category}"></strong>
            </div>
            
            <a href="/recipes" class="btn btn-sm btn-outline-primary ms-3">
                <i class="bi bi-x-circle"></i> 필터 해제
            </a>
        </div>
        ```

## 3. 예상되는 변경 파일

- **수정**:
    - `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`
    - `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`
    - `src/main/resources/templates/recipes/list.html`
- **삭제**:
    - (v1에서 이미 삭제됨) `src/main/resources/templates/recipes/categories.html`
- **수정/대체**:
    - `docs/refactor_recipe_search_plan.md` (현재 파일)

## 4. 기대 효과

- **직관적인 검색 경험**: 사용자가 여러 조건을 조합하여 원하는 레시피를 정확하고 편리하게 찾을 수 있습니다.
- **버그 수정 및 안정성 향상**: 기존의 정렬 및 필터링 오류를 해결하여 서비스의 신뢰도를 높입니다.
- **사용자 피드백 반영**: 사용자가 요청한 정렬 순서 변경 및 필터 조건 표시 기능을 제공하여 만족도를 높입니다.
