# 레시피 검색 기능 개선 계획 (v3)

## 1. v2 구현 후 발생한 문제 분석

v2 계획을 구현하는 과정에서 `popularity`(인기순) 정렬과 관련된 두 가지 주요 문제가 발견되었습니다.

### 1.1. SQL 문법 오류 (`SQLGrammarException`)

- **현상**: 레시피명(`keyword`)과 재료명(`ingredients`)을 동시에 검색하면서 `popularity`로 정렬할 경우, 서버에서 500 오류가 발생합니다.
- **오류 로그**: `Order by expression "COUNT(GB1_0.ID)" must be in the result list in this case`
- **원인**: H2 데이터베이스에서 `SELECT DISTINCT`와 `GROUP BY`를 함께 사용할 때, `ORDER BY` 절에 포함된 집계 함수(`COUNT()`)는 반드시 `SELECT` 절의 조회 목록에도 포함되어야 한다는 제약 조건 때문입니다. 현재 QueryDSL 쿼리는 `ingredients` 조건이 추가되면서 `recipe_ingredients` 테이블과 `JOIN`하고 `DISTINCT`를 사용하는데, `COUNT(group_buy.id)`를 `SELECT` 절에 포함하지 않아 SQL 문법 오류가 발생합니다.
- **재현 조건**: `ingredients` 파라미터가 포함된 상태에서 `sort=popularity`로 정렬하는 경우에만 발생합니다.

### 1.2. 인기순 정렬 시 양방향(오름차순/내림차순) 미작동

- **현상**: `sort=popularity`일 때, `direction=asc`와 `direction=desc`의 정렬 결과가 동일하게 내림차순으로만 나옵니다.
- **원인**: `RecipeController`에서 `sort` 값이 `popularity`일 경우, `Sort.unsorted()`로 정렬 정보를 지운 `Pageable` 객체를 생성합니다. 이로 인해 `direction` 파라미터 값이 서비스 계층으로 전달되지 않고, `RecipeService`에서는 `orderBy(groupBuy.id.count().desc(), ...)` 와 같이 내림차순으로 정렬 방향이 하드코딩되어 있기 때문입니다.

## 2. 개선 계획 (v3)

### 2.1. 목표

- **SQL 오류 해결**: `popularity` 정렬 시 발생하는 SQL 문법 오류를 해결하여 어떤 검색 조건에서도 안정적으로 정렬이 동작하도록 합니다.
- **양방향 정렬 기능 완성**: `popularity` 정렬 시에도 오름차순/내림차순이 정상적으로 동작하도록 로직을 수정합니다.

### 2.2. 세부 구현 계획

#### 1단계: `popularity` 정렬 SQL 오류 수정

1.  **`RecipeService` 수정 (`findRecipes` 메서드)**:
    - `popularity` 정렬 로직에서 `select(recipe)`와 `distinct()`를 함께 사용하는 부분을 수정합니다.
    - `select(recipe)`를 유지하되, `distinct()` 키워드를 제거하여 Hibernate가 생성하는 SQL에서 `SELECT DISTINCT`가 빠지도록 유도합니다. `GROUP BY recipe.id`가 이미 레시피별로 그룹화를 수행하므로, `DISTINCT`는 불필요합니다.
    - **수정 전 (의사코드)**:
        ```java
        queryFactory.select(recipe).distinct().from(recipe)...groupBy(recipe.id)...
        ```
    - **수정 후 (의사코드)**:
        ```java
        queryFactory.selectFrom(recipe)...groupBy(recipe.id)...
        // 또는
        queryFactory.select(recipe).from(recipe)...groupBy(recipe.id)...
        ```
        (QueryDSL의 동작 방식에 따라 `selectFrom`으로 변경하거나 `distinct()`만 제거)

#### 2단계: `popularity` 양방향 정렬 기능 구현

1.  **`RecipeController` 수정 (`searchRecipesPage` 메서드)**:
    - `sort` 값이 `popularity`일 때 `Sort.unsorted()`를 사용하던 로직을 제거합니다.
    - 대신, `popularity`를 위한 정렬 정보를 `Pageable`에 포함시키지 않되, `sort`와 `direction` 파라미터를 `RecipeService`로 직접 전달하는 방식을 유지하거나, `Pageable`을 통해 전달하는 방식을 일관되게 사용합니다. 여기서는 **`sort`와 `direction`을 직접 전달하는 현재 방식을 유지**하고 서비스 레이어에서 동적으로 처리하도록 합니다.
    - 컨트롤러에서는 `Pageable`에 기본 정렬(예: ID)만 넘겨주고, 서비스에서 `popularity`일 경우 이를 무시하고 새로운 정렬 기준을 적용하도록 합니다.

2.  **`RecipeService` 수정 (`findRecipes` 메서드)**:
    - `findRecipes` 메서드가 `direction` 파라미터를 받도록 시그니처를 유지합니다.
    - `popularity` 정렬 로직 내에서, `direction` 파라미터 값에 따라 `OrderSpecifier`를 동적으로 생성합니다.
        ```java
        // in findRecipes method
        JPAQuery<Recipe> query = ...;

        if ("popularity".equals(sort)) {
            // ... join 로직 ...
            
            // 동적 OrderSpecifier 생성
            OrderSpecifier<?> popularityOrder = "asc".equalsIgnoreCase(direction)
                ? groupBuy.id.count().asc()
                : groupBuy.id.count().desc();

            query.orderBy(popularityOrder, recipe.lastSyncedAt.desc()); // 2차 정렬 기준 유지
        } else {
            // Pageable을 이용한 다른 정렬 처리
        }
        ```
    - 이렇게 하면 `direction` 값에 따라 `COUNT(group_buy.id)`에 대한 `ASC` 또는 `DESC`가 동적으로 적용됩니다.

## 3. 예상되는 변경 파일

- **수정**:
    - `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`
    - `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java` (필요 시)
- **수정/대체**:
    - `docs/refactor_recipe_search_plan3.md` (현재 파일)

## 4. 기대 효과

- **서비스 안정성 확보**: 어떤 검색 조건과 정렬 조건을 조합하더라도 서버 오류 없이 안정적인 검색 결과를 제공합니다.
- **사용자 경험 개선**: 인기순 정렬 시에도 오름차순/내림차순을 사용자가 의도한 대로 사용할 수 있게 되어 기능적 완전성이 높아집니다.
