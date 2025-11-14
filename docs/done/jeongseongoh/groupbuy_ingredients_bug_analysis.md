# 공동구매 재료 기능 버그 분석 및 수정 계획

## 문제 상황

### 1. 일반 공구 생성 페이지 접근 시 Thymeleaf 파싱 오류
**에러 로그:**
```
org.attoparser.ParseException: Exception evaluating SpringEL expression: "recipe.ingredients" 
(template: "group-purchases/form" - line 91, col 76)
Caused by: org.springframework.expression.spel.SpelEvaluationException: EL1007E: Property or field 'ingredients' cannot be found on null
```

**발생 위치:** `form.html:91`

**원인:**
- 일반 공구 생성 시 `recipe` 객체가 `null`
- 하지만 `form.html:91`에서 `recipe.ingredients`를 참조하려고 시도
- `th:if="${recipe != null}"` 조건이 있음에도 불구하고 Thymeleaf가 파싱 단계에서 에러 발생

### 2. 레시피 기반 공구 생성 시 재료 JSON 파싱 실패
**에러 로그:**
```
2025-11-14T21:49:50.697+09:00  WARN 15872 --- [RecipeMate] [nio-8080-exec-4] 
c.r.d.g.controller.GroupBuyController    : Failed to parse ingredients JSON for group buy 289: 
Unrecognized token '필요한': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
```

**원인 분석:**
- 프론트엔드에서 `selectedIngredientsJson`을 올바르게 생성했지만
- 백엔드에서 파싱 실패
- 추측: `ingredients` 필드에 JSON이 아닌 일반 텍스트가 저장됨

---

## 근본 원인 분석

### 문제 1: Thymeleaf 파싱 단계 오류 (일반 공구)

**코드 위치:** `form.html:91`
```html
<div th:if="${recipe != null}" th:each="ingredient, stat : ${recipe.ingredients}" class="col-md-6">
```

**원인:**
- Thymeleaf는 **파싱 단계**에서 `${recipe.ingredients}` 표현식을 평가
- `th:if`가 있어도 **`th:each`가 먼저 파싱**되면서 `recipe`가 `null`인 경우 오류 발생

**해결책:**
- `th:if`와 `th:each`를 **분리**하여 중첩 구조로 변경
- 또는 Safe Navigation Operator(`?.`) 사용: `${recipe?.ingredients}`

### 문제 2: 일반 공구에서 재료 처리 누락

**현재 구조 분석:**

1. **컨트롤러 분리:**
   - 일반 공구: `POST /group-purchases` → `createGroupBuy()`
   - 레시피 기반: `POST /group-purchases/recipe-based` → `createRecipeBasedGroupBuy()`

2. **재료 처리 차이:**
   - 레시피 기반: `selectedIngredientsJson` 파싱 후 `request.setSelectedIngredients()`
   - 일반 공구: **재료 처리 로직 없음**

3. **엔티티 생성 차이:**
   - `GroupBuy.createGeneral()`: `ingredients` 필드에 **`null` 저장** (line 124)
   - `GroupBuy.createRecipeBased()`: `ingredients` 필드에 **JSON 문자열 저장** (line 149)

**문제점:**
- 일반 공구 생성 시 프론트엔드에서 `selectedIngredientsJson`을 전송해도
- 백엔드에서 파싱하지 않고 무시함
- 엔티티 생성 시 `ingredients = null`로 저장

### 문제 3: 재료 JSON 저장 방식 불일치

**백엔드 저장 방식 (레시피 기반):**
```java
// GroupBuyController.java:355
request.setSelectedIngredients(ingredients);

// CreateGroupBuyRequest.java
private List<SelectedIngredient> selectedIngredients;
private String selectedIngredientsJson;
```

**의문점:**
- `request.setSelectedIngredients()`는 **List 객체** 설정
- 하지만 DB에 저장되는 `ingredients` 필드는 **String**
- 어디서 JSON 문자열로 변환되는가?

**추측:**
- `GroupBuyService.createGroupBuy()`에서 변환
- 또는 `request.getIngredients()`가 JSON 문자열을 반환?

---

## 코드 흐름 추적

### 레시피 기반 공구 생성

1. **프론트엔드 (form.html:806)**
   ```javascript
   selectedIngredientsInput.value = JSON.stringify(selectedIngredients);
   // 결과: [{"name":"토마토","measure":"200g","selected":true}, ...]
   ```

2. **컨트롤러 (GroupBuyController.java:331-357)**
   ```java
   List<SelectedIngredient> ingredients = objectMapper.readValue(
       request.getSelectedIngredientsJson(), ...
   );
   request.setSelectedIngredients(ingredients); // List 설정
   ```

