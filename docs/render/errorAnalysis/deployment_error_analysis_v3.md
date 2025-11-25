# Render 배포 오류 분석 및 해결 방안 (3차)

## 현상

`application.yml` 및 `application-dev.yml` 파일 수정 후 3차 배포를 시도했으나, `Exited with status 1` 오류와 함께 또다시 실패했습니다.

## 3차 배포 실패 원인 분석

3차 배포 로그(`docs/tmp_log3.md`)를 분석한 결과, 이번 실패의 원인은 다음과 같습니다.

```
org.springframework.boot.context.config.InvalidConfigDataPropertyException: Property 'spring.profiles' ... is invalid and should be replaced with 'spring.config.activate.on-profile'
```

이 오류는 `application.yml` 파일의 최상위(root) 레벨에 있는 `spring.profiles` 속성이 현재 사용 중인 Spring Boot 버전(3.x)의 설정 방식과 맞지 않아 발생합니다.

Spring Boot 2.4 버전 이후, `---` 구분자를 사용해 여러 프로필 설정을 한 파일에 넣는 방식이 도입되면서, `spring.profiles`를 최상위 레벨에서 사용하는 것이 금지되거나 다른 속성과 충돌을 일으키게 되었습니다. 오류 메시지가 명확히 알려주듯이, 프로필 활성화는 `spring.config.activate.on-profile`을 사용해야 하며, 최상위 레벨에서는 `spring.profiles` 속성을 사용해서는 안 됩니다.

이전 단계에서 제가 남겨둔 주석을 포함한 `spring.profiles` 블록이 이 문제를 유발했습니다.

## 최종 해결 방안

`application.yml`에서 문제가 되는 `spring.profiles` 블록을 완전히 제거하여 Spring Boot의 현재 설정 규칙을 따르도록 수정합니다.

-   **제거 대상:** `application.yml` 파일 최상단의 `profiles:` 블록 전체

이렇게 수정하면, 프로필 활성화는 전적으로 외부 설정(`SPRING_PROFILES_ACTIVE` 환경 변수)에 의해 제어되므로 더 이상 설정 파일 내부에서 충돌이 발생하지 않습니다.

## 다음 단계

1.  `application.yml` 파일에서 불필요한 `profiles:` 블록을 제거합니다.
2.  수정된 코드를 Git에 커밋하고 Render에 재배포합니다.
