# 🐛 사용자 레시피로 공구 만들기 오류 수정

**문제 발생일**: 2025년 11월 13일  
**증상**: 사용자가 작성한 레시피로 "이 레시피로 공구 만들기" 버튼을 클릭하면 "레시피 정보를 불러올 수 없습니다" 오류 발생

---

## 🔍 문제 원인

### 증상
사용자가 작성한 레시피 상세 페이지에서 "이 레시피로 공구 만들기" 버튼을 클릭하면 공구 생성 페이지에서 레시피 정보를 불러오지 못했습니다.

### 원인 분석

**파일**: `GroupBuyController.java`의 `createPage` 메서드

```java
// 문제가 있던 코드
@GetMapping("/new")
public String createPage(@RequestParam(required = false) String recipeApiId, ...) {
    if (recipeApiId != null && !recipeApiId.isBlank()) {
        try {
            // ❌ getRecipeDetailByApiId는 API ID 형식만 처리 (meal-123, food-456)
            RecipeDetailResponse recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
            // ...
        }
    }
}
```

**문제점**:
1. `getRecipeDetailByApiId`는 `meal-{id}`, `food-{id}` 형식의 API ID만 처리합니다
2. 사용자 레시피의 ID는 순수 숫자 (예: "123")입니다
3. 레시피 상세 페이지에서 `recipe.id`를 그대로 전달하는데:
   - API 레시피: `recipe.id = "meal-52772"` ✅ 정상 동작
   - 사용자 레시피: `recipe.id = "15"` ❌ 처리 불가

**결과**:
- `getRecipeDetailByApiId("15")`를 호출하면 접두사가 없어서 `INVALID_INPUT_VALUE` 오류 발생
- 공구 생성 페이지에서 레시피 정보를 불러올 수 없음

---

## ✅ 해결 방법

### GroupBuyController 수정

**파일**: `GroupBuyController.java`

```java
@GetMapping("/new")
public String createPage(
    @RequestParam(required = false) String recipeApiId,
    Model model
) {
    // 빈 폼 객체 추가
    CreateGroupBuyRequest formData = new CreateGroupBuyRequest();
    
    // 레시피 기반 공구인 경우 레시피 정보 조회
    if (recipeApiId != null && !recipeApiId.isBlank()) {
        try {
            RecipeDetailResponse recipe;
            
            // ✅ recipeApiId가 순수 숫자인지 확인 (사용자 레시피 = DB ID)
            if (recipeApiId.matches("\\d+")) {
                // 숫자만 있으면 DB ID로 조회
                Long dbId = Long.parseLong(recipeApiId);
                recipe = recipeService.getRecipeDetailById(dbId);
            } else {
                // 접두사가 있으면 API ID로 조회 (meal-, food- 등)
                recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
            }
            
            model.addAttribute("recipe", recipe);
            // ...
        }
    }
    // ...
}
```

### 수정 내용

**1단계 검증**: `recipeApiId.matches("\\d+")`
- 정규표현식으로 순수 숫자인지 확인
- 사용자 레시피: `"123"` → true
- API 레시피: `"meal-52772"` → false

**2단계 조회**:
- 순수 숫자 → `getRecipeDetailById(Long)` 호출 (DB 직접 조회)
- 접두사 포함 → `getRecipeDetailByApiId(String)` 호출 (기존 로직)

---

## 🎯 동작 원리

### 레시피 ID 형식에 따른 처리

| 레시피 종류 | recipe.id | recipeApiId (파라미터) | 처리 방법 |
|------------|-----------|----------------------|----------|
| 사용자 작성 | `"15"` | `"15"` | `getRecipeDetailById(15)` ✅ |
| TheMealDB | `"meal-52772"` | `"meal-52772"` | `getRecipeDetailByApiId("meal-52772")` ✅ |
| 식품안전나라 | `"food-1234"` | `"food-1234"` | `getRecipeDetailByApiId("food-1234")` ✅ |

### 공구 생성 흐름

