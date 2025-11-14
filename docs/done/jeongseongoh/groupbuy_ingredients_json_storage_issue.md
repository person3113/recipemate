# 공구 재료 JSON 저장 방식 문제 분석 및 해결 방안

**작성일:** 2025-11-14  
**상태:** 🔴 긴급 수정 필요

---

## 📋 문제 요약

공구 생성/수정 시 재료(ingredients) 데이터를 **일반 텍스트 형식**으로 저장하여 JSON 파싱이 실패하는 문제 발생

---

## 🐛 발견된 문제

### 1. 레시피 기반 공구 생성 시 JSON 파싱 실패

**에러 로그:**
```
2025-11-14T22:16:01.478+09:00  WARN 13492 --- [RecipeMate] [nio-8080-exec-4] c.r.d.g.controller.GroupBuyController    : Failed to parse ingredients JSON for group buy 321: Unrecognized token '필요한': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')
```

**실제 저장된 데이터:**
```
필요한 재료:
- a (a)
- b (b)
```

**문제:** 이것은 JSON이 아니라 사람이 읽기 위한 일반 텍스트 형식입니다.

---

### 2. 일반 공구 수정 시 재료 업데이트 안 됨

**로그:**
```
2025-11-14T22:21:46.308+09:00  INFO 13492 --- [RecipeMate] [nio-8080-exec-7] c.r.d.g.controller.GroupBuyController    : Parsed 2 ingredients for group buy update
```

**문제:** 
- 컨트롤러에서 재료를 2개 파싱했으나 실제로는 업데이트되지 않음
- 서비스 레이어의 `updateGroupBuy()` 메서드에서 **레시피 기반 공구만** 재료 업데이트하도록 조건 설정됨

**현재 코드 (GroupBuyService.java:391-395):**
```java
// 5. 재료 정보 업데이트 (레시피 기반 공구인 경우)
String updatedIngredients = null;
if (groupBuy.getRecipeApiId() != null && request.getSelectedIngredients() != null && !request.getSelectedIngredients().isEmpty()) {
    updatedIngredients = buildSelectedIngredientsText(request.getSelectedIngredients());
}
```

→ `groupBuy.getRecipeApiId() != null` 조건 때문에 일반 공구는 재료 업데이트 불가

---

### 3. 빈 이미지 파일 업로드 경고

**로그:**
```
2025-11-14T22:16:01.219+09:00  WARN 13492 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Empty file at index 0, skipping
2025-11-14T22:16:01.219+09:00  INFO 13492 --- [RecipeMate] [nio-8080-exec-2] c.r.global.util.ImageUploadUtil          : Successfully uploaded 0 out of 1 images (parallel)
```

**문제:** 
- 사용자가 이미지를 첨부하지 않았는데도 빈 파일이 전송됨
- 프론트엔드에서 빈 `<input type="file">` 필드가 폼과 함께 제출되는 것으로 추정

**영향도:** 낮음 (경고 로그만 발생, 실제 동작에는 문제 없음)

---

## 🔍 근본 원인 분석

### 원인 1: buildSelectedIngredientsText() 메서드의 잘못된 구현

**위치:** `GroupBuyService.java:194-218`

**현재 구현:**
```java
private String buildSelectedIngredientsText(List<SelectedIngredient> selectedIngredients) {
    if (selectedIngredients == null || selectedIngredients.isEmpty()) {
        return "";
    }
    
    // 선택된 재료만 필터링
    List<SelectedIngredient> filteredIngredients = selectedIngredients.stream()
        .filter(ingredient -> Boolean.TRUE.equals(ingredient.getSelected()))
        .toList();
    
    if (filteredIngredients.isEmpty()) {
        return "";
    }
    
    StringBuilder sb = new StringBuilder("필요한 재료:\n");
    for (SelectedIngredient ingredient : filteredIngredients) {
        sb.append("- ").append(ingredient.getName());
        if (ingredient.getMeasure() != null && !ingredient.getMeasure().isBlank()) {
            sb.append(" (").append(ingredient.getMeasure()).append(")");
        }
        sb.append("\n");
    }
    
    return sb.toString().trim();
}
```

