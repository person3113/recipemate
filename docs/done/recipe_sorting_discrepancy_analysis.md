# 레시피 정렬 불일치 분석

## 1. 문제 설명

- **증상:** 레시피 목록을 "인기순"(연결된 공구 수)으로 정렬할 때, 로컬 환경(H2 데이터베이스)에서는 정상적으로 정렬되지만 프로덕션 환경(PostgreSQL 데이터베이스)에서는 순서가 무작위로 보입니다.
- **목표:** 레시피는 연결된 공구 수에 따라 내림차순으로 정렬되어야 합니다.

## 2. 근본 원인 분석

`codebase_investigator`를 통한 조사 결과, 문제는 `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`의 `findRecipes` 메서드 내에 있는 것으로 확인되었습니다.

"인기순" 정렬을 위해 동적으로 생성되는 QueryDSL 쿼리는 다음과 같습니다:

```java
// RecipeService.findRecipes 내부
// ...
query.select(recipe)
     .from(recipe)
     .leftJoin(groupBuy).on(groupBuy.recipe.id.eq(recipe.id))
     .where(builder)
     .groupBy(recipe.id) // <- 문제의 라인
     .orderBy(groupBuy.id.count().desc())
// ...
```

이 쿼리는 H2에서는 유효한 SQL을 생성하지만, 더 엄격한 PostgreSQL의 `GROUP BY` 규칙을 위반합니다.

- **H2 동작:** H2는 `GROUP BY` 절에 포함되지 않은 컬럼(`SELECT recipe.*`)을 선택하는 것을 허용합니다. 단, `GROUP BY`의 기준이 되는 컬럼(`recipe.id`)이 기본 키(Primary Key)여야 합니다. 이 경우 H2는 `recipe` 테이블의 다른 모든 컬럼이 기본 키에 함수적으로 종속된다고 암묵적으로 가정합니다.
- **PostgreSQL 동작:** PostgreSQL은 `SELECT` 목록에 있는 모든 비집계(non-aggregated) 컬럼이 `GROUP BY` 절에도 포함되어야 한다는 규칙을 강제합니다. `SELECT recipe`는 `Recipe` 엔티티의 모든 컬럼을 선택하는데, `recipe.id`로만 그룹화를 하고 있기 때문에 PostgreSQL은 오류를 발생시킵니다. 애플리케이션이 이 오류를 처리하여 충돌하지 않더라도, 쿼리는 의도대로 실행되지 않아 잘못된 정렬로 이어집니다.

같은 파일에 있는 `findPopularRecipes` 메서드는 유사한 시나리오를 더 안정적인 2단계 쿼리 접근 방식으로 올바르게 처리하고 있습니다.

## 3. 제안 해결책

모든 데이터베이스 환경에서 일관된 정렬을 보장하기 위해, `findRecipes` 메서드 내의 "인기순" 정렬 로직을 `findPopularRecipes`에서 사용된 2단계 쿼리 패턴을 채택하도록 리팩터링해야 합니다.

### 단계별 리팩터링 계획

1.  **인기순 정렬 로직 분리:** `findRecipes` 메서드에서 `sort` 파라미터가 `"popularity"`와 같은지 확인합니다.

2.  **1단계: 정렬된 레시피 ID 목록 조회:**
    인기순으로 정렬하는 경우, 먼저 전용 쿼리를 실행합니다. 이 쿼리는 `Recipe`와 `GroupBuy`를 조인하고 `recipe.id`로 그룹화한 뒤, 공구 개수로 정렬하여 정렬된 레시피 ID 목록만 가져옵니다.

    ```java
    // 첫 번째 쿼리 의사코드
    List<Long> recipeIds = jpaQueryFactory
        .select(recipe.id)
        .from(recipe)
        .leftJoin(groupBuy).on(groupBuy.recipe.id.eq(recipe.id))
        .where(builder) // 동일한 필터 적용
        .groupBy(recipe.id)
        .orderBy(groupBuy.id.count().desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
    ```

3.  **2단계: 레시피 엔티티 목록 조회:**
    정렬된 `recipeIds` 목록을 사용하여 두 번째 쿼리를 실행해 전체 `Recipe` 엔티티를 가져옵니다. 이때 첫 번째 단계에서 얻은 순서를 유지하는 것이 중요합니다.

    ```java
    // 두 번째 쿼리 의사코드
    List<Recipe> recipes = jpaQueryFactory
        .selectFrom(recipe)
        .where(recipe.id.in(recipeIds))
        .fetch();

    // ID 목록 기반으로 조회하면 순서가 보장되지 않을 수 있으므로, 재정렬이 필요합니다.
    // 빠른 조회를 위해 Map을 생성하고, ID 목록 순서대로 최종 리스트를 만듭니다.
    Map<Long, Recipe> recipeMap = recipes.stream()
            .collect(Collectors.toMap(Recipe::getId, r -> r));

    List<Recipe> sortedRecipes = recipeIds.stream()
            .map(recipeMap::get)
            .collect(Collectors.toList());
    ```

4.  **빈 결과 처리:** `recipeIds` 목록이 비어있을 경우, 불필요한 쿼리를 피하기 위해 빈 `Page` 객체를 반환합니다.

5.  **기존 로직과 통합:** `findRecipes` 메서드의 나머지 부분(다른 정렬 옵션)은 그대로 유지합니다. 이 변경 사항은 "인기순" 정렬 로직 분기 내에서만 처리되어야 합니다.

이 접근 방식은 집계 및 정렬 로직을 최종 엔티티 선택과 분리하여 SQL 비호환성 문제를 해결합니다. 결과적으로 PostgreSQL과 H2 모두에서 정확하고 성능이 좋은 쿼리를 실행할 수 있습니다.
