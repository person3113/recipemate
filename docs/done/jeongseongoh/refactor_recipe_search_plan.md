# 레시피 검색 기능 개선 계획

## 1. 현황 분석

### 1.1. 문제점

- **재료 검색 제한**: 현재 레시피 검색은 레시피명으로만 가능하며, 재료명 검색 기능이 없어 사용자가 원하는 레시피를 찾기 어렵다.
- **불편한 카테고리 필터링**: 카테고리 필터링을 위해 별도의 페이지(`recipes/categories`)로 이동해야 하며, 이는 사용자 경험을 저해한다.
- **정렬 기능 부재**: 검색 결과를 최신순, 인기순, 이름순 등으로 정렬하는 기능이 없어 사용자가 원하는 정보를 효율적으로 탐색하기 어렵다.

### 1.2. 관련 코드 현황

- **`RecipeController.java`**:
    - `GET /recipes`: 레시피 목록 및 검색 결과를 보여주는 `searchRecipesPage` 메서드가 구현되어 있다.
        - `keyword` 파라미터로 레시피명 검색을 지원한다.
        - `category` 파라미터로 카테고리 필터링을 지원하지만, UI가 별도 페이지에 있다.
        - `ingredients` 파라미터로 재료 검색을 지원하지만, 현재 UI에서는 사용되지 않고 있다.
    - `GET /recipes/categories`: 카테고리 목록을 보여주는 `categoriesPage` 메서드가 별도로 존재한다.
- **`RecipeService.java`**:
    - `findRecipes()`: QueryDSL을 사용하여 키워드, 카테고리, 지역, 출처 등으로 레시피를 검색하며, 기본 정렬은 `lastSyncedAt` (최신순)이다.
    - `findRecipesByIngredients()`: 재료명으로 레시피를 검색하는 기능이 이미 구현되어 있으며, 여러 재료에 대해 **OR 조건**으로 검색한다.
    - `getCategories()`: DB에 저장된 모든 레시피의 카테고리 목록을 중복 없이 가져온다.
- **`recipes/list.html`**: 레시피 목록을 표시하는 템플릿. 현재 검색창은 키워드(레시피명)만 입력받도록 되어 있다.
- **`recipes/categories.html`**: 카테고리 목록을 보여주는 별도의 페이지.

## 2. 개선 계획

### 2.1. 목표

- **통합 검색**: 레시피명뿐만 아니라 **재료명**으로도 검색할 수 있도록 검색 기능을 확장한다.
- **UI 개선**: 별도의 카테고리 페이지를 제거하고, 레시피 목록 페이지 내에서 **드롭다운 필터**로 카테고리를 선택할 수 있도록 개선한다.
- **정렬 기능 추가**: 사용자가 **최신순, 인기순, 이름순**으로 검색 결과를 정렬할 수 있는 드롭다운 메뉴를 추가한다.

### 2.2. 세부 구현 계획

#### 1단계: 카테고리 필터링 UI 개선

1.  **`RecipeController` 수정**:
    - `categoriesPage()` 메서드와 `GET /recipes/categories` 매핑을 삭제한다.
    - `searchRecipesPage()` 메서드에서 `recipeService.getCategories()`를 호출하여 카테고리 목록을 가져온 후, 모델에 추가한다.
        ```java
        // RecipeController.java - searchRecipesPage()
        List<CategoryResponse> categories = recipeService.getCategories();
        model.addAttribute("categories", categories);
        ```
2.  **`recipes/list.html` 수정**:
    - 기존의 단순 검색창을 `group-purchases/list.html`의 검색창처럼 변경한다.
    - 카테고리 목록을 드롭다운 형태로 표시하여 사용자가 선택할 수 있도록 한다.
    - 선택된 카테고리는 `category` 파라미터로 `GET /recipes`에 전달되도록 폼을 구성한다.
3.  **`recipes/categories.html` 삭제**:
    - 해당 파일은 더 이상 사용되지 않으므로 삭제한다.

#### 2단계: 정렬 기능 추가
1.  **`RecipeController` 수정**:
    - `searchRecipesPage()` 메서드에 `sort` 파라미터를 추가한다. 기본값은 `latest` (최신순)로 설정한다.

        ```java
        // RecipeController.java - searchRecipesPage()
        @RequestParam(defaultValue = "latest") String sort,
        ```
    - `sort` 파라미터 값을 `RecipeService`로 전달하여 정렬 로직을 위임한다.
2.  **`RecipeService` 수정**:
    - `findRecipes()` 메서드가 `sort` 파라미터를 받도록 수정한다.
    - `sort` 값에 따라 QueryDSL의 `orderBy()` 조건을 동적으로 변경한다.
        - **`latest` (최신순)**: `recipe.lastSyncedAt.desc()` (기존과 동일)
        - **`popularity` (인기순)**: 레시피와 연결된 `GroupBuy` 개수를 기준으로 내림차순 정렬한다.
            - `QGroupBuy`를 사용하여 서브쿼리 또는 조인을 통해 각 레시피의 공구 개수를 계산해야 한다.
            - **주의**: 성능 문제를 고려하여 효율적인 쿼리 작성이 필요하다.
        - **`name` (이름순)**: `recipe.title.asc()`
