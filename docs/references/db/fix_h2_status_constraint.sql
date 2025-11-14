-- =====================================================
-- H2 Database GroupBuyStatus ENUM to VARCHAR Migration
-- =====================================================
-- Issue: STATUS 컬럼이 ENUM 타입으로 정의되어 있어서 ('CLOSED', 'IMMINENT', 'RECRUITING')만 허용
--        COMPLETED, CANCELLED 상태를 사용할 수 없는 문제 발생
-- Solution: ENUM 타입을 VARCHAR(20)으로 변경하여 모든 상태값 허용

-- =====================================================
-- 실행 방법
-- =====================================================
-- 1. 애플리케이션 중지
-- 2. H2 Console 접속: http://localhost:8080/h2-console
--    - JDBC URL: jdbc:h2:file:./data/recipemate;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
--    - User: sa
--    - Password: (비워두기)
-- 3. 아래 SQL을 순서대로 실행
-- 4. 애플리케이션 재시작

-- =====================================================
-- Step 1: 임시 컬럼 생성 및 데이터 백업
-- =====================================================
ALTER TABLE GROUP_BUYS ADD COLUMN STATUS_TEMP VARCHAR(20);
UPDATE GROUP_BUYS SET STATUS_TEMP = STATUS;

-- =====================================================
-- Step 2: 관련 인덱스 삭제
-- =====================================================
-- STATUS 컬럼을 참조하는 인덱스를 먼저 삭제해야 컬럼 삭제 가능
DROP INDEX IF EXISTS IDX_GROUPBUY_STATUS_DEADLINE;
DROP INDEX IF EXISTS IDX_GROUPBUY_STATUS_DELETED;
DROP INDEX IF EXISTS IDX_GROUPBUY_STATUS;

-- =====================================================
-- Step 3: 기존 ENUM 컬럼 삭제
-- =====================================================
ALTER TABLE GROUP_BUYS DROP COLUMN STATUS;

-- =====================================================
-- Step 4: 새 VARCHAR 컬럼 생성
-- =====================================================
ALTER TABLE GROUP_BUYS ADD COLUMN STATUS VARCHAR(20) NOT NULL DEFAULT 'RECRUITING';

-- =====================================================
-- Step 5: 백업 데이터 복원
-- =====================================================
UPDATE GROUP_BUYS SET STATUS = STATUS_TEMP;

-- =====================================================
-- Step 6: 인덱스 재생성
-- =====================================================
CREATE INDEX IDX_GROUPBUY_STATUS_DEADLINE ON GROUP_BUYS(STATUS, DEADLINE);
CREATE INDEX IDX_GROUPBUY_STATUS_DELETED ON GROUP_BUYS(STATUS, DELETED_AT);

-- =====================================================
-- Step 7: 임시 컬럼 삭제
-- =====================================================
ALTER TABLE GROUP_BUYS DROP COLUMN STATUS_TEMP;

-- =====================================================
-- Step 8: 데이터 무결성 확인
-- =====================================================
-- 기존 데이터가 정상적으로 복원되었는지 확인
SELECT ID, TITLE, STATUS FROM GROUP_BUYS;

-- 레시피 데이터가 그대로 있는지 확인
SELECT COUNT(*) AS RECIPE_COUNT FROM RECIPES;

-- =====================================================
-- Step 9: 새로운 상태값 테스트
-- =====================================================
-- COMPLETED 상태 업데이트 테스트 (테스트 후 원래 상태로 복원)
-- UPDATE GROUP_BUYS SET STATUS = 'COMPLETED' WHERE ID = 1;
-- SELECT ID, TITLE, STATUS FROM GROUP_BUYS WHERE ID = 1;
-- UPDATE GROUP_BUYS SET STATUS = 'CLOSED' WHERE ID = 1;  -- 원래 상태로 복원

-- =====================================================
-- 완료 후 조치
-- =====================================================
-- 1. H2 Console 종료
-- 2. 애플리케이션 재시작
-- 3. 새로운 공구 생성 및 상태 변경 테스트
--    - RECRUITING → IMMINENT → CLOSED → COMPLETED
--    - RECRUITING → CANCELLED
-- 4. 후기 작성 기능 테스트 (COMPLETED 상태에서)

-- =====================================================
-- 예상 결과
-- =====================================================
-- ✅ ENUM 제약 제거: COMPLETED, CANCELLED 상태 사용 가능
-- ✅ 기존 데이터 보존: RECRUITING, IMMINENT, CLOSED 상태 유지
-- ✅ 인덱스 유지: 검색 성능 유지
-- ✅ 레시피 데이터 보존: 식품안전나라 API 제한으로 재생성 불가한 데이터 안전

-- =====================================================
-- 트러블슈팅
-- =====================================================
-- 문제: "Column may be referenced" 에러 발생
-- 해결: 모든 인덱스를 먼저 삭제한 후 컬럼 삭제
--
-- 문제: 데이터가 사라짐
-- 해결: STATUS_TEMP 컬럼에 백업되어 있으므로 Step 5 재실행
--
-- 문제: 애플리케이션 시작 시 스키마 충돌
-- 해결: application.yml에서 ddl-auto: update 확인 (validate가 아닌지 확인)
