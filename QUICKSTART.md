# RecipeMate Docker 실행 가이드

## 🚀 빠른 시작

### 1단계: Docker Desktop 실행 확인

Docker Desktop이 실행 중인지 확인하세요:
- Windows 작업 표시줄에서 Docker 아이콘 확인
- 아이콘이 움직이고 있으면 → 시작 중 (1-2분 대기)
- 아이콘이 고정되어 있으면 → 준비 완료

**확인 명령어:**
```bash
docker info
```

출력이 나오면 Docker가 준비된 것입니다.

---

### 3단계: Docker 컨테이너 실행

```bash
# 프로젝트 디렉토리로 이동
cd C:\Users\UESR\Desktop\uni_project\recipemate-api

# 컨테이너 빌드 및 실행 (5-10분 소요)
docker-compose up -d --build
```

**실행 과정:**
1. PostgreSQL 16 이미지 다운로드 (약 100MB)
2. Redis 7 이미지 다운로드 (약 40MB)
3. Nginx 이미지 다운로드 (약 40MB)
4. Spring Boot 애플리케이션 빌드 (Gradle 의존성 다운로드 + 컴파일, 3-5분)
5. 모든 컨테이너 시작

---

### 4단계: 서비스 상태 확인

```bash
# 모든 컨테이너 상태 확인
docker-compose ps
```

**정상 출력 예시:**
```
NAME                    STATUS              PORTS
recipemate-postgres     Up (healthy)        0.0.0.0:5432->5432/tcp
recipemate-redis        Up (healthy)        0.0.0.0:6379->6379/tcp
recipemate-app          Up (healthy)        0.0.0.0:8080->8080/tcp
recipemate-nginx        Up (healthy)        0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp
```

**모두 "Up (healthy)" 상태여야 합니다.**

---

### 5단계: 로그 확인

```bash
# 애플리케이션 로그 실시간 확인
docker-compose logs -f app

# 특정 서비스 로그
docker-compose logs postgres
docker-compose logs redis
docker-compose logs nginx

# 모든 로그
docker-compose logs -f
```

**정상 시작 확인:**
- `Started RecipeMateApplication in X seconds` 메시지 확인
- 에러 없이 Spring Boot 배너 출력

---

### 6단계: 데이터베이스 스키마 적용

```bash
# schema.sql 실행 (테이블 생성)
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < schema.sql
```

**성공 메시지:**
```
CREATE TYPE
CREATE TYPE
...
CREATE TABLE
CREATE TABLE
...
CREATE INDEX
...
INSERT 0 1
```

**스키마 확인:**
```bash
docker exec -it recipemate-postgres psql -U recipemate -d recipemate -c "\dt"
```

**출력 예시 (13개 테이블):**
```
               List of relations
 Schema |       Name        | Type  |   Owner
--------+-------------------+-------+-----------
 public | badges            | table | recipemate
 public | comments          | table | recipemate
 public | group_buy_images  | table | recipemate
 public | group_buys        | table | recipemate
 public | notifications     | table | recipemate
 public | participations    | table | recipemate
 public | persistent_logins | table | recipemate
 public | point_histories   | table | recipemate
 public | post_images       | table | recipemate
 public | posts             | table | recipemate
 public | reviews           | table | recipemate
 public | users             | table | recipemate
 public | wishlists         | table | recipemate
```

---

### 7단계: 애플리케이션 접속

**브라우저에서 접속:**

1. **Nginx 프록시 경유 (권장):**
   ```
   http://localhost
   ```

2. **직접 접속:**
   ```
   http://localhost:8080
   ```

3. **헬스체크 엔드포인트:**
   ```
   http://localhost:8080/actuator/health
   ```
   - 정상: `{"status":"UP"}`

4. **로그인 페이지:**
   ```
   http://localhost/auth/login
   ```

5. **H2 콘솔 (개발 프로파일에서만):**
   ```
   http://localhost:8080/h2-console
   ```

---

## 🔧 문제 해결

### 문제 1: "bind: address already in use"

**원인:** 포트가 이미 사용 중

**해결:**
```bash
# 포트 사용 중인 프로세스 확인
netstat -ano | findstr :8080
netstat -ano | findstr :5432
netstat -ano | findstr :6379
netstat -ano | findstr :80

# 프로세스 종료 (PID 확인 후)
taskkill /PID [PID번호] /F

# 또는 다른 포트 사용 (docker-compose.yml 수정)
ports:
  - "8081:8080"  # 8080 대신 8081 사용
```

