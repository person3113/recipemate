# 사용자 레시피 수정/삭제 버튼 미표시 버그 수정

## 작업 일자
2025년 11월 15일

## 문제 상황

사용자가 직접 작성한 레시피 상세 페이지에서 **수정하기/삭제하기 버튼이 표시되지 않음**

### 증상
- 사용자가 본인이 작성한 레시피를 조회해도 수정/삭제 버튼이 안 보임
- "내가 작성한 레시피입니다" 알림도 표시되지 않음

## 원인 분석

### 문제의 근본 원인

`RecipeController.java`의 `recipeDetailPage()` 메서드에서 `isOwner` 변수를 설정할 때:

```java
// 문제가 있던 코드
if (userDetails != null && "user".equalsIgnoreCase(recipe.getSource())) {
    // isOwner 체크 로직
}
```

**하지만**, 이전에 사용자 레시피의 `source` 표시를 개선하면서:
- Before: `"user"`
- After: `"홍길동 (사용자)"` (닉네임 포함)

따라서 `"user".equalsIgnoreCase(recipe.getSource())`가 항상 `false`가 되어 isOwner가 설정되지 않음!

### 영향받은 부분

1. **레시피 상세 페이지** (`recipeDetailPage()`)
   - `isOwner` 변수가 항상 `false`
   - 수정/삭제 버튼이 표시되지 않음

2. **레시피 수정 폼** (`editRecipeForm()`)
   - 사용자 레시피 체크가 실패
   - "API로 가져온 레시피는 수정할 수 없습니다" 에러 발생

---

## 해결 방법

### ⚠️ 첫 번째 시도의 문제점

**초기 수정안:**
```java
if (userDetails != null && recipe.getSource() != null && recipe.getSource().contains("(사용자)")) {
    // isOwner 체크
}
```

**보안 문제:**
- `contains("(사용자)")`만 체크하면 **다른 사용자의 레시피도 수정 가능**
- 예: A가 작성한 레시피 → B도 `contains("(사용자)") = true`이므로 접근 가능
- **권한 검증이 불충분함!**

### ✅ 최종 해결 방법

**source 체크를 제거하고 `Recipe.canModify()` 메서드만 사용:**

```java
// source 체크 불필요 - canModify()가 모든 것을 검증
if (userDetails != null && recipeId.matches("\\d+")) {
    User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    if (currentUser != null) {
        Recipe recipeEntity = recipeRepository.findById(dbId).orElse(null);
        if (recipeEntity != null) {
            isOwner = recipeEntity.canModify(currentUser); // ← 여기서 모든 검증
        }
    }
}
```

**Recipe.canModify() 내부 로직:**
```java
public boolean canModify(User user) {
    if (user == null) {
        return false;
    }
    // 3가지 조건 모두 만족해야 true
    return isUserRecipe()                              // 1. 사용자 레시피인가?
        && author != null                              // 2. 작성자가 있는가?
        && author.getId().equals(user.getId());        // 3. 본인이 작성했는가?
}
```

**장점:**
- ✅ **완전한 권한 검증**: 본인이 작성한 레시피만 수정 가능
- ✅ **코드 중복 제거**: source 체크 로직 불필요
- ✅ **일관성**: 엔티티 레벨에서 권한 관리

---

### 1️⃣ RecipeController.java - recipeDetailPage()

**변경 내용:**
```java
// Before (잘못된 첫 시도)
if (userDetails != null && recipe.getSource() != null && recipe.getSource().contains("(사용자)")) {
    // ... isOwner 체크
}

// After (최종)
if (userDetails != null && recipeId.matches("\\d+")) {
    User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    if (currentUser != null) {
        Recipe recipeEntity = recipeRepository.findById(dbId).orElse(null);
        if (recipeEntity != null) {
            isOwner = recipeEntity.canModify(currentUser);
        }
    }
}
```

### 2️⃣ RecipeController.java - editRecipeForm()

**변경 내용:**
```java
// Before (중복된 검증)
RecipeDetailResponse recipeDetail = recipeService.getRecipeDetailById(id);
if (recipeDetail.getSource() == null || !recipeDetail.getSource().contains("(사용자)")) {
    // 에러
}
Recipe recipeEntity = recipeRepository.findById(id)...
if (!recipeEntity.canModify(currentUser)) {
    // 에러
}

// After (단일 검증)
Recipe recipeEntity = recipeRepository.findById(id)...
if (!recipeEntity.canModify(currentUser)) {
    redirectAttributes.addFlashAttribute("error", "이 레시피를 수정할 권한이 없습니다.");
    return "redirect:/recipes/" + id;
}
RecipeDetailResponse recipeDetail = recipeService.getRecipeDetailById(id);
```

