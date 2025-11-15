# 쪽지 기능 알림 추가에 따른 ENUM 제약 조건 오류 분석 및 해결 방안

## 1. 문제 분석

`http://localhost:8080/direct-messages/send/2` API 호출 시 쪽지 전송 후 알림을 생성하는 과정에서 데이터베이스 오류가 발생합니다. 이 문제는 `notifications` 테이블의 `relatedEntityType`과 `type` 두 컬럼이 오래된 ENUM 제약 조건을 가지고 있어 발생합니다.

### 에러 로그 1: `relatedEntityType`
```
org.h2.jdbc.JdbcSQLDataException: Value not permitted for column "('COMMENT', 'GROUP_BUY', 'POST', 'RECIPE', 'REVIEW')": "DIRECT_MESSAGE";
```

### 에러 로그 2: `type`
```
org.h2.jdbc.JdbcSQLDataException: Value not permitted for column "('CANCEL_PARTICIPATION', 'COMMENT_GROUP_BUY', ... 'REVIEW_GROUP_BUY')": "DIRECT_MESSAGE";
```

### 원인
`Notification` 엔티티는 `relatedEntityType`과 `type` 두 개의 ENUM 컬럼을 사용합니다.
- `relatedEntityType`: `EntityType` ENUM 사용
- `type`: `NotificationType` ENUM 사용

자바 코드(`EntityType.java`, `NotificationType.java`)에는 `DIRECT_MESSAGE` 관련 값이 정의되어 있지만, H2 데이터베이스의 `notifications` 테이블 스키마에는 이 값이 반영되지 않은 상태입니다. `ddl-auto: update` 옵션은 ENUM 타입의 정의 변경을 자동으로 반영하지 못하므로 데이터베이스와 애플리케이션 간의 스키마 불일치가 발생합니다.

## 2. 해결 방안

`GROUP_BUYS` 테이블의 `STATUS` 컬럼을 `VARCHAR`로 변경했던 사례와 같이, `notifications` 테이블의 `relatedEntityType`과 `type` 컬럼 타입을 모두 `VARCHAR`로 변경하여 유연성을 확보하고 향후 유사한 문제 발생을 근본적으로 방지합니다.

## 3. 데이터베이스 스키마 마이그레이션 SQL (통합)

**주의:** 아래 SQL 스크립트를 실행하기 전 애플리케이션을 중지해야 합니다.

### 실행 방법

1.  애플리케이션을 중지합니다.
2.  H2 Console에 접속합니다. (http://localhost:8080/h2-console)
    *   **JDBC URL**: `jdbc:h2:file:./data/recipemate;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
    *   **User**: `sa`
    *   **Password**: (공란)
3.  아래 통합 SQL 스크립트를 순서대로 실행합니다.
4.  애플리케이션을 재시작합니다.

### 통합 SQL 스크립트
**참고:** 이전에 첫 번째 스크립트를 이미 실행했더라도, 아래 스크립트는 오류 없이 안전하게 실행되도록 구성되어 있습니다.

```sql
-- =====================================================
-- H2 Database notifications Table ENUM to VARCHAR Migration (Unified)
-- =====================================================
-- Issue: related_entity_type, type 컬럼이 ENUM 타입으로 정의되어 'DIRECT_MESSAGE'를 허용하지 않음
-- Solution: 두 컬럼 모두 ENUM 타입을 VARCHAR로 변경하여 모든 상태값 허용

-- Step 1: 'relatedEntityType' 컬럼 마이그레이션
ALTER TABLE NOTIFICATIONS ADD COLUMN IF NOT EXISTS RELATED_ENTITY_TYPE_TEMP VARCHAR(20);
UPDATE NOTIFICATIONS SET RELATED_ENTITY_TYPE_TEMP = RELATED_ENTITY_TYPE;
ALTER TABLE NOTIFICATIONS DROP COLUMN IF EXISTS RELATED_ENTITY_TYPE;
ALTER TABLE NOTIFICATIONS ADD COLUMN RELATED_ENTITY_TYPE VARCHAR(20);
UPDATE NOTIFICATIONS SET RELATED_ENTITY_TYPE = RELATED_ENTITY_TYPE_TEMP;
ALTER TABLE NOTIFICATIONS DROP COLUMN IF EXISTS RELATED_ENTITY_TYPE_TEMP;

-- Step 2: 'type' 컬럼 마이그레이션
ALTER TABLE NOTIFICATIONS ADD COLUMN IF NOT EXISTS TYPE_TEMP VARCHAR(50);
UPDATE NOTIFICATIONS SET TYPE_TEMP = TYPE;
ALTER TABLE NOTIFICATIONS DROP COLUMN IF EXISTS TYPE;
ALTER TABLE NOTIFICATIONS ADD COLUMN TYPE VARCHAR(50);
UPDATE NOTIFICATIONS SET TYPE = TYPE_TEMP;
ALTER TABLE NOTIFICATIONS DROP COLUMN IF EXISTS TYPE_TEMP;

-- Step 3: 데이터 무결성 확인
SELECT ID, TYPE, RELATED_ENTITY_TYPE FROM NOTIFICATIONS;
```

## 4. 완료 후 조치

1.  H2 Console을 종료합니다.
2.  애플리케이션을 재시작합니다.
3.  쪽지 전송 기능(`http://localhost:8080/direct-messages/send/2`)을 다시 테스트하여 알림이 정상적으로 생성되는지 확인합니다.
```