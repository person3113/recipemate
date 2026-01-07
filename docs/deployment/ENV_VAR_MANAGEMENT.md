# 환경변수 관리 및 흐름 가이드 (Environment Variable Management)

이 문서는 Spring Boot 애플리케이션이 Local(Docker), AWS, Render 등 다양한 환경에서 동일한 코드로 동작하기 위해 환경변수를 어떻게 관리해야 하는지 설명합니다.

---

## 1. 핵심 원칙: "키(Key)는 코드에, 값(Value)은 환경에"

Spring Boot의 `application.yml`에는 구체적인 비밀번호나 IP 주소를 적지 않고, 변수명(Key) 만 명시합니다. 실제 값(Value) 은 애플리케이션이 실행되는 환경(OS, Container)에서 주입받습니다.

### ✅ 올바른 패턴 (application.yml)
```yaml
spring:
  datasource:
    # 코드는 'DB_USERNAME'이라는 변수만 찾음. 실제 값이 무엇인지는 모름.
    username: ${DB_USERNAME} 
    password: ${DB_PASSWORD}
```

### ❌ 피해야 할 패턴
```yaml
spring:
  datasource:
    username: my_real_id  # 값을 코드에 하드코딩하면 보안 사고 발생 및 환경별 변경 불가
```

---

## 2. 환경별 데이터 주입 흐름

어떤 환경이든 "최종 목적지는 Spring Boot 컨테이너의 내부 환경변수" 입니다.

### 2-1. Render (PaaS)
Render 콘솔에서 입력한 값이 컨테이너 실행 시점에 주입됩니다.
> Flow: Render Dashboard (Environment Tab) → Container ENV → Spring Boot (`application.yml`)

### 2-2. Local & AWS EC2 (Docker Compose)
`.env` 파일에 적힌 값을 Docker Compose가 읽어서 컨테이너에 주입합니다.
> Flow: `.env` 파일 → `docker-compose.yml` (`${KEY}`) → Container ENV → Spring Boot (`application.yml`)

---

## 3. Docker Compose와 .env의 연결

Docker Compose는 기본적으로 같은 디렉토리에 있는 `.env` 파일을 자동으로 읽습니다.

### Step 1: `.env` 파일 작성 (값 저장소)
```properties
# .env
DB_USERNAME=recipemate
DB_PASSWORD=secret1234
SPRING_PROFILES_ACTIVE=dev
```

### Step 2: `docker-compose.yml` 설정 (전달자)
`.env`에서 읽은 값을 컨테이너 내부로 전달합니다.
```yaml
services:
  app:
    environment:
      # 형식: [컨테이너 내부 변수명]: [Docker가 .env에서 읽은 값]
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
```

---

## 4. "Local용 vs AWS용 분리"의 의미

"분리"란 키(Key) 이름은 통일하되, 값(Value)만 환경에 맞게 교체하는 전략을 의미합니다.

| 변수명 (Key) | Local 환경 (`.env`) | AWS 환경 (`.env`) | 설명 |
| :--- | :--- | :--- | :--- |
| `DB_HOST` | `postgres` | `recipemate-db.xxxxx.ap-northeast-2.rds.amazonaws.com` | 로컬은 컨테이너명, AWS는 RDS 주소 |
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` | 로컬은 개발 모드, AWS는 운영 모드 |
| `CLOUDINARY_URL` | `cloudinary://test...` | `cloudinary://real...` | (선택) 테스트용/운영용 계정 분리 |

### 실전 적용 방법

1.  로컬 개발 시:
    *   프로젝트 루트에 `.env` 파일을 두고 로컬용 값(DB=localhost 등)을 입력.
    *   `docker compose up` 실행.
2.  AWS 배포 시:
    *   서버에 프로젝트를 클론.
    *   서버에 `.env` 파일을 새로 생성하고, 운영용 값(실제 비밀번호, RDS 주소 등) 을 입력.
    *   `docker compose up` 실행.
    *   *Tip: 굳이 `.env.aws`로 파일명을 바꾸지 않고, 각 서버마다 그에 맞는 `.env` 파일 하나만 두는 것이 가장 단순합니다.*

---

## 5. 요약 및 권장 사항

1.  단일 `.env` 파일 유지: 복잡하게 `.env.local`, `.env.common` 등으로 나누기보다, 현재 환경에 맞는 내용을 담은 `.env` 파일 하나만 관리하는 것이 초기에는 명확합니다.
2.  변수명 통일: `application.yml`, Docker Compose, `.env` 모든 곳에서 사용하는 변수명(예: `DB_USERNAME`)을 정확히 일치시켜야 합니다.
3.  보안: `.env` 파일에는 실제 비밀번호와 API 키가 포함되므로 절대 Git에 커밋하지 않습니다 (`.gitignore` 필수 등록).