**문제점:**
1. **일반 텍스트 형식으로 변환** → JSON 파싱 불가능
2. 메서드 이름이 `buildSelectedIngredientsText()`인데 실제로는 JSON을 저장해야 함
3. 레시피 기반 공구 생성 시에도 이 메서드 사용 (`enrichWithRecipeInfo():173`)

**이 메서드가 호출되는 곳:**
- `enrichWithRecipeInfo()` (line 166) - 레시피 기반 공구 생성
- `updateGroupBuy()` (line 394) - 레시피 기반 공구 수정

---

### 원인 2: 일반 공구 vs 레시피 기반 공구 처리 불일치

| 동작 | 일반 공구 | 레시피 기반 공구 |
|------|-----------|------------------|
| **생성** | `getIngredientsJson()` 사용 ✅ | `buildSelectedIngredientsText()` 사용 ❌ |
| **수정** | 재료 업데이트 불가 ❌ | `buildSelectedIngredientsText()` 사용 ❌ |

**일반 공구 생성 (정상 동작):**
```java
// CreateGroupBuyRequest.java
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
→ 이 방식이 올바름! JSON 문자열로 직렬화

**레시피 기반 공구 생성/수정 (문제 있음):**
```java
// GroupBuyService.java:166, 173
CreateGroupBuyRequest enrichedRequest = CreateGroupBuyRequest.builder()
    .ingredients(ingredientsText) // ← buildSelectedIngredientsText() 결과 (일반 텍스트)
    ...
    .build();
```
→ `ingredients` 필드에 일반 텍스트 저장, JSON 파싱 불가

---

## ✅ 해결 방안

### 방안 1: buildSelectedIngredientsText()를 JSON 직렬화로 변경 (권장)

**장점:**
- 한 곳만 수정하면 모든 경로 해결
- 기존 `getIngredientsJson()` 로직 재사용

**수정 위치:** `GroupBuyService.java:194-218`

**수정 전:**
```java
private String buildSelectedIngredientsText(List<SelectedIngredient> selectedIngredients) {
    // ... 일반 텍스트 생성 로직
}
```

**수정 후:**
```java
private String buildSelectedIngredientsJson(List<SelectedIngredient> selectedIngredients) {
    if (selectedIngredients == null || selectedIngredients.isEmpty()) {
        return null;
    }
    
    // 선택된 재료만 필터링
    List<SelectedIngredient> filteredIngredients = selectedIngredients.stream()
        .filter(ingredient -> Boolean.TRUE.equals(ingredient.getSelected()))
        .toList();
    
    if (filteredIngredients.isEmpty()) {
        return null;
    }
    
    try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(filteredIngredients);
    } catch (Exception e) {
        log.error("재료 JSON 직렬화 실패: {}", e.getMessage());
        return null;
    }
}
```

**호출부 수정:**
1. `enrichWithRecipeInfo()` (line 166)
   ```java
   String ingredientsJson = buildSelectedIngredientsJson(request.getSelectedIngredients());
   ```

2. `updateGroupBuy()` (line 394)
   ```java
   updatedIngredients = buildSelectedIngredientsJson(request.getSelectedIngredients());
   ```

---

### 방안 2: 일반 공구 수정 시 재료 업데이트 지원

**문제:**
- 현재 레시피 기반 공구만 재료 업데이트 가능
- 일반 공구 수정 시 재료 필드가 무시됨

**수정 위치:** `GroupBuyService.java:391-414`

**수정 전:**
```java
// 5. 재료 정보 업데이트 (레시피 기반 공구인 경우)
String updatedIngredients = null;
if (groupBuy.getRecipeApiId() != null && request.getSelectedIngredients() != null && !request.getSelectedIngredients().isEmpty()) {
    updatedIngredients = buildSelectedIngredientsText(request.getSelectedIngredients());
}

// 6. 공구 정보 수정
groupBuy.update(
    request.getTitle(),
    request.getContent(),
    // ... 기타 필드
);

// 재료 정보가 있으면 업데이트
if (updatedIngredients != null) {
    groupBuy.updateIngredients(updatedIngredients);
}
```

**수정 후:**
```java
// 5. 재료 정보 업데이트 (모든 공구 유형 지원)
String updatedIngredients = null;
if (request.getSelectedIngredients() != null && !request.getSelectedIngredients().isEmpty()) {
    updatedIngredients = buildSelectedIngredientsJson(request.getSelectedIngredients());
}

