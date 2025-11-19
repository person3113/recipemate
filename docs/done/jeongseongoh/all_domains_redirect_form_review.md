# 전역 리다이렉트 및 폼 제출 문제 분석

## 1. 요약

이 문서는 `docs/redirect_improvement_plan.md`에서 제기된 문제를 애플리케이션의 다른 도메인으로 확장하여 분석합니다. 기존 분석에서는 `GroupBuy` 도메인의 두 가지 주요 문제, 즉 **불편한 리다이렉션**과 **폼 재제출 위험(PRG 패턴 위반)**을 지적했습니다.

전체 코드를 분석한 결과, 여러 다른 컨트롤러에서도 비슷한 문제가 발견되었습니다. 특히 **회원가입(`AuthController`)**과 **커뮤니티 게시글(`PostController`)** 처리에 있어 PRG 패턴 위반이라는 중대한 결함이 존재함을 확인했습니다. 다른 컨트롤러들은 PRG 패턴을 비교적 잘 준수하고 있었으나, 리다이렉션 로직이 하드코딩되어 있어 사용자 경험의 일관성과 유연성이 부족한 경우가 있었습니다.

이 문서는 각 도메인별 분석 결과를 요약하고 일관된 해결책을 제안합니다.

## 2. 도메인별 분석 결과

### 2.1. `AuthController` (회원가입) - 심각한 문제

-   **PRG 패턴 위반**: `POST /auth/signup` 핸들러는 유효성 검사 실패 시, `RedirectAttributes`를 사용하지 않고 `Model`에 에러 메시지를 담아 뷰(`auth/signup`)를 직접 반환합니다. 이는 사용자가 브라우저를 새로고침할 경우 폼 데이터가 다시 제출될 위험이 있는 심각한 결함입니다.

    ```java
    // src/main/java/com/recipemate/domain/user/controller/AuthController.java
    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupRequest") SignupRequest request,
            org.springframework.validation.BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("formData", request);
            // ...
            return "auth/signup"; // PRG 패턴 위반
        }
        // ...
        return "redirect:/auth/login";
    }
    ```

### 2.2. `PostController` (커뮤니티 게시글) - 심각한 문제

-   **PRG 패턴 위반**: `createPost`와 `updatePost` 메소드 모두 유효성 검사 실패 시 폼 뷰(`community-posts/form`)를 직접 반환하여 `AuthController`와 동일한 재제출 위험을 안고 있습니다.

    ```java
    // src/main/java/com/recipemate/domain/post/controller/PostController.java
    @PostMapping
    public String createPost(
            // ...
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            // ...
            return "community-posts/form"; // PRG 패턴 위반
        }
        // ...
    }
    ```

-   **하드코딩된 리다이렉션**: 작업 완료 후 항상 상세 페이지(`/community-posts/{postId}`) 또는 목록 페이지(`/community-posts/list`)로 리다이렉션됩니다. `redirectUrl` 파라미터를 지원하지 않아, 사용자가 이전에 있던 특정 필터가 적용된 목록 페이지로 돌아갈 수 없습니다.

### 2.3. `UserController` (사용자 기능) - 개선 필요

-   **PRG 관련 사용성 문제**: `chargePoints` (포인트 충전) 메소드는 입력값 유효성 검사 실패 시 PRG 패턴에 따라 리다이렉트는 하지만, `RedirectAttributes`에 사용자가 입력했던 금액을 담아주지 않습니다. 이로 인해 사용자는 입력했던 값을 잃고 빈 폼을 다시 마주하게 되어 사용성이 저하됩니다.

-   **리다이렉션 로직 비일관성**: `myGroupPurchasesPage` (내 공구 목록)에서는 `redirectUrl`을 생성하여 모델에 전달하는 모범적인 패턴이 적용되어 있으나, `myCommunityActivityPage` (내 커뮤니티 활동) 등 다른 목록 페이지에서는 이 패턴이 적용되지 않아 일관성이 부족합니다.

### 2.4. `CommentController`, `DirectMessageController`, `ReportController`, `ReviewController` - 양호

-   **PRG 패턴 준수**: 이 컨트롤러들은 `POST` 요청 처리 시 `RedirectAttributes` 사용 및 리다이렉트를 통해 PRG 패턴을 올바르게 준수하고 있습니다.
-   **하드코딩된 리다이렉션**: 대부분의 경우, 기능의 특성상 고정된 페이지(예: 쪽지방, 신고한 콘텐츠)로 리다이렉션하는 것이 자연스러워 큰 문제는 되지 않습니다. 다만, `CommentController`의 경우 `redirectUrl` 파라미터를 지원하면 더 유연한 사용자 경험을 제공할 수 있습니다.

## 3. 제안된 해결책

1.  **PRG 패턴 위반 해결 (가장 시급)**
    -   `AuthController` (`signup`)와 `PostController` (`createPost`, `updatePost`)의 `POST` 핸들러를 리팩토링해야 합니다.
    -   유효성 검사 실패 시, `Model`에 데이터를 추가하고 뷰를 직접 반환하는 대신, `RedirectAttributes`에 폼 데이터(`@ModelAttribute` 객체)와 `BindingResult`를 추가해야 합니다.
    -   그 후, 폼을 렌더링하는 `GET` 엔드포인트로 리다이렉트해야 합니다. (`redirect:/auth/signup`, `redirect:/community-posts/new` 등)

2.  **리다이렉션 유연성 및 일관성 확보**
    -   `PostController`와 `UserController`의 `myCommunityActivityPage` 등, 목록에서 상세 페이지로 이동하여 작업을 수행하는 모든 경우에 `redirectUrl` 파라미터 패턴을 일관되게 적용합니다.
    -   뷰 템플릿에서는 수정/삭제 등의 링크에 현재 URL(`request.requestURI + query string`)을 `redirectUrl` 값으로 포함시킵니다.
    -   컨트롤러의 작업 처리 메소드는 `redirectUrl`을 받아, 작업 완료 후 해당 URL로 리다이렉트합니다. (기본값은 상세 페이지)

3.  **사용자 입력 값 유지**
    -   `UserController`의 `chargePoints` 메소드를 리팩토링하여, 유효성 검사 실패 시 사용자가 입력했던 금액을 `RedirectAttributes`를 통해 폼으로 다시 전달해야 합니다. 이를 위해 단순 `@RequestParam` 대신 폼 객체(`@ModelAttribute`)를 사용하는 것을 권장합니다.

이러한 변경을 통해 애플리케이션 전반의 안정성과 사용자 경험을 크게 향상시킬 수 있습니다.