3.  **`recipes/list.html` 수정**:
    - 정렬 옵션(최신순, 인기순, 이름순)을 선택할 수 있는 드롭다운 메뉴를 추가한다.
    - 선택된 정렬 옵션은 `sort` 파라미터로 `GET /recipes`에 전달되도록 한다.

#### 3단계: 재료명 검색 기능 UI 연동

1.  **`recipes/list.html` 수정**:
    - 검색창에 재료명을 입력할 수 있는 필드를 추가한다.
    - 사용자가 여러 재료를 쉼표(`,`)로 구분하여 입력할 수 있음을 안내하는 placeholder 텍스트를 추가한다.
    - 입력된 재료명은 `ingredients` 파라미터로 `GET /recipes`에 전달되도록 한다.
2.  **`RecipeController` 수정**:
    - `searchRecipesPage()` 메서드에서 `ingredients` 파라미터가 존재할 경우, 기존에 구현된 `recipeService.findRecipesByIngredients()`를 호출하도록 로직을 유지한다. (현재는 OR 조건으로 검색)

---

### 선택적 개선 사항: 재료명 검색 (AND 조건)

- **`RecipeService` 수정**:
    - `findRecipesByIngredients()` 메서드를 개선하여 **AND 조건** 검색을 지원하는 것을 고려할 수 있다.
    - 예를 들어, `searchType=AND` 와 같은 파라미터를 추가하여 `OR` 검색과 `AND` 검색을 전환할 수 있도록 구현할 수 있다.
        - **AND 조건 구현**: `having()` 절과 `countDistinct()`를 사용하거나, 여러 번의 `join`과 `where` 조건을 중첩하여 구현할 수 있다.

## 3. 예상되는 변경 파일

- **수정**:
    - `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`
    - `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`
    - `src/main/resources/templates/recipes/list.html`
- **삭제**:
    - `src/main/resources/templates/recipes/categories.html`
- **생성**:
    - `docs/refactor_recipe_search_plan.md` (현재 파일)

## 4. 기대 효과

- **사용자 편의성 증대**: 사용자가 원하는 레시피를 더 빠르고 쉽게 찾을 수 있게 된다.
- **UI/UX 개선**: 직관적인 인터페이스를 통해 서비스 만족도를 높일 수 있다.
- **기능 확장성 확보**: 향후 더 다양한 검색 조건이나 정렬 기준을 추가하기 용이한 구조가 된다.

## 5. 고려사항 (성능 및 복잡도)

### 5.1. 인기순 정렬 (`popularity`)

- **성능 이슈**: 레시피와 연결된 `GroupBuy`(공동구매) 개수를 기준으로 정렬하려면 `Recipe`와 `GroupBuy` 테이블을 조인(JOIN)하고, 각 레시피별로 `COUNT`를 계산해야 합니다. 데이터가 많아질 경우 이 정렬 연산은 상당한 부하를 유발할 수 있습니다.
- **구현 방안**:
    1.  **단순 구현**: QueryDSL에서 `leftJoin`과 `groupBy`, `count`를 사용하여 구현합니다. 데이터 양이 적을 때는 문제가 없지만, 많아지면 성능 저하가 발생할 수 있습니다.
    2.  **성능 최적화**: `Recipe` 엔티티에 `groupBuyCount` 필드를 추가하고, 공구가 생성/삭제될 때마다 값을 업데이트합니다. 정렬 시 조인 없이 해당 필드를 사용하므로 빠르지만, 데이터 일관성을 위한 추가 로직이 필요하여 복잡도가 증가합니다.
- **제안**: 초기에는 **단순 구현**으로 진행하고, 추후 성능 모니터링을 통해 병목 발생 시 **성능 최적화**를 적용하는 점진적 접근을 제안합니다.

### 5.2. 재료명 검색 (AND 조건)

- **구현 복잡도**: 여러 재료를 **AND 조건**으로 검색(예: '소고기'와 '양파'가 **모두** 포함된 레시피)하는 기능은 **OR 조건**보다 구현이 복잡합니다. QueryDSL에서 `GROUP BY`와 `HAVING COUNT(DISTINCT ingredient_name) = ?` 형태의 쿼리가 필요하며, 이는 일반적인 `WHERE` 조건보다 복잡합니다.
- **성능 이슈**: `LIKE '%재료명%'` 연산은 인덱스를 효율적으로 사용하기 어려워 데이터 증가 시 검색 속도가 느려질 수 있습니다.
- **제안**: 현재 구현된 **OR 조건** 검색을 먼저 UI에 통합하고, **AND 조건**은 추가 기능으로 고려하는 것이 현실적입니다. 장기적으로는 Elasticsearch 같은 전문 검색 엔진 도입을 고려할 수 있으나, 이는 시스템 복잡도를 크게 높입니다.
