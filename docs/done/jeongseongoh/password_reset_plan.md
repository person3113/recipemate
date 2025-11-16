# 비밀번호 찾기 기능 구현 계획

## 1. 개요

이메일 인증의 번거로움을 줄이고 간편한 사용자 경험을 제공하기 위해, **가입 시 입력한 '이메일'과 '전화번호' 조합**을 이용해 본인임을 확인하고 즉시 비밀번호를 재설정하는 기능을 구현합니다.

### 사용자 흐름

1.  **요청**: 로그인 페이지에서 `[비밀번호를 잊으셨나요?]` 링크를 클릭합니다.
2.  **본인 확인**: '비밀번호 찾기 요청' 페이지(`GET /auth/password/reset-request`)로 이동하여 이메일과 전화번호를 입력하고 제출(`POST`)합니다.
3.  **정보 검증**: 서버는 제출된 이메일과 전화번호가 모두 일치하는 계정이 있는지 확인합니다.
    - **일치 시**: 사용자를 '비밀번호 재설정' 페이지(`GET /auth/password/reset`)로 이동시킵니다. 이 때, URL에 임시 토큰을 포함하여 무단 접근을 방지합니다.
    - **불일치 시**: '비밀번호 찾기 요청' 페이지에 오류 메시지를 표시합니다.
4.  **재설정**: '비밀번호 재설정' 페이지에서 새 비밀번호를 입력하고 제출(`POST`)합니다.
5.  **완료**: 비밀번호 변경이 완료되면, 성공 메시지와 함께 로그인 페이지로 리디렉션됩니다.

## 2. 구현 세부 사항

### 2.1. Backend

#### 1. Controller (`PasswordResetController.java` 신규 생성)

`AuthController`의 역할을 분리하여 비밀번호 찾기 전용 컨트롤러를 `com.recipemate.domain.user.controller` 패키지 내에 생성합니다.

-   **`GET /auth/password/reset-request`**:
    -   본인 확인 폼(`auth/password-reset-request.html`)을 렌더링합니다.
-   **`POST /auth/password/reset-request`**:
    -   `PasswordResetRequestDto` (email, phoneNumber)로 요청을 받습니다.
    -   `UserService`를 호출하여 정보 일치 여부를 확인합니다.
    -   **일치 시**:
        1.  `UUID`를 사용해 임시 토큰을 생성합니다.
        2.  `HttpSession`에 사용자의 `email`과 `토큰`을 저장합니다. (예: `session.setAttribute("resetUserEmail", email)`, `session.setAttribute("resetToken", token)`)
        3.  `/auth/password/reset?token={token}`으로 리디렉션합니다.
    -   **불일치 시**: 오류 메시지와 함께 `auth/password-reset-request.html` 뷰를 다시 보여줍니다.
-   **`GET /auth/password/reset`**:
    -   `@RequestParam`으로 `token`을 받습니다.
    -   세션에 저장된 토큰과 일치하는지, 유효한지 검증합니다.
    -   **유효 시**: 비밀번호 재설정 폼(`auth/password-reset-form.html`)을 렌더링합니다.
    -   **무효 시**: 오류 메시지와 함께 로그인 페이지로 리디렉션합니다.
-   **`POST /auth/password/reset`**:
    -   `PasswordResetFormDto` (newPassword, confirmPassword)와 `@RequestParam`으로 `token`을 받습니다.
    -   세션 토큰을 다시 한번 검증합니다.
    -   `UserService`를 호출하여 세션에 저장된 이메일을 가진 사용자의 비밀번호를 변경합니다.
    -   성공 처리 후 세션의 토큰과 이메일 정보를 제거하고, 성공 메시지와 함께 `/auth/login`으로 리디렉션합니다.

#### 2. Service (`UserService.java` 수정)

-   **`Optional<User> verifyUserByEmailAndPhoneNumber(String email, String phoneNumber)`**:
    -   `UserRepository`를 호출하여 이메일과 전화번호가 모두 일치하는 사용자를 조회하고 `Optional<User>`로 반환합니다.
-   **`void resetPassword(String email, String newPassword)`**:
    -   이메일로 사용자를 조회합니다.
    -   `PasswordEncoder`를 사용해 새 비밀번호를 암호화합니다.
    -   사용자 엔티티의 비밀번호를 업데이트하고 저장합니다.

#### 3. Repository (`UserRepository.java` 수정)

-   이메일과 전화번호로 사용자를 찾는 JPA 쿼리 메서드를 추가합니다.
    ```java
    Optional<User> findByEmailAndPhoneNumber(String email, String phoneNumber);
    ```

#### 4. DTO (`.../user/dto/` 패키지 내 신규 생성)

-   **`PasswordResetRequestDto.java`**: `@NotBlank` 유효성 검사가 적용된 `email`, `phoneNumber` 필드를 가집니다.
-   **`PasswordResetFormDto.java`**: `@NotBlank`, `@Size(min=8)` 등이 적용된 `newPassword`, `confirmPassword` 필드를 가집니다.

### 2.2. Frontend

#### 1. 비밀번호 찾기 요청 폼 (`templates/auth/password-reset-request.html` 신규 생성)

-   이메일과 전화번호를 입력받는 `<form>`을 포함합니다.
-   `POST` 요청 경로는 `/auth/password/reset-request`입니다.
-   오류 발생 시 `th:if`를 사용하여 서버가 전달한 오류 메시지를 표시합니다.

#### 2. 비밀번호 재설정 폼 (`templates/auth/password-reset-form.html` 신규 생성)

-   '새 비밀번호', '새 비밀번호 확인' 필드를 포함한 `<form>`을 가집니다.
-   `POST` 요청 경로는 `th:action="@{/auth/password/reset(token=${param.token})}"`와 같이 토큰을 포함합니다.
-   두 비밀번호 필드의 일치 여부를 실시간으로 확인하는 간단한 JavaScript를 추가하여 사용자 편의성을 높입니다.

#### 3. 로그인 폼 (`templates/auth/login.html` 확인)

-   `[비밀번호를 잊으셨나요?]` 링크의 `href` 속성이 `/auth/password/reset-request`로 올바르게 설정되어 있는지 확인합니다. (현재 코드 베이스에 이미 올바르게 설정되어 있음)

## 3. 작업 단계 요약

1.  `UserRepository`에 `findByEmailAndPhoneNumber` 메서드를 추가합니다.
2.  `PasswordResetRequestDto`와 `PasswordResetFormDto` 클래스를 생성합니다.
3.  `PasswordResetController`를 생성하고, 위에서 설계한 GET/POST 핸들러 메서드들을 구현합니다.
4.  `UserService`에 비밀번호 재설정 관련 비즈니스 로직을 구현합니다.
5.  `templates/auth/` 경로에 `password-reset-request.html`과 `password-reset-form.html` 뷰 파일을 생성합니다.
6.  전체 기능(요청 → 검증 → 재설정 → 로그인)이 정상적으로 동작하는지 테스트합니다.
