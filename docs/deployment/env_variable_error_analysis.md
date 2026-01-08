# 환경변수 누락 오류 분석 보고서

## 1. 오류 상황
Spring Boot 애플리케이션 시작 시 `UnsatisfiedDependencyException`이 발생하며 강제 종료되었습니다.
*   **핵심 원인**: `Injection of autowired dependencies failed`
*   **세부 원인**: `Could not resolve placeholder 'CLOUDINARY_URL' in value "${CLOUDINARY_URL}"`

## 2. 상세 분석

### A. 의존성 주입 실패 체인
에러 로그를 역추적하면 다음과 같은 의존성 연결 고리에서 문제가 발생했습니다.
1.  `GroupBuyController` (생성자)
2.  `GroupBuyService` (생성자)
3.  `ImageUploadUtil` (생성자)
4.  `CloudinaryConfig` (Bean 생성 중 필드 주입 실패)

### B. 근본 원인: 환경변수 설정 누락
`application.yml` 파일에는 다음과 같이 환경변수를 참조하도록 설정되어 있습니다.
```yaml
cloudinary:
  url: ${CLOUDINARY_URL}

kakao:
  javascript-key: ${KAKAO_JAVASCRIPT_KEY}
```

하지만 `docker-compose.yml` 파일의 `app` 서비스 환경변수(`environment`) 목록에는 이 두 변수가 **빠져 있습니다.**

**현재 docker-compose.yml 설정:**
```yaml
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/recipemate
      # (중략)
      FOOD_SAFETY_API_KEY: ${FOOD_SAFETY_API_KEY}
      # CLOUDINARY_URL과 KAKAO_JAVASCRIPT_KEY가 없음!
```

### C. 왜 발생했는가?
이전 가이드(`LOCAL_DOCKER_ENV_GUIDE.md`)에 따라 `.env` 파일에는 `CLOUDINARY_URL` 등의 키 값을 정의했을 것입니다. 하지만 Docker Compose는 `.env` 파일에 있는 값을 **자동으로 컨테이너 내부의 환경변수로 주입해주지 않습니다.**
반드시 `docker-compose.yml`의 `environment` 섹션에 명시적으로 매핑해 주어야 합니다.

## 3. 해결 방안

`docker-compose.yml` 파일에 누락된 환경변수 매핑을 추가해야 합니다.

**변경할 내용 (`app` 서비스의 `environment` 섹션):**
```yaml
    environment:
      # ... 기존 설정 ...
      FOOD_SAFETY_API_KEY: ${FOOD_SAFETY_API_KEY}
      
      # [추가됨] 외부 API 필수 환경변수
      CLOUDINARY_URL: ${CLOUDINARY_URL}
      KAKAO_JAVASCRIPT_KEY: ${KAKAO_JAVASCRIPT_KEY}
      
      # ... 기존 설정 ...
```

위와 같이 수정 후 다시 `docker compose up -d --build`를 실행하면, `.env` 파일에 있는 실제 키 값들이 컨테이너로 전달되어 Spring Boot가 정상적으로 해당 값을 읽을 수 있게 됩니다.
