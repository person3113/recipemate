# 레시피 필터 및 Lazy Loading 개선 계획

## 1. 분석 요약

### 1.1. 불필요한 레시피 필터

- **현상**: `RecipeController`와 `RecipeService`에 지역(`area`) 및 출처(`source`)를 기준으로 레시피를 필터링하는 로직이 존재하지만, 실제 UI(`recipes/list.html`)에는 해당 필터를 설정하는 입력 필드가 없어 사용되지 않고 있습니다.
- **문제점**: 현재 사용되지 않는 코드가 유지보수성을 저해하고, 페이지네이션 링크 등에 불필요한 파라미터를 포함시켜 URL을 복잡하게 만듭니다.
- **결론**: 코드 간소화 및 명확성 증대를 위해 관련 로직을 제거할 필요가 있습니다.

### 1.2. 랜덤 레시피 페이지 Lazy Loading 미적용

- **현상**: `recipes/list.html`(레시피 목록) 페이지에는 이미지 지연 로딩(Lazy Loading)이 적용되어 초기 로딩 속도를 최적화하고 있으나, `recipes/random.html`(랜덤 레시피) 페이지에는 이 기능이 누락되어 있습니다.
- **문제점**: 랜덤 레시피 추천 개수가 많아질 경우, 모든 이미지를 한 번에 로드하게 되어 페이지 로딩 시간이 길어지고 사용자 경험을 저해할 수 있습니다.
- **결론**: `recipes/random.html`에도 동일한 Lazy Loading 메커니즘을 적용하여 성능 일관성을 확보해야 합니다.

## 2. 개선 계획

### 2.1. 목표

1.  **코드 간소화**: 사용하지 않는 `area`, `source` 필터링 관련 코드를 백엔드와 프론트엔드에서 모두 제거합니다.
2.  **성능 개선**: `recipes/random.html` 페이지에 이미지 Lazy Loading을 적용하여 초기 로딩 성능을 향상시킵니다.

### 2.2. 세부 구현 계획

#### 1단계: 불필요한 필터 로직 전체 제거

1.  **`RecipeController.java` 수정**:
    - `searchRecipesPage` 메서드 시그니처에서 `area`, `source` 및 영양 정보(`maxCalories`, `maxCarbohydrate` 등) 관련 `@RequestParam`을 모두 삭제합니다.
    - `findRecipesByNutrition`을 호출하던 `if` 분기문을 제거하고, 모든 요청이 `findRecipes` 메서드를 호출하도록 로직을 통합합니다.
    - `model.addAttribute`에서 `area`, `source` 및 영양 정보 관련 속성을 추가하는 라인을 삭제합니다.

2.  **`RecipeService.java` 수정**:
    - `findRecipes` 메서드 시그니처에서 `String area`, `RecipeSource sourceApi` 파라미터를 삭제하고, 관련 QueryDSL `BooleanBuilder` 로직을 제거합니다.
    - `findRecipesByNutrition` 메서드 전체를 삭제합니다. (근데 일단 리팩토링에서 제거하진 않았음. 추후 필요 시 제거 예정)

3.  **`recipes/list.html` 수정**:
    - 상단의 필터 표시기(`div.alert`) 영역에서 `area`, `source` 관련 `div` 블록을 삭제합니다.
    - 페이지네이션(`nav`) 링크 생성 시 `th:href`에서 `area`, `source` 및 영양 정보 관련 파라미터를 모두 제거합니다.

#### 2단계: Lazy Loading 스크립트 공통화 및 적용

1.  **`recipes/list.html` 수정**:
    - 하단에 있던 `<script>` 블록(Lazy Loading 스크립트)을 **삭제**합니다. (공통 프래그먼트로 이동 예정)

2.  **`recipes/random.html` 수정**:
    - `<img>` 태그의 `th:src` 속성을 `th:data-src`로 변경하고, `lazy-load` 클래스를 추가합니다.
    - `list.html`과 동일한 SVG 플레이스홀더를 `src` 속성에 추가하여 초기 로딩 시 깨진 이미지가 보이지 않도록 합니다.
        ```html
        <!-- 수정 후 예시 -->
        <img th:if="${recipe.imageUrl != null}"
             th:data-src="${recipe.imageUrl}"
             th:alt="${recipe.name}"
             class="card-img-top recipe-img lazy-load"
             src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 200'%3E%3Crect width='400' height='200' fill='%23f0f0f0'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-family='sans-serif' font-size='16' fill='%23999'%3ELoading...%3C/text%3E%3C/svg%3E">
        ```

3.  **`fragments/footer.html` 수정**:
    - `recipes/list.html`에서 삭제했던 Lazy Loading `<script>` 블록을 `footer`의 다른 스크립트와 함께 추가합니다.
    - 이렇게 하면 `footer`를 사용하는 모든 페이지(list, random 등)에서 Lazy Loading 스크립트가 자동으로 적용됩니다.

## 3. 예상되는 변경 파일

- **수정**:
    - `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`
    - `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`
    - `src/main/resources/templates/recipes/list.html`
    - `src/main/resources/templates/recipes/random.html`
    - `src/main/resources/templates/fragments/footer.html`
- **생성**:
    - `docs/refactor_recipe_filters_and_lazyload.md` (현재 파일)