3. **서비스 (GroupBuyService.java:73)**
   ```java
   groupBuy = GroupBuy.createRecipeBased(
       ...
       request.getIngredients(), // ??? 이게 String JSON인가?
       ...
   );
   ```

4. **엔티티 (GroupBuy.java:149)**
   ```java
   .ingredients(ingredients) // String 저장
   ```

**문제:**
- `request.getIngredients()`와 `request.getSelectedIngredients()`의 관계 불명확
- `CreateGroupBuyRequest.ingredients` 필드가 언제 설정되는지 불명확

### 일반 공구 생성

1. **프론트엔드**: 동일하게 `selectedIngredientsJson` 전송

2. **컨트롤러 (GroupBuyController.java:273-296)**
   ```java
   // selectedIngredientsJson 파싱 로직 없음!
   GroupBuyResponse response = groupBuyService.createGroupBuy(user.getId(), request);
   ```

3. **서비스 (GroupBuyService.java:87-99)**
   ```java
   groupBuy = GroupBuy.createGeneral(
       ...
       // ingredients 파라미터 없음
   );
   ```

4. **엔티티 (GroupBuy.java:124)**
   ```java
   .ingredients(null) // 재료 없음
   ```

---

## 해결 방안

### 옵션 1: 일반 공구와 레시피 기반 공구 통합 (권장)

**목표:** 모든 공구에서 재료 필수화

**변경 사항:**

#### 1.1. 컨트롤러 통합
- `createGroupBuy()`에서도 `selectedIngredientsJson` 파싱
- 두 메서드의 재료 처리 로직 통일

#### 1.2. 엔티티 수정
- `GroupBuy.createGeneral()`에 `ingredients` 파라미터 추가
- 생성 메서드 통합 검토

#### 1.3. form.html 수정
- `th:if`와 `th:each` 분리 또는 Safe Navigation 사용
- 일반 공구에서도 재료 섹션 항상 표시 (이미 완료)

#### 1.4. 재료 필수 검증 추가
- `CreateGroupBuyRequest`에 validation 추가
- `selectedIngredientsJson` 필수 검증

---

## 권장 해결책: 옵션 1 (모든 공구 재료 필수)

### 이유:
1. **기획 의도와 일치**: 계획 문서에서 "모든 공구에서 재료 지원"
2. **일관성**: 공구 = 식재료 공동구매 → 재료 없으면 의미 없음
3. **단순화**: 두 개의 생성 로직을 하나로 통합 가능

### 구현 단계:

#### Phase 1: form.html Thymeleaf 오류 수정 (긴급)
```html
<!-- 현재 (오류 발생) -->
<div th:if="${recipe != null}" th:each="ingredient, stat : ${recipe.ingredients}" class="col-md-6">

<!-- 수정 후 (방법 1: 중첩 구조) -->
<th:block th:if="${recipe != null}">
    <div th:each="ingredient, stat : ${recipe.ingredients}" class="col-md-6">
        ...
    </div>
</th:block>

<!-- 수정 후 (방법 2: Safe Navigation) -->
<div th:each="ingredient, stat : ${recipe?.ingredients ?: {}}" class="col-md-6">
    <th:block th:if="${ingredient != null}">
        ...
    </th:block>
</div>
```

#### Phase 2: 일반 공구에서 재료 파싱 추가
```java
// GroupBuyController.java - createGroupBuy() 메서드
@PostMapping
public String createGroupBuy(...) {
    // ... 유효성 검증 ...
    
    // 재료 JSON 파싱 (레시피 기반과 동일)
    if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isBlank()) {
        try {
            List<SelectedIngredient> ingredients = objectMapper.readValue(
                request.getSelectedIngredientsJson(), 
                new TypeReference<List<SelectedIngredient>>() {}
            );
            
            if (ingredients == null || ingredients.isEmpty()) {
                model.addAttribute("errorMessage", "최소 1개 이상의 재료를 선택해주세요.");
                return "group-purchases/form";
            }
            
            request.setSelectedIngredients(ingredients);
            
        } catch (Exception e) {
            log.error("재료 JSON 파싱 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
            return "group-purchases/form";
        }
    } else {
        model.addAttribute("errorMessage", "최소 1개 이상의 재료를 선택해주세요.");
        return "group-purchases/form";
    }
    
    // ... 나머지 로직 ...
}
```

#### Phase 3: CreateGroupBuyRequest 수정
```java
// selectedIngredients → ingredients 변환 메서드 추가
public String getIngredientsJson() {
    if (selectedIngredients == null || selectedIngredients.isEmpty()) {
        return null;
    }
    try {
        return new ObjectMapper().writeValueAsString(selectedIngredients);
    } catch (Exception e) {
        return null;
    }
}
```

