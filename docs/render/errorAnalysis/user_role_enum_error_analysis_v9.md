# Render 배포 실패 분석 (log9.md) - User Role ENUM 타입 오류

## 1. 개요

배포 로그(tmp_log9.md) 분석 결과, 회원가입 실패의 원인은 PostgreSQL의 ENUM 타입과 관련된 SQL 문법 오류 때문입니다. 이 오류는 `users` 테이블의 `role` 컬럼에 데이터를 삽입할 때 발생하며, 개발 환경(H2)에서는 나타나지 않으나 PostgreSQL의 엄격한 타입 시스템으로 인해 운영 환경에서 발생합니다.

이는 `deployment_error_analysis_v5.md`에서 분석된 `group_buy_status` ENUM 오류와 근본적으로 동일한 문제입니다.

## 2. 주요 오류 분석

-   **에러 로그:**
    ```
    org.postgresql.util.PSQLException: ERROR: column "role" is of type user_role but expression is of type character varying
    Hint: You will need to rewrite or cast the expression.
    ```

-   **오류 발생 위치:**
    -   회원가입 API 호출 시, `UserRepository`를 통해 `users` 테이블에 새로운 레코드를 `INSERT` 하는 과정에서 발생합니다.

-   **원인:**
    -   `schema.sql`에 정의된 바와 같이, `users` 테이블의 `role` 컬럼은 `user_role`이라는 커스텀 ENUM 타입입니다.
        ```sql
        CREATE TYPE user_role AS ENUM('ADMIN', 'USER');

        CREATE TABLE users(
            ...
            role user_role NOT NULL
        );
        ```
    -   JPA/Hibernate는 `User` 엔티티의 `role` 필드(`UserRole` Enum)를 데이터베이스에 저장할 때, 기본적으로 ENUM의 이름인 `String` 타입(`'USER'` 또는 `'ADMIN'`)으로 변환하여 SQL 쿼리를 생성합니다.
    -   PostgreSQL은 `character varying`(String) 타입을 `user_role` ENUM 타입으로 자동으로 캐스팅(형변환)하지 않으므로, `role` 컬럼에 값을 직접 삽입할 수 없어 오류가 발생합니다.

## 3. 해결 방안

데이터베이스에 종속적인 네이티브 ENUM 기능을 제거하고, **어떤 데이터베이스와도 호환되는 표준 `VARCHAR` 타입으로 스키마를 통일**하는 방식입니다.

-   **장점**:
    -   데이터베이스 비종속성 확보 (H2, PostgreSQL 등에서 모두 동작)
    -   장기적인 유지보수성 및 이식성 향상
    -   추가 라이브러리 의존성 불필요
-   **단점**:
    -   운영 DB에 직접 마이그레이션 스크립트를 실행해야 하는 번거로움이 있습니다.

#### 1단계: 운영 DB 마이그레이션 (필수 선행 작업)

> **경고: 이 작업을 수행하기 전 반드시 데이터베이스를 백업하십시오.**
>
> `psql` 등을 통해 운영 DB에 직접 접속하여 아래 스크립트를 실행해야 합니다.

```sql
-- 하나의 트랜잭션으로 묶어 안전하게 실행합니다.
BEGIN;

-- 1. role 컬럼의 타입을 VARCHAR(20)으로 변경합니다.
--    USING 절을 통해 기존 ENUM 값을 데이터 손실 없이 텍스트로 변환합니다.
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20) USING role::text;

-- 2. 더 이상 사용되지 않는 네이티브 ENUM 타입을 삭제합니다.
DROP TYPE user_role;

-- 트랜잭션을 완료합니다.
COMMIT;
```

#### 2단계: `schema.sql` 파일 수정

앞으로 생성될 DB에 동일한 스키마가 적용되도록 `schema.sql` 파일을 수정합니다.

-   **수정 내용**:
    1.  `CREATE TYPE user_role AS ENUM(...)` 구문을 삭제합니다.
    2.  `users` 테이블 정의에서 `role` 컬럼 타입을 `user_role`에서 `VARCHAR(20) NOT NULL`으로 변경합니다.


## 4. 결론

회원가입 실패는 H2와 PostgreSQL 간의 기능 차이 및 JPA의 ENUM 처리 방식 때문에 발생한 문제입니다. 임시 방편으로 라이브러리를 사용할 수도 있지만, 장기적인 안정성과 호환성을 위해 **DB 스키마를 `VARCHAR`로 표준화(방안 2)**하는 것이 가장 이상적인 해결책입니다. 이 조치를 통해 환경에 구애받지 않는 안정적인 애플리케이션을 구축할 수 있습니다.
