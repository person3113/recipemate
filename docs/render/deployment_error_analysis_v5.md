# Render 배포 실패 분석 (log5.md)

## 1. 개요

배포 로그(tmp_log5.md) 분석 결과, 애플리케이션 실행 실패의 원인은 두 가지 주요 PostgreSQL 관련 SQL 문법 오류로 압축됩니다. 이 오류들은 개발 환경(H2 Database)에서는 발생하지 않으나, PostgreSQL의 엄격한 SQL 문법 및 타입 시스템으로 인해 운영 환경(Render)에서 발생합니다.

## 2. 주요 오류 분석

### 오류 1: `GROUP BY` 절 관련 SQL 문법 오류

-   **에러 로그:**
    ```
    org.postgresql.util.PSQLException: ERROR: column "a1_0.id" must appear in the GROUP BY clause or be used in an aggregate function
    ```

-   **오류 발생 위치:**
    -   `com.recipemate.domain.recipe.service.RecipeService.findPopularRecipes(RecipeService.java:178)`

-   **원인:**
    -   PostgreSQL에서는 `GROUP BY`를 사용할 때, `SELECT` 절에 있는 컬럼들은 반드시 `GROUP BY` 절에 포함되거나 집계 함수(e.g., `COUNT`, `SUM`, `MAX`) 내에서 사용되어야 합니다.
    -   현재 `findPopularRecipes` 메소드에서 실행되는 쿼리는 `recipes` 테이블의 `id`(`r1_0.id`)만을 기준으로 그룹화하면서, `SELECT` 절에서는 `users` 테이블(`a1_0`)의 여러 컬럼 등 관련 없는 많은 컬럼을 조회하고 있습니다.
    -   H2 데이터베이스는 이러한 경우 `GROUP BY` 기준이 되는 컬럼이 Primary Key이면 다른 컬럼들이 함수적으로 종속된다고 판단하여 허용하지만, PostgreSQL은 이를 허용하지 않습니다.

-   **분석된 SQL:**
    ```sql
    SELECT r1_0.id, r1_0.area, a1_0.id, a1_0.email, ...
    FROM recipes r1_0
    LEFT JOIN users a1_0 ON a1_0.id = r1_0.user_id
    LEFT JOIN group_buys gb1_0 ON ...
    WHERE (r1_0.deleted_at IS NULL)
    GROUP BY r1_0.id  -- SELECT 절의 수많은 다른 컬럼들이 GROUP BY에 없음
    ORDER BY count(gb1_0.id) DESC, r1_0.last_synced_at DESC
    ```

### 오류 2: PostgreSQL ENUM 타입과 `character varying` 간의 비교 연산자 부재

-   **에러 로그:**
    ```
    org.postgresql.util.PSQLException: ERROR: operator does not exist: group_buy_status = character varying
    Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
    ```

-   **오류 발생 위치:**
    -   `com.recipemate.domain.groupbuy.service.GroupBuyScheduler.updateExpiredGroupBuys(GroupBuyScheduler.java:88)`
    -   `findByStatusInAndDeadlineBefore` 리포지토리 메소드 호출 시

-   **원인:**
    -   `schema.sql`에 따르면 `group_buys` 테이블의 `status` 컬럼은 PostgreSQL의 커스텀 ENUM 타입인 `group_buy_status`로 정의되어 있습니다.
    -   JPA/Hibernate가 쿼리를 생성할 때, `IN (?, ?)` 절에 Java의 `Enum.name()` (즉, `String` 타입)을 파라미터로 전달하고 있습니다.
    -   PostgreSQL은 `group_buy_status` 타입과 `character varying`(String) 타입을 직접 비교하는 연산자(`=`, `IN`)를 가지고 있지 않습니다. 따라서 `?::group_buy_status`와 같이 명시적인 타입 캐스팅이 필요합니다.

-   **분석된 SQL:**
    ```sql
    ... WHERE gb1_0.status IN (?, ?) AND gb1_0.deadline < ?
    ```
    -   여기서 `?`에 `'RECRUITING'`과 같은 문자열이 바인딩되면서 오류가 발생합니다.

## 3. 해결 방안

### `GROUP BY` 오류 해결 방안

1.  **(권장) 쿼리 로직 수정:**
    -   1단계: `GROUP BY`를 사용하여 인기 있는 `recipe_id` 목록만 조회합니다.
    -   2단계: 조회된 `recipe_id` 목록을 사용하여 `recipes`와 `users` 정보를 조회하는 쿼리를 별도로 실행합니다.
    -   이렇게 하면 복잡하고 비효율적인 `GROUP BY`를 피하고, 쿼리 로직이 명확해집니다.

2.  **(임시) `GROUP BY` 절에 모든 컬럼 추가:**
    -   `SELECT` 절에 있는 모든 비-집계 컬럼을 `GROUP BY` 절에 추가합니다. 이는 쿼리를 매우 길고 비효율적으로 만들 수 있어 임시 해결책으로만 권장됩니다.

### ENUM 타입 오류 해결 방안

1.  **`@Enumerated(EnumType.STRING)`와 `hibernate-types` 라이브러리 사용:**
    -   JPA Entity의 ENUM 필드에 `@Enumerated(EnumType.STRING)` 어노테이션이 이미 적용되어 있을 가능성이 높습니다.
    -   PostgreSQL의 네이티브 ENUM과 매끄럽게 연동하려면, `hibernate-types`와 같은 라이브러리를 추가하고 `@Type` 어노테이션을 사용하여 ENUM을 매핑하는 것이 가장 이상적입니다.
        ```java
        // build.gradle에 의존성 추가
        implementation 'io.hypersistence:hypersistence-utils-hibernate-63:3.7.0'

        // GroupBuy.java Entity
        import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
        import org.hibernate.annotations.Type;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, columnDefinition = "group_buy_status")
        @Type(PostgreSQLEnumType.class)
        private GroupBuyStatus status;
        ```

2.  **컬럼 타입 변경:**
    -   DB 스키마의 `status` 컬럼 타입을 `group_buy_status` ENUM 대신 `VARCHAR(255)`로 변경합니다.
    -   이 경우 DB 레벨의 타입 안정성은 포기해야 하지만, 코드 수정은 최소화할 수 있습니다.

## 4. 결론

두 오류 모두 H2와 PostgreSQL 간의 SQL 호환성 차이에서 비롯됩니다. 배포를 성공시키려면 위에서 제시된 해결 방안을 코드에 적용해야 합니다. 특히 `GROUP BY` 문제는 쿼리 로직 재설계가 필요하며, ENUM 문제는 라이브러리를 통한 타입 매핑 강화 또는 DB 스키마 변경을 통해 해결할 수 있습니다.
