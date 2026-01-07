# PostgreSQL ENUM 타입 전면 교체를 위한 분석 및 실행 계획

## 1. 개요

`user_role` 및 `recipe_source_api` 컬럼에서 발생한 배포 오류는 PostgreSQL의 네이티브 ENUM 타입과 JPA/Hibernate 간의 호환성 문제로 인해 발생했습니다. 이는 `schema.sql`에 정의된 모든 네이티브 ENUM 타입에서 재발할 가능성이 매우 높은 잠재적 오류입니다.

본 문서는 이러한 문제를 근본적으로 해결하기 위해, 데이터베이스 스키마에서 모든 네이티브 ENUM 타입을 제거하고 `VARCHAR` 타입으로 표준화하는 실행 계획을 제안합니다.

## 2. 근본 원인

-   **오류 메커니즘**: JPA/Hibernate는 Java의 Enum 값을 데이터베이스에 저장할 때, 기본적으로 Enum의 이름(e.g., `'USER'`)을 `character varying`(String) 타입으로 변환하여 SQL 쿼리를 생성합니다.
-   **PostgreSQL 제약**: PostgreSQL은 `character varying` 타입을 `user_role`과 같은 커스텀 ENUM 타입으로 자동으로 형변환(Casting) 해주지 않으므로, 타입 불일치 오류가 발생합니다.
-   **결론**: 이 문제는 특정 Enum에 국한된 것이 아니라, 현재 아키텍처에서 네이티브 ENUM 타입을 사용하는 모든 곳에서 발생할 수 있는 구조적인 문제입니다.

## 3. 영향을 받는 ENUM 및 컬럼 목록

`schema.sql`을 분석한 결과, 아래의 16개 ENUM 타입과 이를 사용하는 17개 컬럼이 동일한 문제에 노출되어 있습니다.

| ENUM 타입                        | 테이블                  | 컬럼                         |
| -------------------------------- | ----------------------- | ---------------------------- |
| `badge_type`                     | `badges`                | `badge_type`                 |
| `comment_type`                   | `comments`              | `type`                       |
| `group_buy_category`             | `group_buys`            | `category`                   |
| `delivery_method`                | `group_buys`            | `delivery_method`            |
| `delivery_method`                | `participations`        | `selected_delivery_method`   |
| `notification_related_entity_type` | `notifications`         | `related_entity_type`        |
| `notification_type`              | `notifications`         | `type`                       |
| `participation_status`           | `participations`        | `status`                     |
| `point_history_type`             | `point_histories`       | `type`                       |
| `post_category`                  | `posts`                 | `category`                   |
| `recipe_correction_type`         | `recipe_corrections`    | `correction_type`            |
| `recipe_correction_status`       | `recipe_corrections`    | `status`                     |
| `recipe_source_api`              | `recipes`               | `source_api`                 |
| `report_type`                    | `reports`               | `report_type`                |
| `reported_entity_type`           | `reports`               | `reported_entity_type`       |
| `report_status`                  | `reports`               | `status`                     |
| `user_role`                      | `users`                 | `role`                       |

## 4. 해결 방안: VARCHAR 타입으로 표준화

데이터베이스에 독립적인 스키마 구조를 확보하고 장기적인 안정성을 위해, 모든 네이티브 ENUM 타입을 `VARCHAR`로 통일합니다. `group_buy_status` 컬럼이 이미 `VARCHAR`로 변경된 선례를 따르는 방식입니다.

#### 1단계: 운영 DB 마이그레이션 (필수 선행 작업)

> **경고: 이 작업을 수행하기 전 반드시 운영 데이터베이스를 백업하십시오.**
>
> `psql` 등 PostgreSQL 클라이언트를 통해 운영 DB에 직접 접속하여 아래 스크립트를 실행해야 합니다. 스크립트는 모든 변경 사항을 하나의 트랜잭션으로 묶어 안전하게 실행됩니다.

