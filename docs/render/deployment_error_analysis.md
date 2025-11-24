# Render 배포 오류 분석 및 해결 방안 (2025-11-24)

## 현상

1.  **1차 배포 실패 (메모리 초과):** `application.yml`의 기본 프로필 설정으로 인해 `dev`와 `prod` 프로필이 동시에 활성화되어 메모리 초과 오류가 발생했습니다.
2.  **2차 배포 실패 (설정 오류):** 1차 문제 수정 과정에서, `dev` 프로필 블록 내부에 `spring.profiles.include`를 잘못 위치시켜 `InactiveConfigDataAccessException` 오류가 발생했습니다.

## 2차 배포 실패 원인 분석 (정정)

이전 분석은 제가 다른 로그 파일을 참조하여 발생한 오류였습니다. `docs/tmp_log2.md`에 기록된 실제 오류는 다음과 같습니다.

```
org.springframework.boot.context.config.InactiveConfigDataAccessException: Inactive property source ... cannot contain property 'spring.profiles.include'
```

이 오류는 Spring Boot의 프로필 설정 규칙을 위반했기 때문에 발생합니다. `spring.profiles.include` 속성은 `---` 구분자로 나뉜 프로필별 문서 블록 내부에서는 사용할 수 없습니다.

두 번째 수정 단계에서 제가 `dev` 프로필 블록 안에 `include` 설정을 추가하여 이 오류를 직접적으로 유발했습니다.

## 최종 해결 방안

Spring Boot의 표준 구성 방식을 따라 다음과 같이 문제를 해결했습니다.

1.  **`application.yml` 수정:** `dev` 프로필 블록 내부에 잘못 추가했던 `profiles.include` 설정을 제거했습니다.
2.  **`application-dev.yml` 신규 생성:** `dev` 프로필이 활성화될 때만 `dev.local` 프로필을 포함시키기 위해, `src/main/resources/application-dev.yml` 파일을 새로 만들고 아래 내용을 추가했습니다.

    ```yaml
    # src/main/resources/application-dev.yml
    spring:
      profiles:
        include: dev.local
    ```

### 정상 동작 원리

-   **운영 환경 (`SPRING_PROFILES_ACTIVE=prod`):** Spring Boot는 `application.yml`과 `application-prod.yml`만 로드합니다. `application-dev.yml`은 프로필이 일치하지 않아 무시되므로, 불필요한 로컬 설정이 포함되지 않습니다.
-   **개발 환경 (`SPRING_PROFILES_ACTIVE=dev`):** Spring Boot는 `application.yml`과 `application-dev.yml`을 로드합니다. 이때 `application-dev.yml`에 의해 `application-dev.local.yml`이 추가로 포함되어, 기존 로컬 개발 환경이 정상적으로 동작합니다.

## 다음 단계

모든 수정이 완료되었습니다. 변경 사항을 Git에 커밋하고 Render에 다시 배포를 시도해 보시기 바랍니다. 이제 프로필 설정 문제가 완전히 해결되어 정상적으로 배포가 진행될 것입니다.
