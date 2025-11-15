-- H2 데이터베이스 ENUM 제약조건 수정 스크립트
-- 실행 방법: http://localhost:8080/h2-console 접속 후 아래 SQL을 순서대로 실행

-- 1. 기존 notifications 테이블의 제약조건 확인
SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE TABLE_NAME = 'NOTIFICATIONS';

-- 2. related_entity_type 컬럼의 CHECK 제약조건 삭제
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS CONSTRAINT_8D;

-- 3. 새로운 CHECK 제약조건 추가 (DIRECT_MESSAGE 포함)
ALTER TABLE notifications
ADD CONSTRAINT check_entity_type
CHECK (related_entity_type IN ('GROUP_BUY', 'POST', 'COMMENT', 'REVIEW', 'RECIPE', 'DIRECT_MESSAGE'));

-- 4. 확인
SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS WHERE TABLE_NAME = 'NOTIFICATIONS' AND CONSTRAINT_TYPE = 'CHECK';