// 6. 공구 정보 수정
groupBuy.update(
    request.getTitle(),
    request.getContent(),
    // ... 기타 필드
);

// 재료 정보가 있으면 업데이트 (null이면 기존 값 유지)
if (updatedIngredients != null) {
    groupBuy.updateIngredients(updatedIngredients);
}
```

**변경 사항:**
- `groupBuy.getRecipeApiId() != null` 조건 제거
- 일반 공구도 재료 업데이트 가능하도록 변경

---

### 방안 3: 빈 이미지 파일 전송 방지 (선택 사항)

**문제:** 프론트엔드에서 빈 `<input type="file">` 전송

**해결 방법 (프론트엔드):**
```javascript
// form.html의 JavaScript 부분
const form = document.querySelector('form');
form.addEventListener('submit', function(e) {
    const fileInputs = form.querySelectorAll('input[type="file"]');
    fileInputs.forEach(input => {
        if (input.files.length === 0) {
            input.disabled = true; // 빈 파일 입력 비활성화
        }
    });
});
```

**또는 백엔드 로직 개선:**
- 현재 `ImageUploadUtil`이 이미 빈 파일을 건너뛰고 있음
- WARN 로그를 DEBUG 레벨로 낮추거나 완전히 제거

---

## 📝 수정 체크리스트

### 필수 수정 (긴급)
- [ ] `buildSelectedIngredientsText()` → `buildSelectedIngredientsJson()`으로 변경
- [ ] 메서드 내부를 JSON 직렬화로 수정
- [ ] `enrichWithRecipeInfo()` 호출부 수정
- [ ] `updateGroupBuy()` 호출부 수정 및 레시피 기반 조건 제거
- [ ] 기존 잘못된 데이터 마이그레이션 (선택 사항)

### 테스트 항목
1. **레시피 기반 공구 생성**
   - [ ] 재료 JSON 형식으로 저장되는지 확인
   - [ ] 상세 페이지에서 재료 정상 표시되는지 확인

2. **일반 공구 생성**
   - [ ] 재료 JSON 형식으로 저장되는지 확인 (현재 정상)
   - [ ] 상세 페이지에서 재료 정상 표시되는지 확인

3. **공구 수정 (일반/레시피 기반 모두)**
   - [ ] 재료 추가/삭제 후 정상 업데이트되는지 확인
   - [ ] DB에 JSON 형식으로 저장되는지 확인

---

## 🗄️ 데이터 마이그레이션

**문제:** 기존에 생성된 공구들의 `ingredients` 필드가 일반 텍스트 형식으로 저장됨

**영향받는 데이터:**
- 공구 321번 (로그 기준)
- 레시피 기반으로 생성된 모든 공구

**마이그레이션 스크립트 예시:**
```sql
-- 1. 잘못된 형식으로 저장된 데이터 조회
SELECT id, ingredients 
FROM group_buys 
WHERE ingredients IS NOT NULL 
  AND ingredients LIKE '필요한 재료:%';

-- 2. 수동 마이그레이션 (재료 정보 재생성 필요)
-- 또는 해당 공구들의 ingredients를 NULL로 설정하고 재입력 요청
UPDATE group_buys 
SET ingredients = NULL 
WHERE ingredients LIKE '필요한 재료:%';
```

**권장 사항:**
- 테스트 환경에서 먼저 수정 적용 후 검증
- 프로덕션 환경에는 데이터 백업 후 적용

---

## 🎯 결론

**즉시 수정 필요:**
1. `buildSelectedIngredientsText()` 메서드를 JSON 직렬화로 변경
2. 일반 공구 수정 시에도 재료 업데이트 가능하도록 조건 변경

**장기적 개선:**
1. 프론트엔드에서 빈 파일 입력 전송 방지
2. 재료 데이터 저장 방식 통일 (항상 JSON)
3. 단위 테스트 추가 (재료 직렬화/역직렬화)

---

**관련 파일:**
- `GroupBuyService.java`
- `CreateGroupBuyRequest.java`
- `UpdateGroupBuyRequest.java`
- `GroupBuyController.java`
