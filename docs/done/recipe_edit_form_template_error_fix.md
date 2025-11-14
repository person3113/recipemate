# 🐛 레시피 수정 폼 템플릿 파싱 오류 수정

**문제 발생일**: 2025년 11월 13일  
**증상**: 레시피 수정 버튼 클릭 시 "Internal Server Error - template parsing error" 발생

---

## 🔍 문제 원인

### 오류 메시지
```
500 Internal Server Error
An error happened during template parsing 
(template: "class path resource [templates/recipes/form.html]")
```

### 원인 분석

**파일**: `RecipeController.java`의 `editRecipeForm` 메서드

```java
// 문제가 있던 코드
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id, ...) {
    RecipeDetailResponse recipe = recipeService.getRecipeDetailById(id);
    
    model.addAttribute("recipe", recipe);  // ❌ 잘못된 타입!
    model.addAttribute("isEdit", true);
    // ...
}
```

**문제점**:
1. 템플릿은 `RecipeUpdateRequest` 객체를 기대하지만, `RecipeDetailResponse`를 전달했습니다
2. `RecipeDetailResponse`에는 `th:field="*{title}"` 같은 바인딩에 필요한 setter가 없습니다
3. 재료 목록의 타입이 달라서 Thymeleaf가 처리할 수 없었습니다
   - `RecipeDetailResponse.IngredientInfo` vs `RecipeUpdateRequest.IngredientDto`

**결과**:
- Thymeleaf가 템플릿을 파싱하는 과정에서 타입 불일치로 오류 발생
- 500 Internal Server Error 반환

---

## ✅ 해결 방법

### 1. Controller 수정

**파일**: `RecipeController.java`

```java
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
    // ...
    
    RecipeDetailResponse recipeDetail = recipeService.getRecipeDetailById(id);

    // ✅ RecipeUpdateRequest 객체 생성 및 데이터 변환
    RecipeUpdateRequest recipe = new RecipeUpdateRequest();
    recipe.setTitle(recipeDetail.getName());
    recipe.setCategory(recipeDetail.getCategory());
    recipe.setArea(recipeDetail.getArea());
    recipe.setInstructions(recipeDetail.getInstructions());
    recipe.setYoutubeUrl(recipeDetail.getYoutubeUrl());
    recipe.setSourceUrl(recipeDetail.getSourceUrl());
    recipe.setExistingMainImageUrl(recipeDetail.getImageUrl());
    
    // ✅ 재료 변환
    if (recipeDetail.getIngredients() != null) {
        List<RecipeUpdateRequest.IngredientDto> ingredients = new ArrayList<>();
        for (RecipeDetailResponse.IngredientInfo ing : recipeDetail.getIngredients()) {
            RecipeUpdateRequest.IngredientDto ingredientDto = 
                new RecipeUpdateRequest.IngredientDto();
            ingredientDto.setName(ing.getName());
            ingredientDto.setMeasure(ing.getMeasure());
            ingredients.add(ingredientDto);
        }
        recipe.setIngredients(ingredients);
    }

    model.addAttribute("recipe", recipe);  // ✅ 올바른 타입!
    model.addAttribute("isEdit", true);
    model.addAttribute("recipeId", id);
    
    return "recipes/form";
}
```

### 2. 템플릿 수정

**파일**: `templates/recipes/form.html`

#### 재료 입력 부분 수정

```html
<!-- 수정 모드: 기존 재료 표시 -->
<div th:if="${isEdit and recipe.ingredients != null and !recipe.ingredients.isEmpty()}">
    <div th:each="ingredient, iterStat : ${recipe.ingredients}" class="ingredient-item mb-2">
        <div class="row">
            <div class="col-md-5">
                <input type="text" 
                       class="form-control" 
                       th:name="|ingredients[${iterStat.index}].name|" 
                       th:value="${ingredient.name}"
                       placeholder="재료명" 
                       required>
            </div>
            <div class="col-md-5">
                <input type="text" 
                       class="form-control" 
                       th:name="|ingredients[${iterStat.index}].measure|" 
                       th:value="${ingredient.measure}"
                       placeholder="분량" 
                       required>
            </div>
            <div class="col-md-2">
                <button type="button" class="btn btn-danger" onclick="removeIngredient(this)">
                    <i class="bi bi-trash"></i>
                </button>
            </div>
        </div>
    </div>
</div>

<!-- 작성 모드: 빈 재료 입력 폼 -->
<div th:if="${!isEdit or recipe.ingredients == null or recipe.ingredients.isEmpty()}" 
     class="ingredient-item mb-2">
    <!-- ... 빈 폼 -->
</div>
```

#### JavaScript 초기화 수정

