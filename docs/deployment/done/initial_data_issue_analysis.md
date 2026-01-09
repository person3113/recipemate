# 초기 데이터 미생성 원인 분석 및 해결 가이드

## 1. 현상 분석
현재 Docker 환경에서 실행 시 `AdminUserInitializer`와 `RecipeDataInitializer`가 동작하지 않아 초기 데이터(관리자 계정, 테스트 계정, 레시피 데이터)가 생성되지 않는 현상이 발생하고 있습니다.

## 2. 원인 상세

### 2.1 관리자 및 테스트 계정 미생성 (`AdminUserInitializer`)
- **코드 설정**: `AdminUserInitializer`는 `@Profile("dev")`로 설정되어 있어 `dev` 프로파일에서만 빈으로 등록됩니다.
- **Docker 설정**: `docker-compose.yml`은 기본적으로 `prod` 프로파일을 사용합니다.
- **결론**: 프로파일 불일치로 인해 초기화 코드가 실행되지 않습니다.

### 2.2 레시피 데이터 미생성 (`RecipeDataInitializer`)
- **코드 설정**: `RecipeDataInitializer`는 `@ConditionalOnProperty(name = "recipe.init.enabled", havingValue = "true")`가 붙어 있어, 해당 설정이 명시적으로 `true`일 때만 빈으로 등록됩니다.
- **로직 상세**: 빈이 등록된 후 `run()` 메서드 내부에서 `recipeRepository.count() > 0`을 체크합니다.
    - 데이터가 이미 있다면? -> 초기화 스킵 (단, `force=true`면 강제 진행)
    - 데이터가 없다면? -> API 호출하여 데이터 저장
- **결론**: `RECIPE_INIT_ENABLED` 환경변수가 `false`(기본값)이면 아예 빈 등록조차 안 되므로, 데이터가 비어있어도 채워 넣는 로직이 시작되지 않습니다.

## 3. 심층 분석: `dev` 프로파일과 DB 충돌 문제

1.  **`application.yml`의 `dev` 설정**:
    ```yaml
    spring:
      config:
        activate:
          on-profile: dev
      datasource:
        driver-class-name: org.h2.Driver
        url: jdbc:h2:file:./data/recipemate...
    ```
    `dev` 프로파일은 H2 데이터베이스를 사용하도록 강제되어 있습니다.

2.  **`docker-compose.yml`의 설정**:
    ```yaml
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/recipemate
    ```
    Docker는 PostgreSQL을 사용하도록 환경변수를 주입합니다.

3.  **충돌 발생**:
    만약 Docker에서 `SPRING_PROFILES_ACTIVE=dev`로 설정하면:
    - Spring Boot는 `application.yml`의 `dev` 섹션을 읽어 **H2 Driver**와 **H2 Dialect**를 로드하려고 시도합니다.
    - 동시에 `docker-compose`는 URL을 **PostgreSQL**로 덮어씁니다.
    - **결과**: 드라이버(H2)와 URL(Postgres)이 불일치하거나, Hibernate Dialect 설정이 꼬여서 오류가 발생하거나 의도치 않은 동작(임베디드 H2 구동 등)이 일어날 수 있습니다.

**따라서 Docker 환경(PostgreSQL 사용)에서는 `dev` 프로파일을 활성화하지 않는 것이 좋습니다.**

## 4. 최종 해결 가이드

관리자/테스트 계정은 굳이 필요 없고 **"초기 레시피 데이터"**만 필요하다면, 프로파일은 `prod`로 유지하고 레시피 초기화 옵션만 켜는 것이 가장 안전하고 깔끔합니다.

### 추천 방법: `.env` 파일 설정

프로젝트 루트에 `.env` 파일을 생성하고 아래와 같이 설정합니다.

```properties
# .env 파일

# 프로파일은 prod 유지 (PostgreSQL 사용을 위해)
SPRING_PROFILES_ACTIVE=prod

# 레시피 초기화 기능 활성화 (이게 true여야 빈이 생성되고, DB가 비었는지 체크함)
RECIPE_INIT_ENABLED=true

# (선택) 데이터가 이미 있어도 덮어쓰고 싶다면 true, 아니면 false
RECIPE_INIT_FORCE=false
```

이후 Docker를 재시작합니다.

```bash
docker-compose down
docker-compose up -d
```

### 요약
- **`RECIPE_INIT_ENABLED=true`**: "초기화 기능을 켠다." (이게 꺼져있으면 DB가 비어있어도 아무 일도 안 일어남)
- **`RECIPE_INIT_FORCE=false`**: "기능은 켜져 있지만, DB에 데이터가 있으면 건너뛴다." (안전한 기본값)
- **`dev` 프로파일**: Docker 환경(Postgres)에서는 `application.yml` 설정(H2)과 충돌하므로 사용을 권장하지 않음.
