# Spring Boot 실행 오류 분석 및 최종 해결 방안 (v3)

## 1. 최초 분석 및 문제점

최초 애플리케이션 실행 오류는 `dev` 프로필에서 사용하는 H2 데이터베이스가 `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` 어노테이션을 지원하지 않아 발생했습니다.

이에 따라 해당 어노테이션을 제거하는 해결책을 제안했으나, 이는 운영 환경(PostgreSQL)에서 `ERROR: operator does not exist: group_buy_status = character varying` 오류를 재발시키는 불완전한 해결책이었습니다.

## 2. 근본 원인: 데이터베이스 스키마와 코드의 비호환성

`schema.sql` 파일을 통해 확인한 결과, 운영 DB 스키마는 PostgreSQL 전용 네이티브 ENUM 타입인 `group_buy_status`를 사용하고 있습니다. 이것이 바로 로컬(H2)과 운영(PostgreSQL) 환경 간 호환성 문제의 근본 원인입니다.

## 3. 최종 해결 방안: DB 스키마 표준화 및 마이그레이션

가장 확실한 해결책은 특정 데이터베이스에 종속적인 기능을 제거하여, **애플리케이션이 어떤 데이터베이스와도 잘 동작하도록 스키마를 표준화**하는 것입니다. 이를 위해 코드 수정과 함께, 이미 운영 중인 데이터베이스에 대한 마이그레이션 작업이 필요합니다.

### 1단계: 기존 운영 DB 마이그레이션 (필수 선행 작업)

> **경고: 이 작업을 수행하기 전 반드시 데이터베이스를 백업하십시오.**

`schema.sql` 변경만으로는 이미 생성된 DB가 바뀌지 않으므로, `psql` 등을 통해 운영 DB에 직접 접속하여 아래의 마이그레이션 스크립트를 실행해야 합니다.

```sql
-- 하나의 트랜잭션으로 묶어 안전하게 실행합니다.
BEGIN;

-- 1. status 컬럼의 타입을 VARCHAR(20)으로 변경합니다.
--    USING 절을 통해 기존 ENUM 값을 데이터 손실 없이 텍스트로 변환합니다.
ALTER TABLE group_buys ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- 2. 더 이상 사용되지 않는 네이티브 ENUM 타입을 삭제합니다.
DROP TYPE group_buy_status;

-- 트랜잭션을 완료합니다.
COMMIT;
```

### 2단계: `schema.sql` 파일 수정

운영 DB 마이그레이션이 완료되었다면, 앞으로 새로 생성될 DB에 동일한 스키마가 적용되도록 `schema.sql` 파일을 수정합니다.

-   **수정 내용**:
    1.  `CREATE TYPE group_buy_status AS ENUM(...)` 구문을 삭제합니다.
    2.  `group_buys` 테이블의 `status` 컬럼 타입을 `group_buy_status`에서 `VARCHAR(20)`으로 변경합니다.

### 3단계: `GroupBuy.java` 엔티티 수정

마지막으로, 표준화된 스키마에 맞게 엔티티 코드를 수정합니다.

-   **수정 내용**: `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` 어노테이션을 삭제하고, 표준 JPA 기능인 `@Enumerated(EnumType.STRING)`만 남겨둡니다.
-   **수정 후 최종 코드**:
    ```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupBuyStatus status = GroupBuyStatus.RECRUITING;
    ```

### 기대 효과

위 3단계를 모두 완료하면, 운영 데이터베이스와 애플리케이션 코드가 모두 표준 `VARCHAR`-`Enum` 매핑을 사용하도록 통일됩니다. 이를 통해 로컬(H2)의 시동 오류와 운영(PostgreSQL)의 쿼리 오류가 모두 해결되며, 향후 데이터베이스 유지보수 및 이식성이 크게 향상됩니다.
