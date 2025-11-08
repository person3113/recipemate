# 댓글 기능 비로그인 사용자 접근 문제 분석 및 리팩토링 계획

## 1. 문제 상황

- `http://localhost:8080/group-purchases/{id}` (공구 상세 페이지)
- `http://localhost:8080/community-posts/{id}` (커뮤니티 게시글 상세 페이지)

위 페이지들에서 비로그인 사용자가 접근할 경우, 댓글 목록이 보여야 할 자리에 로그인 폼이 대신 표시되는 문제가 발생하고 있습니다.

**기대하는 동작:**
- 비로그인 사용자도 댓글 **목록**은 볼 수 있어야 합니다.
- 비로그인 사용자는 댓글을 **작성**할 수 없어야 합니다. (댓글 작성 폼이 보이지 않아야 함)

## 2. 원인 분석

`codebase_investigator`를 통해 분석한 결과, 문제의 원인은 **Spring Security 설정**에 있었습니다.

1.  **HTMX를 통한 동적 로딩**:
    - 상세 페이지(`group-purchases/detail.html`, `community-posts/detail.html`)는 `htmx`를 사용하여 페이지 로드 시 `/comments/fragments` 라는 URL로 GET 요청을 보내 댓글 부분을 동적으로 가져옵니다.
    - 이 요청은 `CommentController`의 `getCommentsFragment` 메서드에 의해 처리됩니다.

2.  **보안 설정 누락**:
    - `SecurityConfig.java` 파일의 `securityFilterChain` 설정에서 `/comments/fragments` 경로에 대한 모든 사용자(`permitAll`)의 접근 권한이 명시적으로 설정되어 있지 않습니다.
    - 따라서 `anyRequest().authenticated()` 규칙이 적용되어, 인증되지 않은 사용자의 요청이 로그인 페이지로 리디렉션됩니다.
    - `htmx`는 이 리디렉션 응답(로그인 페이지의 HTML)을 받아, 원래 댓글이 표시되어야 할 `<div>` 내부에 그대로 삽입합니다. 이것이 사용자가 로그인 폼을 보게 되는 이유입니다.

3.  **올바르게 구현된 부분**:
    - 댓글 UI를 담당하는 `fragments/comments.html` 템플릿 자체는 문제가 없습니다.
    - Thymeleaf Security Dialect (`sec:authorize="isAuthenticated()"`)를 사용하여 로그인한 사용자에게만 댓글 작성 폼을 보여주도록 이미 올바르게 구현되어 있습니다.

## 3. 해결 방안

`SecurityConfig.java` 파일에 `/comments/fragments` 경로에 대한 `GET` 요청을 모든 사용자가 허용하도록 설정을 추가하면 문제를 해결할 수 있습니다.

### 단계별 해결 계획

1.  **`SecurityConfig.java` 파일 열기**
    - 경로: `src/main/java/com/recipemate/global/config/SecurityConfig.java`

2.  **`securityFilterChain` 메서드 수정**
    - `authorizeHttpRequests` 설정 부분에 `.requestMatchers(HttpMethod.GET, "/comments/fragments").permitAll()` 설정을 추가합니다.
    - 이렇게 하면 인증 여부와 관계없이 모든 사용자가 댓글 목록을 조회할 수 있게 됩니다.

3.  **수정 후 코드 예시**

    ```java
    // src/main/java/com/recipemate/global/config/SecurityConfig.java

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ... 기존 설정들
            .authorizeHttpRequests(auth -> auth
                // ... 기존 requestMatchers 설정들
                .requestMatchers(HttpMethod.GET, "/comments/fragments").permitAll() // <-- 이 줄을 추가
                .anyRequest().authenticated()
            )
            // ... 나머지 설정들
        ;
        return http.build();
    }
    ```

## 4. 기대 효과

- 위와 같이 수정하면, 비로그인 사용자도 공구 및 게시글 상세 페이지에서 댓글 목록을 정상적으로 볼 수 있습니다.
- 댓글 작성 폼은 `fragments/comments.html`의 `sec:authorize` 설정에 따라 로그인한 사용자에게만 계속 표시되므로, 기존의 "로그인해야만 댓글 작성 가능" 정책은 유지됩니다.