```javascript
// ✅ 재료 카운터를 페이지 로드 시 현재 재료 개수로 초기화
let ingredientCount = document.querySelectorAll('.ingredient-item').length;

// 재료 추가 시 올바른 인덱스 사용
function addIngredient() {
    // ...
    newIngredient.innerHTML = `
        <input type="text" name="ingredients[${ingredientCount}].name" ...>
        <input type="text" name="ingredients[${ingredientCount}].measure" ...>
    `;
    ingredientCount++;
}
```

---

## 🎯 동작 원리

### DTO 변환 흐름

```
사용자 레시피 수정 요청
    ↓
1. getRecipeDetailById(id) 
    → RecipeDetailResponse (조회용 DTO)
    ↓
2. RecipeDetailResponse → RecipeUpdateRequest 변환
    - 필드 매핑
    - 재료 리스트 변환
    ↓
3. Model에 RecipeUpdateRequest 추가
    ↓
4. Thymeleaf 템플릿 렌더링 성공 ✅
```

### 재료 표시 방식

| 모드 | 조건 | 표시 내용 |
|------|------|----------|
| 작성 | `!isEdit` | 빈 재료 입력 폼 1개 |
| 수정 | `isEdit` + 재료 있음 | 기존 재료 모두 표시 (th:each) |
| 수정 | `isEdit` + 재료 없음 | 빈 재료 입력 폼 1개 |

---

## ✅ 테스트 시나리오

### 1. 레시피 수정 폼 접근
1. 사용자 레시피 상세 페이지 접속
2. "수정하기" 버튼 클릭
3. ✅ 수정 폼 정상 표시
4. ✅ 기존 데이터 자동 로드됨

### 2. 기존 데이터 확인
1. 제목, 카테고리, 지역 등이 채워져 있는지 확인
2. ✅ 조리 방법 textarea에 기존 내용 표시
3. ✅ 기존 재료 목록이 입력 폼에 표시됨

### 3. 재료 수정
1. 기존 재료 수정 가능
2. "재료 추가" 버튼으로 새 재료 추가
3. ✅ 올바른 인덱스로 추가됨
4. 삭제 버튼으로 재료 제거 가능

### 4. 이미지 수정
1. 기존 이미지 표시됨
2. 새 이미지 선택 시 미리보기
3. ✅ 기존 이미지 URL 유지 가능

---

## 📊 수정 파일 요약

### 수정된 파일 (2개)

1. **RecipeController.java**
   - `editRecipeForm()` 메서드 수정
   - DTO 변환 로직 추가
   - 재료 리스트 변환

2. **templates/recipes/form.html**
   - 재료 입력 부분 수정 (작성/수정 모드 구분)
   - JavaScript 재료 카운터 초기화 로직 개선
   - Thymeleaf 조건문 추가

### 변경 사항
- **추가**: DTO 변환 로직 (RecipeDetailResponse → RecipeUpdateRequest)
- **수정**: 재료 표시 로직 (기존 재료 th:each로 표시)
- **개선**: JavaScript 카운터 초기화

---

## 🎉 결과

**문제 해결!** ✅

이제 레시피 수정 기능이:
1. ✅ 수정 폼 정상 표시
2. ✅ 기존 데이터 자동 로드
3. ✅ 재료 수정/추가/삭제 가능
4. ✅ 이미지 변경 가능
5. ✅ 수정 완료 시 정상 저장

---

## 💡 교훈

### DTO 설계 원칙
1. **조회용 DTO와 수정용 DTO는 별도로 관리**
   - `RecipeDetailResponse`: 조회 전용 (getter만)
   - `RecipeCreateRequest`: 생성 전용 (setter 필요)
   - `RecipeUpdateRequest`: 수정 전용 (setter 필요)

2. **템플릿에 전달하는 객체는 바인딩 가능해야 함**
   - `th:field`, `th:object` 사용 시 setter 필수
   - 조회용 DTO는 템플릿 바인딩에 적합하지 않음

3. **타입 변환은 Controller 레이어에서**
   - Service는 각자의 DTO 반환
   - Controller가 필요 시 DTO 간 변환 수행

---

## 🔍 디버깅 팁

### 템플릿 파싱 오류 발생 시

1. **Model에 전달한 객체 타입 확인**
   - 템플릿이 기대하는 타입과 일치하는지 확인

2. **Thymeleaf 바인딩 확인**
   - `th:object="${recipe}"` - recipe 객체 필요
   - `th:field="*{title}"` - title의 getter/setter 필요

3. **컬렉션 타입 확인**
   - `th:each`로 순회하는 리스트의 요소 타입 확인
   - DTO 내부 클래스 타입이 일치하는지 확인

---

**수정 완료 날짜**: 2025년 11월 13일  
**상태**: ✅ 해결됨