---

### 문제 2: "no configuration file provided"

**원인:** .env 파일이 없음

**해결:**
```bash
copy .env.example .env
```

---

### 문제 3: Spring Boot 빌드 실패

**원인:** Gradle 의존성 다운로드 실패 또는 컴파일 오류

**해결:**
```bash
# 로컬에서 먼저 빌드 테스트
gradlew.bat clean build

# 성공하면 Docker 재빌드
docker-compose up -d --build app
```

---

### 문제 4: PostgreSQL 연결 실패

**원인:** 데이터베이스가 아직 준비되지 않음

**해결:**
```bash
# PostgreSQL 헬스체크 확인
docker exec recipemate-postgres pg_isready -U recipemate

# 로그 확인
docker-compose logs postgres

# 재시작
docker-compose restart postgres
docker-compose restart app
```

---

### 문제 5: Redis 연결 실패

**원인:** Redis 비밀번호 불일치

**해결:**
```bash
# Redis 연결 테스트
docker exec recipemate-redis redis-cli -a recipemate2024!secure ping

# PONG 응답이 와야 함

# 비밀번호 확인
type .env | findstr REDIS_PASSWORD
```

---

## 🛑 컨테이너 중지 및 삭제

### 컨테이너 중지
```bash
docker-compose stop
```

### 컨테이너 중지 및 삭제
```bash
docker-compose down
```

### 컨테이너 + 볼륨 삭제 (⚠️ 데이터 삭제됨!)
```bash
docker-compose down -v
```

### 이미지까지 삭제
```bash
docker-compose down --rmi all -v
```

---

## 📊 유용한 명령어

### 컨테이너 상태 모니터링
```bash
# 실시간 리소스 사용량
docker stats

# 특정 컨테이너만
docker stats recipemate-app
```

### 컨테이너 내부 접속
```bash
# PostgreSQL 쉘
docker exec -it recipemate-postgres psql -U recipemate -d recipemate

# Redis CLI
docker exec -it recipemate-redis redis-cli -a recipemate2024!secure

# 애플리케이션 컨테이너 Bash
docker exec -it recipemate-app bash

# Nginx 컨테이너
docker exec -it recipemate-nginx sh
```

### 데이터베이스 작업
```bash
# 백업
docker exec recipemate-postgres pg_dump -U recipemate recipemate > backup_$(date +%Y%m%d).sql

# 복원
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < backup_20250105.sql

# 테이블 목록
docker exec recipemate-postgres psql -U recipemate -d recipemate -c "\dt"

# 사용자 목록
docker exec recipemate-postgres psql -U recipemate -d recipemate -c "SELECT id, username, email, nickname FROM users;"
```

### Redis 캐시 관리
```bash
# 모든 키 조회
docker exec recipemate-redis redis-cli -a recipemate2024!secure KEYS "*"

# 특정 키 값 조회
docker exec recipemate-redis redis-cli -a recipemate2024!secure GET "recipes::search::chicken"

# 캐시 통계
docker exec recipemate-redis redis-cli -a recipemate2024!secure INFO stats

# 전체 캐시 삭제
docker exec recipemate-redis redis-cli -a recipemate2024!secure FLUSHALL
```

---

## 🎯 초기 데이터 삽입

### 관리자 계정 (이미 schema.sql에 포함)
```
Username: admin
Password: admin123
Email: admin@recipemate.com
Role: ADMIN
```

### 추가 테스트 데이터 삽입
```bash
# SQL 파일 실행
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < test-data.sql

# 또는 직접 SQL 실행
docker exec recipemate-postgres psql -U recipemate -d recipemate -c "
INSERT INTO users (username, password, email, nickname, role, points, is_active)
VALUES ('testuser', '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EH', 'test@test.com', '테스트유저', 'USER', 100, true);
"
```

---

## 📝 다음 단계

1. ✅ Docker 컨테이너 실행
2. ✅ 데이터베이스 스키마 적용
3. ⬜ 브라우저에서 접속 테스트
4. ⬜ 회원가입 / 로그인 테스트
5. ⬜ 공동구매 생성 테스트
6. ⬜ 레시피 검색 테스트
7. ⬜ E2E 시나리오 테스트

---

## 📚 참고 문서

- Docker 설정: `DOCKER.md`
- 데이터베이스 스키마: `schema.sql`
- 환경변수 템플릿: `.env.example`
- 개발 작업 현황: `docs/DEVELOPMENT_TASKS.md`
