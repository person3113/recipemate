# 🔒 레시피 수정/삭제 권한 체크 추가

**문제 발생일**: 2025년 11월 13일  
**증상**: 다른 사용자가 작성한 레시피를 수정/삭제할 수 있는 심각한 보안 문제

---

## 🚨 문제 원인

### 증상
다른 사용자로 로그인해도 타인이 작성한 레시피를 수정/삭제할 수 있었습니다.

### 원인 분석

**파일**: `RecipeController.java`의 `editRecipeForm` 메서드

```java
// 문제가 있던 코드
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id, ...) {
    // 로그인 체크만 있고, 권한 체크가 없음!
    RecipeDetailResponse recipeDetail = recipeService.getRecipeDetailById(id);
    
    // ❌ 권한 체크 누락!
    // 주석만 있고 실제 체크는 없었음: "권한 체크는 서비스 레이어에서 처리됨"
    
    model.addAttribute("recipe", recipe);
    return "recipes/form";
}
```

**문제점**:
1. Controller에서 권한 체크를 하지 않음
2. Service의 `updateUserRecipe`에는 권한 체크가 있지만, 수정 폼 페이지(`editRecipeForm`)에는 없음
3. 누구나 수정 폼에 접근하여 타인의 레시피를 볼 수 있고, 수정 시도 가능

**보안 위험**:
- 타인의 레시피 내용을 볼 수 있음
- 수정 폼에서 데이터를 변경하고 제출하면 일부는 Service에서 막히지만, 이미 데이터가 노출됨

---

## ✅ 해결 방법

### 1. Controller에 권한 체크 추가

**파일**: `RecipeController.java`

#### editRecipeForm 메서드 수정

```java
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
    // ...
    
    try {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 레시피 조회
        RecipeDetailResponse recipeDetail = recipeService.getRecipeDetailById(id);
        
        // ✅ 1단계: 사용자 레시피인지 확인
        if (!"user".equalsIgnoreCase(recipeDetail.getSource())) {
            redirectAttributes.addFlashAttribute("error", "API로 가져온 레시피는 수정할 수 없습니다.");
            return "redirect:/recipes/" + id;
        }
        
        // ✅ 2단계: 본인이 작성한 레시피인지 확인
        Recipe recipeEntity = recipeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
                
        if (!recipeEntity.canModify(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "이 레시피를 수정할 권한이 없습니다.");
            return "redirect:/recipes/" + id;
        }

        // 권한 확인 후 수정 폼 표시
        // ...
    }
}
```

### 2. RecipeRepository 의존성 추가

```java
@Controller
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    private final RecipeSyncService recipeSyncService;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;  // ✅ 추가
}
```

### 3. Recipe import 추가

```java
import com.recipemate.domain.recipe.entity.Recipe;
```

---

## 🔐 보안 계층

### 다층 방어 (Defense in Depth)

이제 권한 체크가 **2개 레이어**에서 이루어집니다:

#### 1단계: Controller (editRecipeForm)
- **목적**: 수정 폼 접근 제어
- **체크 내용**:
  1. 로그인 여부
  2. 사용자 레시피인지 (API 레시피는 수정 불가)
  3. 본인이 작성한 레시피인지
- **실패 시**: 상세 페이지로 리다이렉트 + 에러 메시지

#### 2단계: Service (updateUserRecipe, deleteUserRecipe)
- **목적**: 실제 수정/삭제 작업 제어
- **체크 내용**:
  1. 레시피 존재 여부
  2. `recipe.canModify(currentUser)` - 본인 확인
- **실패 시**: `CustomException(ErrorCode.UNAUTHORIZED)` 발생

---

## 🎯 권한 체크 로직

### Recipe.canModify() 메서드

**파일**: `Recipe.java`

```java
public boolean canModify(User user) {
    if (user == null) {
        return false;
    }
    // USER 타입 레시피이고, author가 현재 사용자와 일치하는지 확인
    return isUserRecipe() && author != null && author.getId().equals(user.getId());
}

public boolean isUserRecipe() {
    return this.sourceApi == RecipeSource.USER;
}
```

### 권한 체크 흐름