---

## 수정된 코드

### RecipeController.java - recipeDetailPage()

```java
@GetMapping("/{recipeId}")
public String recipeDetailPage(
        @PathVariable String recipeId,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
    
    RecipeDetailResponse recipe;
    
    // recipeId가 순수 숫자인지 확인 (DB ID)
    if (recipeId.matches("\\d+")) {
        recipe = recipeService.getRecipeDetailById(Long.parseLong(recipeId));
    } else {
        recipe = recipeService.getRecipeDetailByApiId(recipeId);
    }
    
    model.addAttribute("recipe", recipe);
    
    // ✅ 본인이 작성한 레시피인지 확인 (최종 버전)
    boolean isOwner = false;
    if (userDetails != null && recipeId.matches("\\d+")) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            
            if (currentUser != null) {
                Long dbId = Long.parseLong(recipeId);
                Recipe recipeEntity = recipeRepository.findById(dbId).orElse(null);
                if (recipeEntity != null) {
                    // Recipe 엔티티에서 직접 권한 확인
                    isOwner = recipeEntity.canModify(currentUser);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check recipe ownership", e);
        }
    }
    model.addAttribute("isOwner", isOwner);
    
    // 관련 공동구매 조회
    List<GroupBuyResponse> relatedGroupBuys = recipeService.getRelatedGroupBuys(recipe.getId());
    model.addAttribute("relatedGroupBuys", relatedGroupBuys);
    
    return "recipes/detail";
}
```

### RecipeController.java - editRecipeForm()

```java
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
    
    if (userDetails == null) {
        redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
        return "redirect:/auth/login";
    }

    try {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // DB에서 레시피 엔티티 조회
        Recipe recipeEntity = recipeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        // ✅ 권한 체크: canModify()로 통합 확인
        if (!recipeEntity.canModify(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "이 레시피를 수정할 권한이 없습니다.");
            return "redirect:/recipes/" + id;
        }

        // RecipeDetailResponse로 조회 (화면 표시용)
        RecipeDetailResponse recipeDetail = recipeService.getRecipeDetailById(id);
        
        // ... 폼 데이터 준비 ...
        
        return "recipes/form";
    } catch (CustomException e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/recipes/" + id;
    }
}
```

### Recipe.java - canModify()

```java
/**
 * 특정 사용자가 이 레시피를 수정할 수 있는지 확인
 */
public boolean canModify(User user) {
    if (user == null) {
        return false;
    }
    // 3가지 조건을 모두 만족해야 true
    return isUserRecipe()                          // 사용자 레시피인가?
        && author != null                          // 작성자가 있는가?
        && author.getId().equals(user.getId());    // 본인이 작성했는가?
}

/**
 * 사용자가 작성한 레시피인지 확인
 */
public boolean isUserRecipe() {
    return this.sourceApi == RecipeSource.USER;
}
```

---

## 테스트 체크리스트

### 기본 기능 테스트
- [ ] 사용자가 작성한 레시피 상세 페이지에서 수정/삭제 버튼이 표시되는지 확인
- [ ] "내가 작성한 레시피입니다" 알림이 표시되는지 확인
- [ ] 수정하기 버튼 클릭 시 정상적으로 수정 폼으로 이동하는지 확인
- [ ] 삭제하기 버튼 클릭 시 확인 메시지가 표시되고 삭제되는지 확인

### 보안 테스트 (중요!)
- [ ] **다른 사용자가 작성한 레시피에는 버튼이 표시되지 않는지 확인**
- [ ] 다른 사용자의 레시피 수정 URL로 직접 접근 시 에러 메시지 표시 확인
  - 예: A가 작성한 레시피(ID: 123)를 B가 `/recipes/123/edit` 접근 시
  - 예상: "이 레시피를 수정할 권한이 없습니다" 에러
- [ ] API 레시피(TheMealDB, 식품안전나라)에는 버튼이 표시되지 않는지 확인
- [ ] 로그인하지 않은 상태에서는 버튼이 표시되지 않는지 확인

---

## 관련 작업

이 버그는 다음 작업의 영향을 받음:
- **사용자 레시피 출처 표시 개선** (`recipe_user_source_display_improvement.md`)
  - 사용자 레시피의 source를 "user" → "닉네임 (사용자)"로 변경
  - 이 변경으로 인해 기존의 `"user"` 문자열 비교가 실패하게 됨

---

## 완료 ✅

사용자가 작성한 레시피에서 이제 정상적으로 수정하기/삭제하기 버튼이 표시되며, **본인이 작성한 레시피만** 수정/삭제 가능합니다!
