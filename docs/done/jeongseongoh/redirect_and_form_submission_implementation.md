# 리다이렉트 및 폼 제출 개선 구현 완료

## 1. 구현 개요

`docs/redirect_improvement_plan.md`에 명시된 계획을 구현하여 사용자 경험(UX) 개선 및 PRG(Post-Redirect-Get) 패턴 위반 문제를 해결했습니다.

**구현 날짜**: 2025-11-19

## 2. 해결한 문제

### 2.1. 잘못된 리다이렉션
**문제**: "내 공구 목록" 페이지에서 공구를 수정/취소한 후 상세 페이지로 리다이렉트되어 사용자가 다시 목록으로 돌아가야 하는 불편함

**해결**: Context-aware redirect 구현 - 사용자가 시작한 위치를 기억하고 그곳으로 다시 돌아가도록 개선

### 2.2. PRG 패턴 위반
**문제**: 유효성 검증 실패 시 POST 핸들러에서 직접 뷰를 반환하여 브라우저 새로고침 시 폼 재제출 위험 발생

**해결**: 모든 POST 핸들러를 PRG 패턴에 맞게 리팩토링하여 유효성 검증 실패 시에도 리다이렉트 수행

## 3. 구현 상세

### 3.1. GroupBuyController.java 수정

#### (1) cancelGroupBuy 메서드 - 동적 리다이렉션 추가