```
사용자 → GET /recipes/{id}/edit 요청
    ↓
1. 로그인 확인 (UserDetails != null)
    ↓
2. User 조회 (currentUser)
    ↓
3. 레시피 조회 (RecipeDetailResponse)
    ↓
4. source == "user" 확인 ✅
    ↓
5. Recipe 엔티티 조회
    ↓
6. recipe.canModify(currentUser) ✅
    ↓
7. 통과 → 수정 폼 표시
```

---

## ✅ 테스트 시나리오

### 1. 본인 레시피 수정 (정상 케이스)
1. 사용자 A로 로그인
2. 사용자 A가 작성한 레시피 상세 페이지 접속
3. "수정하기" 버튼 클릭
4. ✅ 수정 폼 정상 표시
5. 수정 후 저장
6. ✅ 정상 저장

### 2. 타인 레시피 수정 시도 (차단)
1. 사용자 A로 로그인
2. 사용자 B가 작성한 레시피 상세 페이지 접속
3. URL을 직접 입력하여 수정 폼 접근 시도: `/recipes/{id}/edit`
4. ❌ "이 레시피를 수정할 권한이 없습니다." 메시지
5. ✅ 상세 페이지로 리다이렉트

### 3. API 레시피 수정 시도 (차단)
1. 사용자 A로 로그인
2. API로 가져온 레시피 상세 페이지 접속
3. URL을 직접 입력하여 수정 폼 접근 시도
4. ❌ "API로 가져온 레시피는 수정할 수 없습니다." 메시지
5. ✅ 상세 페이지로 리다이렉트

### 4. 비로그인 사용자 (차단)
1. 로그아웃 상태
2. URL을 직접 입력하여 수정 폼 접근 시도
3. ❌ "로그인이 필요합니다." 메시지
4. ✅ 로그인 페이지로 리다이렉트

---

## 📊 수정 파일 요약

### 수정된 파일 (1개)

**RecipeController.java**
- `editRecipeForm()` 메서드에 권한 체크 로직 추가
- `RecipeRepository` 의존성 추가
- `Recipe` import 추가

### 변경 사항
- **추가**: 2단계 권한 체크 (source 확인 + canModify)
- **추가**: RecipeRepository 의존성
- **개선**: 보안 강화 (다층 방어)

---

## 🎉 결과

**보안 문제 해결!** ✅

이제:
1. ✅ 본인이 작성한 레시피만 수정/삭제 가능
2. ✅ 타인의 레시피는 수정 폼 접근 불가
3. ✅ API 레시피는 수정 불가
4. ✅ 비로그인 시 접근 불가
5. ✅ 에러 메시지로 명확한 피드백

---

## 🔍 추가 보안 권장사항

### 1. 프론트엔드 버튼 숨김 (이미 구현됨)

**파일**: `recipes/detail.html`

```html
<!-- 사용자가 작성한 레시피인 경우 수정/삭제 버튼 표시 -->
<div th:if="${recipe.source == 'user'}" sec:authorize="isAuthenticated()">
    <!-- 본인만 보이도록 추가 체크 필요 -->
</div>
```

현재는 모든 로그인 사용자에게 버튼이 보이지만, URL 직접 접근은 차단됩니다.

### 2. Spring Security Method Security (선택사항)

더 강력한 보안을 원한다면:

```java
@PreAuthorize("@recipeService.canModifyRecipe(#id, principal)")
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id, ...) {
    // ...
}
```

---

## 💡 교훈

### 보안 원칙

1. **다층 방어 (Defense in Depth)**
   - Controller와 Service 모두에서 권한 체크
   - 한 곳이 뚫려도 다른 곳에서 막을 수 있음

2. **프론트엔드는 신뢰하지 않기**
   - 버튼을 숨기는 것만으로는 부족
   - 백엔드에서 반드시 권한 체크

3. **명시적 권한 체크**
   - "당연히 될 거야"라고 가정하지 말기
   - 모든 민감한 작업에 권한 체크 추가

4. **테스트 철저히**
   - 다른 사용자 계정으로 테스트
   - URL 직접 입력 테스트
   - 비로그인 상태 테스트

---

**수정 완료 날짜**: 2025년 11월 13일  
**상태**: ✅ 해결됨  
**보안 등급**: 🔒 안전

