# H2 Database 파일 관리 가이드

## 📁 디렉터리 구조

```
recipemate-api/
├── data/                          # H2 DB 파일 저장 위치 (gitignored)
│   ├── recipemate.mv.db           # 메인 DB 파일 (자동 생성)
│   └── recipemate.trace.db        # 로그 파일 (자동 생성, optional)
├── src/
├── build.gradle
└── application.yml
```

## 🚀 H2 파일 생성 방법

### 자동 생성 (권장)

application.yml에 설정되어 있으므로 **별도 작업 불필요**:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/recipemate;MODE=PostgreSQL;...
  jpa:
    hibernate:
      ddl-auto: update  # 테이블 자동 생성
```

**동작 방식:**
1. 애플리케이션 최초 실행 시 `data/` 디렉터리 자동 생성
2. `recipemate.mv.db` 파일 자동 생성
3. JPA 엔티티 기반으로 테이블 자동 생성
4. `RecipeDataInitializer`가 초기 데이터 로드 (설정 활성화 시)

### 수동 생성 (선택)

만약 디렉터리를 미리 만들고 싶다면:

```bash
# Linux/Mac
mkdir -p data

# Windows (PowerShell)
New-Item -ItemType Directory -Path data -Force

# Windows (CMD)
mkdir data
```

## 🔧 H2 Console 접속

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 브라우저에서 H2 Console 접속
```
http://localhost:8080/h2-console
```

### 3. 접속 정보 입력
```
Driver Class: org.h2.Driver
JDBC URL:     jdbc:h2:file:./data/recipemate
User Name:    sa
Password:     (비워두기)
```

### 4. Connect 클릭

## 📊 데이터 확인

H2 Console에서 SQL 실행:

```sql
-- 레시피 개수 확인
SELECT COUNT(*) FROM recipe;

-- 최근 추가된 레시피 10개
SELECT id, title, source_api, last_synced_at 
FROM recipe 
ORDER BY created_at DESC 
LIMIT 10;

-- 소스별 레시피 통계
SELECT source_api, COUNT(*) as count
FROM recipe
GROUP BY source_api;
```

## 🗑️ DB 초기화 (데이터 완전 삭제)

### 방법 1: 파일 삭제 (가장 확실)
```bash
# 애플리케이션 종료 후
rm -rf data/                    # Linux/Mac
rmdir /s /q data                # Windows CMD
Remove-Item -Recurse -Force data  # Windows PowerShell
```

### 방법 2: ddl-auto 변경
```yaml
# application.yml (일시적 변경)
spring:
  jpa:
    hibernate:
      ddl-auto: create  # update → create (DB 재생성)
```
⚠️ **주의**: 운영 환경에서는 절대 사용 금지!

### 방법 3: 강제 재초기화
```bash
# 환경변수로 강제 초기화
RECIPE_INIT_FORCE=true ./gradlew bootRun
```

## 📦 백업 및 복원

### 백업
```bash
# 애플리케이션 종료 후
cp -r data/ data_backup_$(date +%Y%m%d)/  # Linux/Mac
xcopy data data_backup_%date:~0,8% /E /I  # Windows CMD
```

### 복원
```bash
# 애플리케이션 종료 후
rm -rf data/
cp -r data_backup_20250107/ data/
```

### SQL Export (권장)
```sql
-- H2 Console에서 실행
SCRIPT TO 'backup_20250107.sql';

-- 복원 시
RUNSCRIPT FROM 'backup_20250107.sql';
```

## ⚠️ 주의사항

### 1. 동시 접근 불가
H2 file mode는 단일 프로세스만 접근 가능:
- ❌ IntelliJ + H2 Console 동시 접속 불가
- ✅ 애플리케이션 종료 → H2 Console 접속

### 2. 파일 경로 주의
```yaml
# ❌ 절대 경로 (이식성 낮음)
url: jdbc:h2:file:/Users/yourname/data/recipemate

# ✅ 상대 경로 (권장)
url: jdbc:h2:file:./data/recipemate
```

### 3. .gitignore 필수
```gitignore
# .gitignore에 반드시 포함
data/
*.db
*.mv.db
*.trace.db
```

## 🐳 Docker 환경에서 H2 사용

로컬 개발용 Docker Compose:

```yaml
# docker-compose.dev.yml
services:
  app:
    build: .
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      # H2 파일 영속화
      - ./data:/app/data
    ports:
      - "8080:8080"
```

실행:
```bash
docker-compose -f docker-compose.dev.yml --env-file .env.dev up
```

## 🔄 PostgreSQL로 마이그레이션

실제 운영 또는 팀 협업 시:

### 1. Docker PostgreSQL 실행
```bash
docker-compose --env-file .env.dev up postgres
```

### 2. Profile 변경
```bash
# .env.dev 수정
SPRING_PROFILES_ACTIVE=prod

# 또는 실행 시
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

### 3. 데이터 마이그레이션
```sql
-- H2에서 Export
SCRIPT TO 'h2_export.sql';

-- PostgreSQL에서 Import (문법 수정 필요할 수 있음)
psql -U recipemate -d recipemate < h2_export_modified.sql
```

## 📈 성능 팁

### 1. 메모리 설정
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:h2:file:./data/recipemate;MODE=PostgreSQL;CACHE_SIZE=32768
    #                                                   ↑ 32MB 캐시
```

### 2. 인덱스 확인
```sql
-- 인덱스 목록 조회
SELECT * FROM INFORMATION_SCHEMA.INDEXES;
```

### 3. 쿼리 성능 분석
```sql
-- 실행 계획 확인
EXPLAIN SELECT * FROM recipe WHERE title LIKE '%파스타%';
```

## 🆘 문제 해결

### DB 파일이 손상된 경우
```bash
# 1. 백업 복원
cp data_backup/recipemate.mv.db data/

# 2. 백업 없으면 재생성
rm -rf data/
RECIPE_INIT_ENABLED=true ./gradlew bootRun
```

### "Database already closed" 오류
```bash
# 애플리케이션 완전 종료 후 재시작
pkill -f java  # Linux/Mac
taskkill /F /IM java.exe  # Windows
```

### 스키마 변경 후 오류
```bash
# 방법 1: update → create (임시)
# application.yml에서 ddl-auto: create

# 방법 2: Flyway 사용 (권장, 아래 참조)
```