#### Phase 4: GroupBuy 엔티티 수정
```java
// GroupBuy.createGeneral() 메서드
public static GroupBuy createGeneral(
    User host, String title, String content, String ingredients, // ingredients 추가
    GroupBuyCategory category, Integer totalPrice,
    Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
    String meetupLocation, Integer parcelFee, boolean isParticipantListPublic
) {
    validateCreateArgs(totalPrice, targetHeadcount, deadline);
    return GroupBuy.builder()
        .host(host)
        .title(title)
        .content(content)
        .ingredients(ingredients) // null이 아닌 JSON 저장
        .category(category)
        // ... 나머지 필드 ...
        .build();
}
```

#### Phase 5: GroupBuyService 수정
```java
// GroupBuyService.createGroupBuy() 메서드
@Transactional
public GroupBuyResponse createGroupBuy(Long userId, CreateGroupBuyRequest request) {
    // ... 기존 로직 ...
    
    // ingredients JSON 문자열 가져오기
    String ingredientsJson = request.getIngredientsJson(); // Phase 3에서 추가한 메서드
    
    GroupBuy groupBuy;
    if (request.getRecipeApiId() != null) {
        groupBuy = GroupBuy.createRecipeBased(
            host, request.getTitle(), request.getContent(),
            ingredientsJson, // JSON 문자열 전달
            // ... 나머지 파라미터 ...
        );
    } else {
        groupBuy = GroupBuy.createGeneral(
            host, request.getTitle(), request.getContent(),
            ingredientsJson, // JSON 문자열 전달 (Phase 4에서 추가)
            // ... 나머지 파라미터 ...
        );
    }
    
    // ... 나머지 로직 ...
}
```

#### Phase 6: 테스트 및 검증
1. 일반 공구 생성 → 재료 없이 제출 시 에러 확인
2. 일반 공구 생성 → 재료 추가 후 제출 성공 확인
3. 레시피 기반 공구 생성 → 재료 선택 후 제출 성공 확인
4. 공구 상세 페이지 → 재료 표시 확인

---

## 예상 리스크

### 1. 기존 공구 데이터 호환성
- **문제:** 기존 일반 공구는 `ingredients = null`
- **해결:** ✅ **이미 구현됨** - `detail.html:117,134-137`에서 Fallback 처리 완료
  - JSON 파싱 성공 시: 구조화된 재료 목록 표시
  - JSON 파싱 실패 시: 원본 텍스트를 경고 스타일로 표시

### 2. JSON 파싱 실패 시 UX
- **문제:** 사용자가 재료 입력했는데 파싱 실패 시 데이터 유실
- **상태:** ⚠️ **부분적으로 구현됨**
  - ✅ 레시피 기반 공구: `GroupBuyController.java:361,369`에서 `formData` 모델에 추가하고 `recipe` 재조회
  - ❌ **문제점:** `recipe.ingredients`는 복원되지만, **사용자가 수정한 재료 데이터는 유실됨**
    - `selectedIngredientsJson`이 모델에 전달되지 않음
    - 프론트엔드가 JSON을 파싱해서 재료 UI를 재구성할 방법 없음
- **해결 방안:**
  - `selectedIngredientsJson`을 모델에 추가
  - JavaScript로 페이지 로드 시 JSON 파싱하여 재료 UI 재구성
  - 또는: Hidden input에 `selectedIngredientsJson` 저장하여 폼 재표시 시 복원

### 3. 두 개의 POST 엔드포인트 유지 이슈
- **문제:** `/group-purchases`와 `/recipe-based` 중복 로직
- **해결:** 공통 로직 추출하여 private 메서드로 분리

---

## 다음 단계

1. **즉시 수정 (긴급):** Phase 1 - form.html Thymeleaf 오류
2. **단기 수정:** Phase 2-5 - 일반 공구 재료 처리 로직 추가
3. **장기 리팩터링:** 두 개의 생성 엔드포인트 통합 검토

---

## 참고 파일

- `src/main/resources/templates/group-purchases/form.html` (line 91, 790-795)
- `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java` (line 273-296, 302-357)
- `src/main/java/com/recipemate/domain/groupbuy/service/GroupBuyService.java` (line 52-100)
- `src/main/java/com/recipemate/domain/groupbuy/entity/GroupBuy.java` (line 114-136, 138-164)
- `src/main/java/com/recipemate/domain/groupbuy/dto/CreateGroupBuyRequest.java` (line 29, 73, 76)