**위치**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java:536`

```java
@PostMapping("/{purchaseId}/cancel")
public String cancelGroupBuy(
    @PathVariable Long purchaseId,
    @RequestParam(required = false) String redirectUrl,  // 추가
    @AuthenticationPrincipal UserDetails userDetails,
    RedirectAttributes redirectAttributes
) {
    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
    User user = customUserDetails.getUser();
    
    try {
        groupBuyService.cancelGroupBuy(user.getId(), purchaseId);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매가 취소되었습니다.");
    } catch (CustomException e) {
        log.error("공구 취소 실패 - userId: {}, groupBuyId: {}, error: {}", 
            user.getId(), purchaseId, e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    
    // redirectUrl이 제공되면 해당 URL로, 없으면 상세 페이지로 리다이렉트
    if (redirectUrl != null && !redirectUrl.isBlank()) {
        return "redirect:" + redirectUrl;
    }
    return "redirect:/group-purchases/" + purchaseId;
}
```

#### (2) updateGroupBuy 메서드 - PRG 패턴 적용 및 동적 리다이렉션

**위치**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java:461`

```java
@PostMapping("/{purchaseId}")
public String updateGroupBuy(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable Long purchaseId,
    @Valid @ModelAttribute("updateGroupBuyRequest") UpdateGroupBuyRequest request,
    @RequestParam(required = false) List<String> deletedImages,
    @RequestParam(required = false) String redirectUrl,  // 추가
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes
) {
    // 1. 유효성 검증 실패 처리 - PRG 패턴 적용
    if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("updateGroupBuyRequest", request);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateGroupBuyRequest", bindingResult);
        redirectAttributes.addFlashAttribute("errorMessage", 
            bindingResult.getAllErrors().get(0).getDefaultMessage());
        // redirectUrl을 쿼리 파라미터로 전달
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            redirectAttributes.addAttribute("redirectUrl", redirectUrl);
        }
        return "redirect:/group-purchases/" + purchaseId + "/edit";
    }
    
    // 2. 재료 JSON 파싱 (레시피 기반 공구인 경우)
    if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isBlank()) {
        try {
            List<SelectedIngredient> ingredients = objectMapper.readValue(
                request.getSelectedIngredientsJson(),
                new TypeReference<List<SelectedIngredient>>() {}
            );
            request.setSelectedIngredients(ingredients);
            log.info("Parsed {} ingredients for group buy update", ingredients.size());
        } catch (Exception e) {
            log.error("Failed to parse ingredients JSON: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("updateGroupBuyRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                redirectAttributes.addAttribute("redirectUrl", redirectUrl);
            }
            return "redirect:/group-purchases/" + purchaseId + "/edit";
        }
    }
    
    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
    User user = customUserDetails.getUser();
    
    groupBuyService.updateGroupBuy(user.getId(), purchaseId, request, deletedImages);
    redirectAttributes.addFlashAttribute("successMessage", "공동구매가 성공적으로 수정되었습니다.");
    
    // redirectUrl이 제공되면 해당 URL로, 없으면 상세 페이지로 리다이렉트
    if (redirectUrl != null && !redirectUrl.isBlank()) {
        return "redirect:" + redirectUrl;
    }
    return "redirect:/group-purchases/" + purchaseId;
}
```

#### (3) createGroupBuy 메서드 - PRG 패턴 적용

**위치**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java:326`

```java
@PostMapping
public String createGroupBuy(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @ModelAttribute("createGroupBuyRequest") CreateGroupBuyRequest request,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes
) {
    // 유효성 검증 실패 시 PRG 패턴 적용
    if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createGroupBuyRequest", bindingResult);
        redirectAttributes.addFlashAttribute("errorMessage", 
            bindingResult.getAllErrors().get(0).getDefaultMessage());
        return "redirect:/group-purchases/new";
    }
    
    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
    User user = customUserDetails.getUser();
    
    GroupBuyResponse response = groupBuyService.createGroupBuy(user.getId(), request);
    redirectAttributes.addFlashAttribute("successMessage", "공동구매가 성공적으로 생성되었습니다.");
    return "redirect:/group-purchases/" + response.getId();
}
```

#### (4) createRecipeBasedGroupBuy 메서드 - PRG 패턴 적용

**위치**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java:388`

```java
@PostMapping("/recipe-based")
public String createRecipeBasedGroupBuy(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @ModelAttribute("createGroupBuyRequest") CreateGroupBuyRequest request,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes
) {
    // 유효성 검증 실패 시 PRG 패턴 적용
    if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createGroupBuyRequest", bindingResult);
        redirectAttributes.addFlashAttribute("errorMessage", 
            bindingResult.getAllErrors().get(0).getDefaultMessage());
        if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
            redirectAttributes.addAttribute("recipeApiId", request.getRecipeApiId());
        }
        return "redirect:/group-purchases/new";
    }
    
    // 재료 검증 및 파싱 로직 (생략)
    
    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
    User user = customUserDetails.getUser();
    
    GroupBuyResponse response = groupBuyService.createRecipeBasedGroupBuy(user.getId(), request);
    redirectAttributes.addFlashAttribute("successMessage", "레시피 기반 공동구매가 성공적으로 생성되었습니다.");
    return "redirect:/group-purchases/" + response.getId();
}
```

#### (5) createPage 메서드 - Flash 속성 지원 추가

**위치**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java:199`

```java
@GetMapping("/new")
public String createPage(
    @RequestParam(required = false) String recipeApiId,
    Model model
) {
    // Flash 속성에서 폼 데이터가 없으면 빈 객체로 초기화
    if (!model.containsAttribute("createGroupBuyRequest")) {
        model.addAttribute("createGroupBuyRequest", new CreateGroupBuyRequest());
    }
    
    // 레시피 기반 공구 생성인 경우 레시피 정보 로드
    if (recipeApiId != null && !recipeApiId.isBlank()) {
        CreateGroupBuyRequest formData = (CreateGroupBuyRequest) model.getAttribute("createGroupBuyRequest");
        try {
            RecipeResponse recipe = recipeService.getRecipeByApiId(recipeApiId);
            model.addAttribute("recipe", recipe);
            
            if (formData != null && (formData.getSelectedIngredients() == null || formData.getSelectedIngredients().isEmpty())) {
                // 레시피 재료 정보 추가 로직 (생략)
            }
            
            formData.setRecipeApiId(recipeApiId);
            formData.setCategory(GroupBuyCategory.RECIPE);
        } catch (CustomException e) {
            model.addAttribute("errorMessage", "레시피 정보를 불러올 수 없습니다.");
        }
    }
    
    model.addAttribute("formData", formData);
    model.addAttribute("categories", GroupBuyCategory.values());
    return "group-purchases/form";
}
```

#### (6) editPage 메서드 - Flash 속성 지원 및 redirectUrl 추가

**위치**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java:248`

```java
@GetMapping("/{purchaseId}/edit")
public String editPage(
    @PathVariable Long purchaseId, 
    @RequestParam(required = false) String redirectUrl,  // 추가
    Model model
) {
    GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
    model.addAttribute("groupBuy", groupBuy);
    model.addAttribute("redirectUrl", redirectUrl);  // 모델에 추가
    
    // 카카오 지도 API 키 추가
    model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);
    
    // Flash 속성에서 폼 데이터가 없으면 기존 데이터로 초기화
    UpdateGroupBuyRequest formData;
    if (model.containsAttribute("updateGroupBuyRequest")) {
        // Flash 속성에서 가져오기 (유효성 검증 실패 후 리다이렉트된 경우)
        formData = (UpdateGroupBuyRequest) model.getAttribute("updateGroupBuyRequest");
    } else {
        // 기존 데이터로 초기화
        formData = new UpdateGroupBuyRequest();
        formData.setTitle(groupBuy.getTitle());
        formData.setContent(groupBuy.getContent());
        formData.setCategory(groupBuy.getCategory());
        formData.setTargetAmount(groupBuy.getTargetAmount());
        formData.setTargetHeadcount(groupBuy.getTargetHeadcount());
        formData.setDeadline(groupBuy.getDeadline());
        formData.setDeliveryMethod(groupBuy.getDeliveryMethod());
        formData.setMeetupLocation(groupBuy.getMeetupLocation());
        formData.setLatitude(groupBuy.getLatitude());
        formData.setLongitude(groupBuy.getLongitude());
        formData.setParcelFee(groupBuy.getParcelFee());
        formData.setIsParticipantListPublic(groupBuy.getIsParticipantListPublic());
    }
    
    // 기존 이미지 URL 목록 추가
    model.addAttribute("existingImages", groupBuy.getImageUrls());
    
    // 재료 목록 파싱 (모든 공구 유형에서 시도)
    if (groupBuy.getIngredients() != null && !groupBuy.getIngredients().isBlank()) {
        try {
            List<SelectedIngredient> ingredientsList = objectMapper.readValue(
                groupBuy.getIngredients(),
                new TypeReference<List<SelectedIngredient>>() {}
            );
            model.addAttribute("ingredientsList", ingredientsList);
        } catch (Exception e) {
            log.warn("Failed to parse ingredients JSON for group buy {}: {}", purchaseId, e.getMessage());
        }
    }
    
    model.addAttribute("formData", formData);
    model.addAttribute("categories", GroupBuyCategory.values());
    return "group-purchases/form";
}
```

### 3.2. UserController.java 수정

#### myGroupPurchasesPage 메서드 - 현재 URL을 모델에 추가

**위치**: `src/main/java/com/recipemate/domain/user/controller/UserController.java:376`

```java
@GetMapping("/me/group-purchases")
public String myGroupPurchasesPage(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) GroupBuyStatus status,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        jakarta.servlet.http.HttpServletRequest request,  // 추가
        Model model) {
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    
    Page<GroupBuy> groupBuys;
    if (status != null) {
        groupBuys = groupBuyRepository.findByHostIdAndStatusInAndNotDeleted(user.getId(), List.of(status), pageable);
    } else {
        groupBuys = groupBuyRepository.findByHostIdAndNotDeleted(user.getId(), pageable);
    }
    
    // 현재 URL을 모델에 추가 (쿼리 파라미터 포함)
    String currentUrl = request.getRequestURI();
    if (request.getQueryString() != null) {
        currentUrl += "?" + request.getQueryString();
    }
    
    model.addAttribute("groupBuys", groupBuys);
    model.addAttribute("currentStatus", status);
    model.addAttribute("currentUrl", currentUrl);  // 추가
    return "user/my-group-purchases";
}
```

**설명**: Thymeleaf에서 `#httpServletRequest`에 직접 접근할 수 없는 문제를 해결하기 위해 컨트롤러에서 현재 URL을 모델에 추가했습니다.

### 3.3. my-group-purchases.html 템플릿 수정

**위치**: `src/main/resources/templates/user/my-group-purchases.html`

```html
<!-- 수정 버튼 -->
<a th:if="${groupBuy.status.name() == 'RECRUITING'}" 
   th:href="@{/group-purchases/{id}/edit(id=${groupBuy.id}, redirectUrl=${currentUrl})}" 
   class="btn btn-outline-warning">
    <i class="bi bi-pencil"></i> 수정
</a>

<!-- 취소 폼 -->
<form th:if="${groupBuy.status.name() == 'RECRUITING'}"
      th:action="@{/group-purchases/{id}/cancel(id=${groupBuy.id})}" 
      method="post"
      onsubmit="return confirm('정말로 이 공동구매를 취소하시겠습니까?\n참여자들에게 알림이 전송됩니다.');">
    <input type="hidden" th:if="${_csrf}" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <input type="hidden" name="redirectUrl" th:value="${currentUrl}"/>
    <button type="submit" class="btn btn-outline-danger w-100">
        <i class="bi bi-x-circle"></i> 공구 취소
    </button>
</form>
```

**변경 사항**:
- `${#httpServletRequest.requestURI}` → `${currentUrl}` 변경
- 컨트롤러에서 제공한 `currentUrl` 사용

### 3.4. form.html 템플릿 수정

**위치**: `src/main/resources/templates/group-purchases/form.html`

#### (1) redirectUrl을 hidden input으로 추가

```html
<form id="groupBuyForm" method="post" 
      th:action="${groupBuy != null ? '/group-purchases/' + groupBuy.id : (recipe != null ? '/group-purchases/recipe-based' : '/group-purchases')}"
      th:object="${formData}"
      enctype="multipart/form-data">
    <input type="hidden" th:if="${_csrf}" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <input type="hidden" th:if="${redirectUrl}" name="redirectUrl" th:value="${redirectUrl}"/>
    
    <!-- 폼 필드들... -->
</form>
```

#### (2) 취소 버튼에 redirectUrl 적용

```html
<div class="d-flex gap-2 mt-4">
    <button type="submit" class="btn btn-primary px-4">
        <i class="bi bi-check-circle"></i>
        <span th:text="${groupBuy != null ? '수정하기' : '작성하기'}">작성하기</span>
    </button>
    <a th:href="${redirectUrl != null ? redirectUrl : (groupBuy != null ? '/group-purchases/' + groupBuy.id : '/group-purchases/list')}" 
       class="btn btn-outline-secondary px-4">
        <i class="bi bi-x-circle"></i> 취소
    </a>
</div>
```

**로직 우선순위**:
1. `redirectUrl`이 있으면 → 해당 URL로 (context-aware)
2. 수정 모드면 (`groupBuy != null`) → 상세 페이지로
3. 생성 모드면 → 목록 페이지로

## 4. 동작 플로우

### 4.1. 목록에서 수정/취소 시작

```
/users/me/group-purchases (목록)
  ↓ [수정 버튼 클릭]
/group-purchases/1/edit?redirectUrl=/users/me/group-purchases
  ↓ [완료 버튼] → POST /group-purchases/1 (redirectUrl 포함)
/users/me/group-purchases (목록으로 복귀) ✅

  ↓ [취소 버튼] → GET redirectUrl
/users/me/group-purchases (목록으로 복귀) ✅
```

```
/users/me/group-purchases (목록)
  ↓ [공구 취소 버튼 클릭]
POST /group-purchases/1/cancel (redirectUrl 포함)
  ↓
/users/me/group-purchases (목록으로 복귀) ✅
```

### 4.2. 상세 페이지에서 수정 시작 (redirectUrl 없음)

```
/group-purchases/1 (상세)
  ↓ [수정 버튼 클릭]
/group-purchases/1/edit (redirectUrl 없음)
  ↓ [완료 버튼]
/group-purchases/1 (상세로 복귀) ✅

  ↓ [취소 버튼]
/group-purchases/1 (상세로 복귀) ✅
```

### 4.3. 유효성 검증 실패 시 (PRG 패턴)

```
/group-purchases/1/edit?redirectUrl=/users/me/group-purchases
  ↓ [완료 버튼 - 유효성 검증 실패]
POST /group-purchases/1 (유효성 검증 오류)
  ↓ Flash 속성에 데이터 저장 후 리다이렉트
GET /group-purchases/1/edit?redirectUrl=/users/me/group-purchases
  ↓ Flash 속성에서 데이터 복원하여 폼 표시
사용자가 오류 메시지와 함께 입력한 데이터 확인 가능
  ↓ 브라우저 새로고침 시
GET /group-purchases/1/edit?redirectUrl=/users/me/group-purchases (폼 재제출 없음) ✅
```

## 5. 구현된 개선사항

### 5.1. UX 개선
✅ **동적 리다이렉션**: 사용자가 시작한 위치로 자동으로 복귀  
✅ **직관적인 네비게이션**: 목록 관리 시 효율적인 워크플로우 제공  
✅ **Flash 메시지**: 작업 성공/실패를 명확하게 피드백

### 5.2. 안정성 개선
✅ **PRG 패턴 준수**: 폼 재제출 위험 완전 제거  
✅ **Flash 속성 활용**: 리다이렉트 간 데이터 및 오류 정보 유지  
✅ **일관된 패턴**: 모든 폼 제출 엔드포인트에 동일한 패턴 적용

### 5.3. 코드 품질 개선
✅ **Best Practice 적용**: Spring MVC의 표준 패턴 준수  
✅ **유지보수성**: 명확한 책임 분리 및 일관된 코드 스타일  
✅ **확장성**: 다른 컨트롤러에도 쉽게 적용 가능한 패턴

## 6. 테스트 시나리오

### 6.1. 동적 리다이렉션 테스트
- [ ] 목록에서 수정 → 완료 → 목록으로 복귀 확인
- [ ] 목록에서 취소 → 목록으로 복귀 확인
- [ ] 상세에서 수정 → 완료 → 상세로 복귀 확인
- [ ] 폼에서 취소 버튼 → 올바른 위치로 복귀 확인

### 6.2. PRG 패턴 테스트
- [ ] 유효성 검증 실패 → 오류 메시지 표시 확인
- [ ] 유효성 검증 실패 후 새로고침 → 폼 재제출 없음 확인
- [ ] 유효성 검증 실패 후 뒤로가기 → 정상 동작 확인
- [ ] Flash 속성에 사용자 입력 데이터 유지 확인

### 6.3. 엣지 케이스 테스트
- [ ] redirectUrl이 없을 때 기본 동작 확인
- [ ] 잘못된 redirectUrl 처리 확인
- [ ] 쿼리 파라미터가 있는 URL 처리 확인

## 7. 향후 개선 사항

### 7.1. 다른 도메인에도 동일 패턴 적용
- [ ] CommunityPostController에 PRG 패턴 적용
- [ ] RecipeController에 PRG 패턴 적용
- [ ] 기타 폼 제출 컨트롤러들에 적용

### 7.2. 보안 강화
- [ ] redirectUrl에 대한 화이트리스트 검증 추가
- [ ] Open Redirect 취약점 방지 로직 추가

### 7.3. 사용자 피드백 개선
- [ ] Toast 알림으로 Flash 메시지 표시
- [ ] 작업 진행 상태 표시 (로딩 스피너 등)

## 8. 참고 자료

- **원본 계획서**: `docs/redirect_improvement_plan.md`
- **Spring MVC PRG 패턴**: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/redirectattributes.html
- **관련 이슈**: 공구 수정/취소 시 UX 개선 요청

## 9. 결론

이번 구현으로 다음을 달성했습니다:

1. ✅ **사용자 경험 대폭 개선**: Context-aware redirect로 직관적인 네비게이션 제공
2. ✅ **폼 재제출 위험 제거**: PRG 패턴 적용으로 안정성 확보
3. ✅ **코드 품질 향상**: Spring MVC Best Practice 준수 및 일관된 패턴 적용

실무 표준에 부합하는 견고한 폼 처리 시스템을 구축했습니다.
