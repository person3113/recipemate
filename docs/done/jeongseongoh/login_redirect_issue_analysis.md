# 로그인 후 간헐적 404 오류 분석 및 해결 방안

## 1. 문제 현상

로그인 성공 직후, 간헐적으로 의도하지 않은 URL로 리디렉션되면서 404 Not Found 오류 페이지가 표시되는 문제가 발생합니다.

- **사례 1:** `http://localhost:8080/.well-known/appspecific/com.chrome.devtools.json?continue` 로 리디렉션되며 404 오류 발생.
    - 브라우저 개발자 도구(F12)가 열려 있을 때 발생 빈도가 높음.
    - 백엔드 콘솔에는 관련 로그가 남지 않음.

- **사례 2:** `http://localhost:8080/login?continue` 로 리디렉션되며 404 오류 발생.
    - `No static resource login.` 라는 메시지가 함께 표시됨.

두 경우 모두, 브라우저에서 뒤로 가기를 눌러 다시 로그인을 시도하거나 홈으로 이동하면 정상적으로 로그인 상태가 유지됩니다.

## 2. 원인 분석

이 문제의 핵심 원인은 Spring Security의 `SavedRequest` 기능과 관련이 있습니다.

Spring Security는 인증되지 않은 사용자가 특정 페이지에 접근을 시도할 경우, 해당 요청을 세션에 저장(`SavedRequest`)하고 사용자를 로그인 페이지로 보냅니다. 로그인에 성공하면, 원래 요청했던 페이지로 다시 리디렉션해주는 사용자 편의 기능을 기본으로 제공합니다.

- **사례 1의 원인 (`.well-known`):**
    1. 사용자가 아직 인증되지 않은 상태에서 브라우저(특히 Chrome) 개발자 도구가 백그라운드에서 소스맵핑 등을 위해 `/.well-known/...` 경로로 요청을 보냅니다.
    2. Spring Security는 이 요청을 "인증이 필요한 사용자의 원래 목적지"로 간주하고 세션에 저장합니다.
    3. 사용자가 로그인을 성공적으로 마치면, Spring Security는 저장된 요청인 `/.well-known/...` 경로로 리디렉션합니다.
    4. 우리 애플리케이션에는 해당 경로를 처리하는 컨트롤러나 정적 리소스가 없으므로 서버는 404 Not Found를 반환합니다.

- **사례 2의 원인 (`login?continue`):**
    1. 사용자가 로그인 페이지(`/auth/login`)에 머무는 동안, 브라우저의 자동 요청이나 새로고침 등의 이유로 Spring Security가 현재 페이지인 `/auth/login`을 `SavedRequest`로 저장하는 경우가 발생할 수 있습니다.
    2. 로그인 성공 후, Spring Security는 저장된 경로인 `/auth/login`으로 다시 리디렉션합니다.
    3. 이때, 내부 처리 과정에서 `?continue`와 같은 파라미터가 붙을 수 있으며, 애플리케이션은 이 요청을 처리할 적절한 핸들러를 찾지 못해 404 오류를 반환합니다.

## 3. 해결 방안

`SavedRequest` 기능 자체는 유용하므로 비활성화하는 대신, 불필요한 요청이 저장되지 않도록 Spring Security 설정을 수정하는 것이 바람직합니다.

1. **특정 경로를 Spring Security가 무시하도록 설정:**
   - 개발자 도구나 브라우저가 보내는 내부 요청(`/.well-known/**`)은 인증이 필요 없는 리소스이므로, Spring Security 필터 체인에서 무시하도록 설정하여 `SavedRequest`에 저장되지 않도록 합니다.
   - `SecurityConfig.java`의 `authorizeHttpRequests` 설정에 `.requestMatchers("/.well-known/**").permitAll()`를 추가합니다.

2. **로그인 성공 시 기본 리디렉션 URL 지정:**
   - 만약 `SavedRequest`에 저장된 요청이 없을 경우(예: 사용자가 바로 로그인 페이지로 접근한 경우), 명확한 기본 목적지를 지정해줍니다.
   - `formLogin` 설정에 `.defaultSuccessUrl("/")`를 추가하여, 별도의 저장된 요청이 없으면 항상 홈페이지로 이동하도록 합니다. 이는 `/login?continue`와 같은 예외 상황을 방지하는 데 도움이 됩니다.

## 4. 조치 계획

`src/main/java/com/recipemate/global/config/SecurityConfig.java` 파일을 다음과 같이 수정합니다.

- `authorizeHttpRequests` 체인에 `/.well-known/**` 경로를 `permitAll()`에 추가합니다.
- `formLogin` 체인에 `defaultSuccessUrl("/")`를 추가합니다.
