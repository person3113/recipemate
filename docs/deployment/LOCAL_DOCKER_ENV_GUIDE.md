# 로컬 Docker 실행을 위한 환경변수 설정 가이드

이 문서는 로컬 환경에서 Docker Compose를 사용하여 서비스를 실행할 때 필요한 `.env` 파일 설정 방법을 설명합니다.
기존에 자동 생성되었던 설정과 Render(PaaS) 배포 설정을 분석하여, 로컬 Docker 환경에 최적화된 구성을 도출했습니다.

## 1. 분석 내용

### 기존 상황
- **초기 생성 설정:** 로컬 개발에 적합한 단순한 비밀번호(`recipemate2024!secure` 등)를 사용하고 있었습니다.
- **Render 배포 설정:** 실제 운영 환경의 복잡한 비밀번호와 PaaS 특화 설정(`REDIS_HOST=red-...`)이 섞여 있었습니다.

### 로컬 Docker를 위한 전략
로컬 Docker 환경은 **"구조는 운영(Prod)과 같게, 값은 개발(Local)처럼 단순하게"** 구성하는 것이 핵심입니다.

1.  **Spring Profile:** `prod`를 사용합니다.
    - 이유: `dev` 프로필은 H2(파일 DB)를 사용하므로, Docker Compose로 띄운 PostgreSQL/Redis와 연동하려면 `prod` 프로필 설정을 따르는 것이 `docker-compose.yml` 구조와 일치합니다.
2.  **DB/Redis 접속 정보:**
    - `host`는 `docker-compose.yml` 내부의 서비스명(`postgres`, `redis`)을 따릅니다 (자동 매핑됨).
    - `username/password`는 기억하기 쉬운 값으로 통일합니다.
3.  **외부 API 키:**
    - 실제 사용 가능한 키 값을 주입해야 기능이 정상 동작합니다.

---

## 2. `.env` 파일 작성 (복사해서 사용)

프로젝트 최상위 경로(`recipemate-api/`)에 `.env` 파일을 생성하고 아래 내용을 붙여넣으세요.
`xxxxx` 부분은 실제 보유한 Key 값으로 변경해야 합니다.

```properties
# ==========================================
# 1. 애플리케이션 기본 설정
# ==========================================
# Docker Compose 환경에서는 'prod' 프로필을 사용하여
# PostgreSQL 및 Redis 컨테이너와 연결합니다.
SPRING_PROFILES_ACTIVE=prod

# ==========================================
# 2. 데이터베이스 설정 (PostgreSQL)
# ==========================================
# Docker 컨테이너 생성 시 설정될 계정 정보이자,
# Spring Boot가 접속할 계정 정보입니다.
DB_USERNAME=recipemate
DB_PASSWORD=recipemate2024!secure

# ==========================================
# 3. 캐시 설정 (Redis)
# ==========================================
REDIS_PASSWORD=redis2024!secure
# 참고: REDIS_HOST와 REDIS_PORT는 docker-compose.yml에서
# 서비스명('redis')으로 자동 설정되므로 여기서 지정할 필요가 없습니다.

# ==========================================
# 4. 외부 서비스 API 키 (필수 입력)
# ==========================================
# [중요] 실제 작동하는 Key 값으로 교체해주세요.
CLOUDINARY_URL=cloudinary://xxxxx:xxxxx@xxxxx
KAKAO_JAVASCRIPT_KEY=xxxxx
FOOD_SAFETY_API_KEY=xxxxx

# ==========================================
# 5. 레시피 데이터 동기화 옵션 (선택)
# ==========================================
# 앱 실행 시 레시피 데이터 초기화 여부
RECIPE_INIT_ENABLED=false
RECIPE_INIT_FORCE=false

# 주기적 동기화 스케줄러 활성화 여부
RECIPE_SYNC_ENABLED=false
RECIPE_SYNC_CRON="0 0 3 * * SUN"
```

---

## 3. 실행 방법

1.  위 내용을 바탕으로 `.env` 파일을 작성/저장합니다.
2.  Docker Compose를 실행합니다.
    ```bash
    docker compose up -d --build
    ```
3.  로그를 확인하여 정상 실행되었는지 점검합니다.
    ```bash
    docker compose logs -f app
    ```

## 4. 주의사항

- 로컬 Docker 환경의 DB 데이터는 `docker-compose.yml`의 `volumes` 설정에 따라 Docker Volume에 저장되어 컨테이너 재시작 후에도 유지됩니다. 데이터를 완전히 초기화하려면 `docker compose down -v` 명령어를 사용하세요.
