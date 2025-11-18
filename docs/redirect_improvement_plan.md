# 리다이렉트 및 폼 제출 개선 계획

## 1. 요약

이 문서는 애플리케이션의 폼 처리 및 리다이렉션 로직의 사용자 경험과 안정성을 개선하기 위한 계획을 설명합니다. 현재 구현에는 두 가지 주요 문제가 있습니다.

1.  **불편한 리다이렉션:** "내 공구 목록" 페이지 (`/users/me/group-purchases`)에서 공동 구매를 수정하거나 취소한 후, 사용자는 이전에 있던 목록 페이지가 아닌 해당 항목의 상세 페이지로 리다이렉트됩니다.
2.  **폼 재제출 위험:** 여러 폼, 특히 유효성 검사 실패 시 Post-Redirect-Get (PRG) 패턴을 올바르게 따르지 않습니다. 이는 사용자가 브라우저의 '뒤로 가기'나 '새로 고침' 버튼을 사용하여 실수로 폼을 다시 제출할 수 있음을 의미하며, 이는 중대한 결함입니다.

제안된 해결책은 두 단계로 구성됩니다.
- **1단계:** 리다이렉션 로직을 보다 동적으로 수정하여 사용자가 올바른 페이지로 돌아갈 수 있도록 원하는 반환 URL을 매개변수로 전달합니다.
- **2단계:** 컨트롤러를 리팩토링하여 PRG 패턴을 엄격하게 준수하도록 합니다. 이를 위해 `RedirectAttributes`를 사용하여 POST 요청 후에 데이터와 오류를 전달한 다음, 폼과 결과를 표시하는 GET 엔드포인트로 리다이렉트하여 재제출 가능성을 완전히 방지합니다.

## 2. 문제 분석

### 2.1. 작업 후 잘못된 리다이렉션

사용자가 개인 목록 페이지(`/users/me/group-purchases`)에서 작업(수정, 취소)을 수행하면, 컨트롤러 로직에 하드코딩된 리다이렉션으로 인해 사용자 경험이 저하됩니다.

**예시: 공동 구매 취소**

`GroupBuyController.java`에서 `cancelGroupBuy` 메소드는 항상 상세 페이지로 리다이렉트합니다.

```java
// src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java

@PostMapping("/{groupBuyId}/cancel")
public String cancelGroupBuy(@PathVariable Long groupBuyId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    groupBuyService.cancelGroupBuy(groupBuyId, userPrincipal.user());
    // 항상 상세 페이지로 리다이렉트되어 사용자 경험에 좋지 않음
    return "redirect:/group-purchases/" + groupBuyId;
}
```

`updateGroupBuy` 메소드에도 동일한 문제가 존재합니다. 사용자는 이전에 보던 목록으로 돌아가기를 기대합니다.

### 2.2. PRG 패턴 위반 및 재제출 위험

가장 중요한 문제는 PRG 패턴 위반입니다. 폼 제출 시 유효성 검사 오류가 발생하면, 컨트롤러는 `POST` 요청 핸들러에서 직접 폼 뷰를 반환합니다.

**예시: 유효하지 않은 데이터로 공동 구매 생성**

```java
// src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java

@PostMapping
public String createGroupBuy(@Valid @ModelAttribute("groupBuy") GroupBuyRequestDto requestDto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserPrincipal userPrincipal,
                             Model model) {
    if (bindingResult.hasErrors()) {
        // 잘못된 처리. POST 요청에서 직접 폼 뷰를 반환하고 있음.
        // 이 페이지에서 브라우저를 새로고침하면 폼을 다시 제출하려고 시도함.
        model.addAttribute("user", userPrincipal.user());
        return "group-purchases/form";
    }
    GroupBuy groupBuy = groupBuyService.createGroupBuy(userPrincipal.user(), requestDto);
    return "redirect:/group-purchases/" + groupBuy.getId();
}
```

이 구현은 사용자의 브라우저 URL이 `POST` 엔드포인트에 머물러 있음을 의미합니다. 페이지를 새로고침하면 브라우저는 데이터 재제출을 요청합니다. 다른 곳으로 이동했다가 '뒤로 가기' 버튼을 클릭하면 동일한 깨진 상태로 돌아갑니다. 이 문제는 `updateGroupBuy` 메소드에도 존재합니다.

## 3. 제안된 해결책 및 구현 계획

### 1단계: 잘못된 리다이렉션 수정

`redirectUrl` 매개변수를 도입하여 리다이렉션을 동적이고 컨텍스트에 맞게 만듭니다.

**조치 1: 뷰 템플릿 업데이트**

`user/my-group-purchases.html`과 같은 템플릿에서 수정 및 취소 작업에 현재 URL을 매개변수로 추가해야 합니다.

**예시 (`user/my-group-purchases.html`):**

```html
<!-- 수정 버튼/링크 -->
<a th:href="@{/group-purchases/{id}/edit(id=${item.id}, redirectUrl=${#httpServletRequest.requestURI})}">수정</a>

<!-- 취소 폼 -->
<form th:action="@{/group-purchases/{id}/cancel(id=${item.id})}" method="post">
    <input type="hidden" name="redirectUrl" th:value="${#httpServletRequest.requestURI}" />
    <button type="submit">취소</button>
</form>
```

**조치 2: 컨트롤러 메소드 업데이트**

컨트롤러 메소드는 `redirectUrl` 매개변수를 받아 우선적으로 사용하도록 업데이트됩니다.