```sql
-- 모든 ENUM 타입 변경을 하나의 트랜잭션으로 묶어 안전하게 실행합니다.
BEGIN;

-- 1. badge_type -> VARCHAR
ALTER TABLE badges ALTER COLUMN badge_type TYPE VARCHAR(50) USING badge_type::text;
DROP TYPE badge_type;

-- 2. comment_type -> VARCHAR
ALTER TABLE comments ALTER COLUMN type TYPE VARCHAR(50) USING type::text;
DROP TYPE comment_type;

-- 3. group_buy_category -> VARCHAR
ALTER TABLE group_buys ALTER COLUMN category TYPE VARCHAR(50) USING category::text;
DROP TYPE group_buy_category;

-- 4. delivery_method -> VARCHAR (group_buys, participations 테이블 모두)
ALTER TABLE group_buys ALTER COLUMN delivery_method TYPE VARCHAR(50) USING delivery_method::text;
ALTER TABLE participations ALTER COLUMN selected_delivery_method TYPE VARCHAR(50) USING selected_delivery_method::text;
DROP TYPE delivery_method;

-- 5. notification_related_entity_type -> VARCHAR
ALTER TABLE notifications ALTER COLUMN related_entity_type TYPE VARCHAR(50) USING related_entity_type::text;
DROP TYPE notification_related_entity_type;

-- 6. notification_type -> VARCHAR
ALTER TABLE notifications ALTER COLUMN type TYPE VARCHAR(50) USING type::text;
DROP TYPE notification_type;

-- 7. participation_status -> VARCHAR
ALTER TABLE participations ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
DROP TYPE participation_status;

-- 8. point_history_type -> VARCHAR
ALTER TABLE point_histories ALTER COLUMN type TYPE VARCHAR(50) USING type::text;
DROP TYPE point_history_type;

-- 9. post_category -> VARCHAR
ALTER TABLE posts ALTER COLUMN category TYPE VARCHAR(50) USING category::text;
DROP TYPE post_category;

-- 10. recipe_correction_type -> VARCHAR
ALTER TABLE recipe_corrections ALTER COLUMN correction_type TYPE VARCHAR(50) USING correction_type::text;
DROP TYPE recipe_correction_type;

-- 11. recipe_correction_status -> VARCHAR
ALTER TABLE recipe_corrections ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
DROP TYPE recipe_correction_status;

-- 12. recipe_source_api -> VARCHAR
ALTER TABLE recipes ALTER COLUMN source_api TYPE VARCHAR(50) USING source_api::text;
DROP TYPE recipe_source_api;

-- 13. report_type -> VARCHAR
ALTER TABLE reports ALTER COLUMN report_type TYPE VARCHAR(50) USING report_type::text;
DROP TYPE report_type;

-- 14. reported_entity_type -> VARCHAR
ALTER TABLE reports ALTER COLUMN reported_entity_type TYPE VARCHAR(50) USING reported_entity_type::text;
DROP TYPE reported_entity_type;

-- 15. report_status -> VARCHAR
ALTER TABLE reports ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
DROP TYPE report_status;

-- 16. user_role -> VARCHAR
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(50) USING role::text;

-- 모든 변경사항을 확정합니다.
COMMIT;
```

#### 2단계: `schema.sql` 파일 수정

마이그레이션 완료 후, 앞으로 생성될 데이터베이스가 올바른 스키마를 갖도록 `schema.sql` 파일을 다음과 같이 수정해야 합니다.

1.  파일 상단의 모든 `CREATE TYPE ... AS ENUM(...)` 구문을 **삭제**합니다.
2.  각 테이블 정의(`CREATE TABLE ...`)에서 위 `3. 영향을 받는 ENUM 및 컬럼 목록` 표에 명시된 모든 컬럼의 데이터 타입을 해당 ENUM 타입에서 `VARCHAR(50)`으로 변경합니다.
    -   대부분 `VARCHAR(50) NOT NULL`로 변경됩니다.
    -   `notifications.related_entity_type` 컬럼과 같이 `NOT NULL` 제약조건이 없는 경우는 `VARCHAR(50)`으로 변경합니다.

## 5. 기대 효과

-   **배포 안정성 확보**: H2(개발), PostgreSQL(운영) 등 어떤 환경에서도 동일하게 동작하여, ENUM 타입으로 인한 배포 실패 문제가 완전히 해결됩니다.
-   **유지보수성 향상**: 스키마가 특정 데이터베이스의 기능에 종속되지 않아 이식성이 높아지고 관리가 용이해집니다.
-   **일관성 있는 구조**: 프로젝트 내 모든 "상태" 또는 "타입" 필드가 `VARCHAR`로 통일되어 코드 및 데이터 구조의 일관성이 향상됩니다.

## 6. 결론

반복되는 배포 실패를 막고 안정적인 서비스를 운영하기 위해, 위 계획에 따라 모든 네이티브 ENUM 타입을 `VARCHAR`로 표준화하는 작업을 조속히 진행할 것을 권장합니다. 이는 장기적으로 더 견고하고 유연한 시스템을 구축하는 기반이 될 것입니다.