```
사용자 레시피 상세 페이지
    ↓
"이 레시피로 공구 만들기" 버튼 클릭
    ↓
GET /group-purchases/new?recipeApiId=15
    ↓
1. recipeApiId = "15" 확인
    ↓
2. "15".matches("\\d+") = true ✅
    ↓
3. Long.parseLong("15") = 15
    ↓
4. getRecipeDetailById(15) 호출
    ↓
5. 레시피 정보 로드 성공 ✅
    ↓
6. 공구 생성 폼에 레시피 정보 표시
```

---

## ✅ 테스트 시나리오

### 1. 사용자 레시피로 공구 만들기 (수정 후)
1. 사용자 A가 작성한 레시피 상세 페이지 접속
2. "이 레시피로 공구 만들기" 버튼 클릭
3. ✅ 공구 생성 페이지로 이동
4. ✅ 레시피 정보가 자동으로 채워짐
5. ✅ 재료 선택 가능

### 2. TheMealDB 레시피로 공구 만들기 (기존 동작 유지)
1. TheMealDB API 레시피 상세 페이지 접속
2. "이 레시피로 공구 만들기" 버튼 클릭
3. ✅ 공구 생성 페이지로 이동
4. ✅ 레시피 정보가 자동으로 채워짐 (기존과 동일)

### 3. 식품안전나라 레시피로 공구 만들기 (기존 동작 유지)
1. 식품안전나라 API 레시피 상세 페이지 접속
2. "이 레시피로 공구 만들기" 버튼 클릭
3. ✅ 공구 생성 페이지로 이동
4. ✅ 레시피 정보가 자동으로 채워짐 (기존과 동일)

---

## 📊 수정 파일 요약

### 수정된 파일 (1개)

**GroupBuyController.java**
- `createPage()` 메서드 수정
- recipeApiId 형식 검증 로직 추가
- 사용자 레시피와 API 레시피 구분 처리

### 변경 사항
- **추가**: 정규표현식 검증 (`recipeApiId.matches("\\d+")`)
- **추가**: 조건부 메서드 호출 (DB ID vs API ID)
- **유지**: 기존 API 레시피 처리 로직

---

## 🎉 결과

**문제 해결!** ✅

이제:
1. ✅ 사용자 레시피로 공구 만들기 정상 동작
2. ✅ TheMealDB 레시피로 공구 만들기 정상 동작 (기존과 동일)
3. ✅ 식품안전나라 레시피로 공구 만들기 정상 동작 (기존과 동일)
4. ✅ 모든 레시피 출처에서 공구 생성 가능

---

## 💡 추가 개선 사항

### 1. RecipeService에 통합 메서드 추가 (선택사항)

더 깔끔한 코드를 원한다면 Service에 통합 메서드를 추가할 수 있습니다:

```java
// RecipeService.java
public RecipeDetailResponse getRecipeDetail(String recipeId) {
    if (recipeId.matches("\\d+")) {
        Long dbId = Long.parseLong(recipeId);
        return getRecipeDetailById(dbId);
    } else {
        return getRecipeDetailByApiId(recipeId);
    }
}
```

그러면 Controller 코드가 더 간단해집니다:

```java
// GroupBuyController.java
recipe = recipeService.getRecipeDetail(recipeApiId);
```

### 2. 에러 처리 개선

레시피를 찾지 못했을 때 사용자에게 명확한 메시지를 제공:

```java
} catch (CustomException e) {
    log.error("Failed to load recipe: {}", recipeApiId, e);
    model.addAttribute("error", "레시피 정보를 불러올 수 없습니다. 레시피가 삭제되었을 수 있습니다.");
}
```

---

## 🔍 디버깅 팁

### 문제 발생 시 확인 사항

1. **recipeApiId 파라미터 확인**
   - 브라우저 개발자 도구 → 네트워크 탭
   - URL: `/group-purchases/new?recipeApiId=???`
   - 값이 제대로 전달되는지 확인

2. **로그 확인**
   ```
   GroupBuyController - Recipe ID: 15
   RecipeService - Finding recipe by DB ID: 15
   ```

3. **데이터베이스 확인**
   ```sql
   SELECT id, title, source_api FROM recipes WHERE id = 15;
   ```

---

**수정 완료 날짜**: 2025년 11월 13일  
**상태**: ✅ 해결됨