```java
// src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java

@PostMapping("/{groupBuyId}/cancel")
public String cancelGroupBuy(@PathVariable Long groupBuyId,
                             @RequestParam(defaultValue = "/group-purchases/{groupBuyId}") String redirectUrl,
                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
    groupBuyService.cancelGroupBuy(groupBuyId, userPrincipal.user());
    // 제공된 URL로 리다이렉트하거나, 기본적으로 상세 페이지로 이동
    return "redirect:" + redirectUrl;
}

@PostMapping("/{groupBuyId}/edit")
public String updateGroupBuy(@PathVariable Long groupBuyId,
                             @Valid @ModelAttribute("groupBuy") GroupBuyRequestDto requestDto,
                             BindingResult bindingResult,
                             @RequestParam(defaultValue = "/group-purchases/{groupBuyId}") String redirectUrl,
                             /* ... 다른 매개변수들 ... */) {
    // ... (2단계의 유효성 검사 로직이 여기에 들어감)
    
    groupBuyService.updateGroupBuy(groupBuyId, requestDto, userPrincipal.user());
    
    // 제공된 URL로 리다이렉트
    return "redirect:" + redirectUrl;
}
```

### 2단계: PRG 패턴 올바르게 구현하기

`POST` 핸들러가 뷰를 직접 반환하지 않도록 리팩토링합니다. 대신, 유효성 검사 실패 시 `RedirectAttributes`를 사용하여 데이터를 전달하고 리다이렉트합니다.

**조치 1: POST 핸들러 수정**

```java
// src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@PostMapping
public String createGroupBuy(@Valid @ModelAttribute("groupBuy") GroupBuyRequestDto requestDto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserPrincipal userPrincipal,
                             RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        // DTO와 bindingResult를 flash 속성으로 추가
        redirectAttributes.addFlashAttribute("groupBuy", requestDto);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.groupBuy", bindingResult);
        // 폼을 위한 GET 엔드포인트로 다시 리다이렉트
        return "redirect:/group-purchases/new";
    }
    GroupBuy groupBuy = groupBuyService.createGroupBuy(userPrincipal.user(), requestDto);
    return "redirect:/group-purchases/" + groupBuy.getId();
}

@PostMapping("/{groupBuyId}/edit")
public String updateGroupBuy(@PathVariable Long groupBuyId,
                             @Valid @ModelAttribute("groupBuy") GroupBuyRequestDto requestDto,
                             BindingResult bindingResult,
                             @RequestParam(defaultValue = "/group-purchases/{groupBuyId}") String redirectUrl,
                             @AuthenticationPrincipal UserPrincipal userPrincipal,
                             RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("groupBuy", requestDto);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.groupBuy", bindingResult);
        redirectAttributes.addAttribute("redirectUrl", redirectUrl); // 리다이렉트 간 redirectUrl 유지
        return "redirect:/group-purchases/" + groupBuyId + "/edit";
    }
    
    groupBuyService.updateGroupBuy(groupBuyId, requestDto, userPrincipal.user());
    return "redirect:" + redirectUrl;
}
```

**조치 2: GET 핸들러가 Flash 속성을 지원하도록 보장**

폼을 위한 GET 핸들러(`/group-purchases/new`, `/group-purchases/{id}/edit`)는 이제 리다이렉트로부터 flash 속성(`groupBuy` 및 `bindingResult`)을 암묵적으로 수신하므로, Thymeleaf는 사용자의 이전 입력 및 유효성 검사 오류와 함께 폼을 렌더링할 수 있습니다. GET 핸들러 자체에는 flash 속성이 없을 경우 빈 폼 객체를 올바르게 설정하는 한, 큰 변경은 필요하지 않습니다.

**예시: 수정 폼을 위한 GET 핸들러**
```java
// src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java

@GetMapping("/{groupBuyId}/edit")
public String editGroupBuyForm(@PathVariable Long groupBuyId, Model model, @RequestParam(required = false) String redirectUrl) {
    // 모델에 리다이렉트로부터 온 'groupBuy' 속성이 포함되어 있지 않다면...
    if (!model.containsAttribute("groupBuy")) {
        GroupBuyRequestDto dto = groupBuyService.findGroupBuyDtoById(groupBuyId);
        model.addAttribute("groupBuy", dto);
    }
    model.addAttribute("redirectUrl", redirectUrl);
    return "group-purchases/form";
}
```

## 4. "뒤로 가기" 및 "목록으로" 버튼에 대한 모범 사례

사용자 경험(UX)에 대한 모범 사례에 대해 문의하셨습니다.

-   **명시적인 `redirectUrl` 매개변수 (권장):** 위에서 설명한 접근 방식이 가장 안정적입니다. 명시적이고 신뢰할 수 있으며, `Referer` 헤더를 제거할 수 있는 브라우저/프록시 구성에 영향을 받지 않습니다.
-   **`Referer` 헤더:** 대안으로 `Referer` HTTP 헤더를 검사하여 이전 페이지를 결정할 수 있습니다. 뷰 템플릿을 수정할 필요가 없어 더 간단할 수 있지만, 신뢰성이 떨어지며 신중하게 처리하지 않으면 보안 위험이 될 수 있습니다.
-   **일관성:** 어떤 방법을 선택하든, 예측 가능한 사용자 경험을 제공하기 위해 모든 폼 제출 및 작업(생성, 수정, 삭제)에 걸쳐 애플리케이션 전반에 일관되게 적용해야 합니다.

이러한 변경 사항을 구현함으로써 즉각적인 UX 불만을 해결하고 중대한 폼 재제출 취약점을 수정할 것입니다.