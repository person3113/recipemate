# 🐛 사용자 레시피 조회 오류 수정

**문제 발생일**: 2025년 11월 13일  
**증상**: 사용자가 작성한 레시피가 목록에는 표시되지만, 클릭 시 "레시피를 찾을 수 없습니다" 오류 발생

---

## 🔍 문제 원인

### 증상
```json
{
  "success": false,
  "code": "RECIPE-001",
  "message": "레시피를 찾을 수 없습니다.",
  "data": null
}
```

### 원인 분석

**파일**: `RecipeService.java`의 `convertRecipeEntityToSimpleInfo` 메서드

```java
// 문제가 있던 코드
private RecipeListResponse.RecipeSimpleInfo convertRecipeEntityToSimpleInfo(Recipe recipe) {
    String apiId = recipe.getSourceApi() == RecipeSource.MEAL_DB 
            ? MEAL_PREFIX + recipe.getSourceApiId()
            : FOOD_PREFIX + recipe.getSourceApiId();  // ❌ USER 타입 처리 안 됨!
    // ...
}
```

**문제점**:
1. 이 메서드는 DB에서 조회한 레시피를 목록용 DTO로 변환할 때 사용됩니다
2. `RecipeSource.USER` (사용자 레시피) 타입을 처리하지 못했습니다
3. 사용자 레시피의 경우 `sourceApiId`가 `null`이므로 잘못된 ID가 생성되었습니다
   - 예: `food-null` 또는 오류 발생

**결과**:
- 목록에서는 레시피가 표시됨 (DB에서 조회되므로)
- 하지만 생성된 ID가 잘못되어 상세 페이지에서 조회 불가
- Controller에서 해당 ID로 레시피를 찾지 못해 `RECIPE_NOT_FOUND` 오류 발생

---

## ✅ 해결 방법

### 수정된 코드

**파일**: `RecipeService.java`

```java
/**
 * Recipe 엔티티를 RecipeSimpleInfo로 변환
 */
private RecipeListResponse.RecipeSimpleInfo convertRecipeEntityToSimpleInfo(Recipe recipe) {
    // API ID 결정 (사용자 레시피는 DB ID 사용)
    String apiId;
    if (recipe.getSourceApi() == RecipeSource.USER) {
        apiId = String.valueOf(recipe.getId());  // ✅ 사용자 레시피는 DB ID
    } else if (recipe.getSourceApi() == RecipeSource.MEAL_DB) {
        apiId = MEAL_PREFIX + recipe.getSourceApiId();
    } else {
        apiId = FOOD_PREFIX + recipe.getSourceApiId();
    }
    
    String imageUrl = recipe.getThumbnailImageUrl() != null 
            ? recipe.getThumbnailImageUrl() 
            : recipe.getFullImageUrl();
    
    return RecipeListResponse.RecipeSimpleInfo.builder()
            .id(apiId)
            .name(recipe.getTitle())
            .imageUrl(imageUrl)
            .category(recipe.getCategory())
            .source(recipe.getSourceApi().name().toLowerCase())
            .build();
}
```

### 추가 수정

**중복 메서드 제거**: `convertToSimpleInfo` 메서드 삭제
- `getUserRecipes`에서 `convertRecipeEntityToSimpleInfo` 사용하도록 통일

---

## 🎯 동작 원리

### 레시피 ID 구분

| RecipeSource | sourceApiId | 목록 ID 형식 | 상세 조회 방법 |
|--------------|-------------|--------------|----------------|
| MEAL_DB | "52772" | `meal-52772` | API ID로 조회 |
| FOOD_SAFETY | "1234" | `food-1234` | API ID로 조회 |
| USER | null | `"123"` (DB ID) | DB ID로 조회 |

### Controller 처리 로직

```java
@GetMapping("/{recipeId}")
public String recipeDetailPage(@PathVariable String recipeId, Model model) {
    RecipeDetailResponse recipe;
    
    // recipeId가 순수 숫자인지 확인 (DB ID)
    if (recipeId.matches("\\d+")) {
        // ✅ 사용자 레시피: DB ID로 조회
        Long dbId = Long.parseLong(recipeId);
        recipe = recipeService.getRecipeDetailById(dbId);
    } else {
        // ✅ API 레시피: API ID로 조회
        recipe = recipeService.getRecipeDetailByApiId(recipeId);
    }
    
    // ...
}
```

---

## ✅ 테스트 시나리오

### 1. 사용자 레시피 생성
1. `/recipes/new` 접속
2. 레시피 정보 입력 및 저장
3. ✅ 상세 페이지로 리다이렉트 성공

### 2. 목록에서 조회
1. `/recipes` 레시피 목록 접속
2. 사용자가 작성한 레시피 클릭
3. ✅ 상세 페이지 정상 표시

### 3. 내 레시피 목록
1. `/recipes/my` 접속
2. 내가 작성한 레시피 클릭
3. ✅ 상세 페이지 정상 표시

### 4. API 레시피 조회 (기존 기능)
1. `/recipes` 접속
2. API로 받아온 레시피 클릭
3. ✅ 상세 페이지 정상 표시 (기존 동작 유지)

---

## 📊 수정 파일 요약

### 수정된 파일 (1개)
- `RecipeService.java`
  - `convertRecipeEntityToSimpleInfo()` 메서드 수정
  - `convertToSimpleInfo()` 중복 메서드 제거

### 변경 사항
- **추가**: USER 타입 레시피 ID 처리 로직
- **삭제**: 중복 메서드 1개
- **수정**: 메서드 참조 통일

---

## 🎉 결과

**문제 해결!** ✅

이제 사용자가 작성한 레시피가:
1. ✅ 목록에 정상 표시
2. ✅ 클릭 시 상세 페이지 정상 조회
3. ✅ 수정/삭제 버튼 정상 표시
4. ✅ API 레시피와 구분되어 관리

---

## 🔍 디버깅 팁

### 문제 발생 시 확인 사항

1. **레시피 ID 형식 확인**
   - 브라우저 개발자 도구에서 네트워크 탭 확인
   - 목록의 링크 URL이 `/recipes/123` 형식인지 확인

2. **DB 데이터 확인**
   ```sql
   SELECT id, title, source_api, source_api_id, user_id 
   FROM recipes 
   WHERE source_api = 'USER';
   ```

3. **로그 확인**
   ```
   RecipeService - 레시피 조회: ID=123
   ```

---

**수정 완료 날짜**: 2025년 11월 13일  
**상태**: ✅ 해결됨

