# 공동구매 재료 기능 전면 지원 계획

## 📋 목표

**모든 공동구매(일반/레시피 기반, 생성/수정, 상세보기)에서 재료 기능을 일관되게 지원**

- ✅ 일반 공구 생성 시 → 재료 추가 가능
- ✅ 레시피 기반 공구 생성 시 → 레시피 재료 표시 + 수정/추가 가능
- ✅ 일반 공구 수정 시 → 재료 수정/추가 가능
- ✅ 레시피 기반 공구 수정 시 → 재료 수정/추가 가능
- ✅ 상세 페이지 → 재료가 있으면 항상 표시

---

## 🔍 현재 문제점

### 1. 데이터 일관성 문제
- **DB의 `ingredients` 필드**: JSON 형식과 일반 텍스트가 혼재
- **예시 (공구 257번)**: 
  ```
  "필요한 재료: 돼지고기 200g, 양배추 1/8통, ..."  ← 일반 텍스트
  ```
  ```json
  [{"name":"돼지고기","measure":"200g","selected":true}]  ← JSON (정상)
  ```
- **결과**: JSON 파싱 실패 → 상세 페이지에서 재료 섹션 안 보임

### 2. UI 표시 조건 문제
- **detail.html (117라인)**:
  ```html
  <div th:if="${ingredientsList != null and !ingredientsList.isEmpty()}">
  ```
  → JSON 파싱 실패 시 `ingredientsList`가 `null`이라 재료 섹션 안 보임

### 3. 폼 조건 불완전
- **form.html (71라인)**:
  ```html
  <div th:if="${recipe != null or (groupBuy != null and groupBuy.recipeApiId != null)}">
  ```
  → **일반 공구 생성/수정 시 재료 섹션이 아예 안 보임**

### 4. JavaScript 조건 불완전
- **form.html (730라인)**:
  ```html
  <script th:if="${recipe != null or (groupBuy != null and groupBuy.recipeApiId != null)}">
  ```
  → **일반 공구에서 재료 추가/제거 버튼이 동작 안 함**

---

## ✅ 해결 방안

### Phase 1: 프론트엔드 조건 수정 (즉시 적용 가능)

#### 1.1. form.html - 재료 섹션 조건 변경

**현재 (71라인):**
```html
<div th:if="${recipe != null or (groupBuy != null and groupBuy.recipeApiId != null)}">
```

**수정 후:**
```html
<!-- 항상 표시 (생성/수정 모두) -->
<div class="mb-4">
```

**이유**: 모든 공구에서 재료를 입력할 수 있어야 함

---

#### 1.2. form.html - JavaScript 조건 제거

**현재 (730라인):**
```html
<script th:if="${recipe != null or (groupBuy != null and groupBuy.recipeApiId != null)}">
```

**수정 후:**
```html
<!-- 조건 제거: 모든 공구에서 동작 -->
<script th:inline="javascript">
```

**이유**: 일반 공구에서도 재료 추가/제거 버튼이 동작해야 함

---

#### 1.3. form.html - 재료 목록 렌더링 로직 통합

**현재 (83-143라인)**: 
- `recipe != null` 일 때: `recipe.ingredients` 표시
- `groupBuy != null and ingredientsList != null` 일 때: `ingredientsList` 표시

**수정 후**:
```html
<div class="row g-2" id="ingredientsList">
    <!-- 레시피 기반 생성: recipe.ingredients를 표시 -->
    <div th:if="${recipe != null}" th:each="ingredient, stat : ${recipe.ingredients}" class="col-md-6">
        <!-- 기존 코드 유지 -->
    </div>
    
    <!-- 공구 수정 (레시피 기반 + 일반 모두): ingredientsList를 표시 -->
    <div th:if="${groupBuy != null and ingredientsList != null and !ingredientsList.isEmpty()}" 
         th:each="ingredient, stat : ${ingredientsList}" class="col-md-6">
        <!-- 기존 코드 유지 -->
    </div>
    
    <!-- 일반 생성: 비어있음 (JavaScript로 추가) -->
</div>
```

**안내 메시지 조건부 표시**:
```html
<p class="text-muted small mb-3">
    <i class="bi bi-info-circle"></i> 
    <span th:if="${groupBuy != null}">
        공동구매에 포함할 재료를 수정하세요. 분량은 수정할 수 있습니다.
    </span>
    <span th:if="${groupBuy == null and recipe != null}">
        공동구매에 포함할 재료를 선택하세요. 분량은 수정할 수 있습니다.
    </span>
    <span th:if="${groupBuy == null and recipe == null}">
        공동구매에 필요한 재료를 추가하세요. (선택사항)
    </span>
</p>
```

---

#### 1.4. form.html - JavaScript 초기화 로직 개선

**현재 (737라인):**
```javascript
let ingredientIndex = /*[[${recipe != null ? recipe.ingredients.size() : (ingredientsList != null ? ingredientsList.size() : 0)}]]*/ 0;
```

**문제없음**: 이미 모든 케이스 처리됨

---

#### 1.5. form.html - 폼 제출 시 재료 필수 검증 제거

**현재 (790-794라인):**
```javascript
if (selectedIngredients.length === 0) {
    e.preventDefault();
    alert('최소 1개 이상의 재료를 선택해주세요.');
    return false;
}
```

**수정 후:**
```javascript
// 재료가 없어도 제출 가능 (일반 공구의 경우)
// 재료가 있을 때만 JSON 생성
if (selectedIngredients.length > 0) {
    let selectedIngredientsInput = document.getElementById('selectedIngredientsJson');
    if (!selectedIngredientsInput) {
        selectedIngredientsInput = document.createElement('input');
        selectedIngredientsInput.type = 'hidden';
        selectedIngredientsInput.id = 'selectedIngredientsJson';
        selectedIngredientsInput.name = 'selectedIngredientsJson';
        form.appendChild(selectedIngredientsInput);
    }
    selectedIngredientsInput.value = JSON.stringify(selectedIngredients);
}
```

**이유**: 일반 공구는 재료가 선택사항이므로

---

#### 1.6. detail.html - Fallback 표시 추가

**현재 (117라인):**
```html
<div th:if="${ingredientsList != null and !ingredientsList.isEmpty()}">
```

**수정 후:**
```html
<div th:if="${groupBuy.ingredients != null and !groupBuy.ingredients.isBlank()}" class="border-top pt-4 mt-4">
    <h5 class="fw-bold mb-3">
        <i class="bi bi-basket3 text-primary"></i> 필요한 재료
    </h5>
    
    <!-- JSON 파싱 성공 시 -->
    <div th:if="${ingredientsList != null and !ingredientsList.isEmpty()}" class="row g-2">
        <div th:each="ingredient : ${ingredientsList}" class="col-md-6">
            <div class="d-flex align-items-center p-2 bg-light rounded">
                <i class="bi bi-circle-fill text-primary me-2 difficulty-icon"></i>
                <span class="fw-semibold me-2" th:text="${ingredient.name}">재료명</span>
                <span class="text-muted small" th:text="${ingredient.measure}">분량</span>
            </div>
        </div>
    </div>
    
    <!-- JSON 파싱 실패 시 Fallback: 원본 텍스트 표시 -->
    <div th:if="${ingredientsList == null or ingredientsList.isEmpty()}" class="alert alert-warning">
        <i class="bi bi-exclamation-triangle"></i> 
        <span th:text="${groupBuy.ingredients}">재료 정보</span>
    </div>
</div>
```

**이유**: JSON 파싱 실패해도 재료 정보는 보여야 함

---

### Phase 2: 백엔드 수정 (선택사항)

#### 2.1. GroupBuyController - editPage 메서드 수정

**현재 로직 (179-230라인)**:
```java
// 레시피 기반 공구만 재료 파싱
if (groupBuy.getRecipeApiId() != null && groupBuy.getIngredients() != null) {
    // 파싱...
}
```

**수정 후**:
```java
// 모든 공구에서 재료 파싱 시도
if (groupBuy.getIngredients() != null && !groupBuy.getIngredients().isBlank()) {
    try {
        List<SelectedIngredient> ingredientsList = objectMapper.readValue(
            groupBuy.getIngredients(),
            new TypeReference<List<SelectedIngredient>>() {}
        );
        model.addAttribute("ingredientsList", ingredientsList);
    } catch (Exception e) {
        log.warn("Failed to parse ingredients JSON for group buy {}: {}", id, e.getMessage());
        // 파싱 실패 시에도 폼은 정상 표시 (사용자가 다시 입력 가능)
    }
}
```

**이유**: 일반 공구도 재료가 있을 수 있음

---

#### 2.2. 폼 제출 엔드포인트 수정 (선택사항)

**createGroupBuy / createRecipeBasedGroupBuy / updateGroupBuy**:
- `selectedIngredientsJson`이 비어있어도 에러 안 나도록 검증 로직 수정
- 재료가 없으면 `ingredients` 필드를 `null` 또는 빈 문자열로 저장

---

### Phase 3: 데이터 마이그레이션 (기존 데이터 정리)

#### 3.1. 기존 공구의 ingredients 필드 정리

**문제**: DB에 JSON이 아닌 일반 텍스트가 저장된 공구들이 존재

**해결 방법 1 - SQL 업데이트 스크립트**:
```sql
-- 1. JSON이 아닌 텍스트 형식 데이터 찾기
SELECT id, ingredients 
FROM group_buys 
WHERE ingredients IS NOT NULL 
  AND ingredients NOT LIKE '[%'
  AND ingredients NOT LIKE '{%';

-- 2. 해당 데이터를 null로 초기화 (또는 수동 변환)
UPDATE group_buys 
SET ingredients = NULL 
WHERE ingredients IS NOT NULL 
  AND ingredients NOT LIKE '[%'
  AND ingredients NOT LIKE '{%';
```

**해결 방법 2 - 애플리케이션 레벨에서 변환**:
```java
// 스타트업 시 또는 별도 스크립트로 실행
@PostConstruct
public void migrateIngredientsData() {
    List<GroupBuy> allGroupBuys = groupBuyRepository.findAll();
    
    for (GroupBuy gb : allGroupBuys) {
        String ingredients = gb.getIngredients();
        if (ingredients != null && !ingredients.isBlank()) {
            // JSON 형식 체크
            if (!ingredients.trim().startsWith("[") && !ingredients.trim().startsWith("{")) {
                // 일반 텍스트 → null로 초기화 (또는 변환 로직 추가)
                gb.updateIngredients(null);
            }
        }
    }
    
    groupBuyRepository.saveAll(allGroupBuys);
}
```

---

## 📊 작업 우선순위

| 순서 | 작업 | 우선순위 | 비고 |
|------|------|----------|------|
| 1 | form.html 재료 섹션 조건 제거 | **긴급** | 일반 공구에서 재료 입력 불가 |
| 2 | form.html JavaScript 조건 제거 | **긴급** | 버튼 동작 안 함 |
| 3 | form.html 재료 필수 검증 제거 | **긴급** | 일반 공구 제출 불가 |
| 4 | detail.html Fallback 표시 추가 | **높음** | 파싱 실패 시 재료 안 보임 |
| 5 | GroupBuyController editPage 수정 | 중간 | 일관성 개선 |
| 6 | 데이터 마이그레이션 | 낮음 | 기존 데이터 정리 |

---

## 🎯 기대 효과

### 사용자 경험 개선
- ✅ 모든 공구 유형에서 일관된 UX
- ✅ 일반 공구에서도 재료 입력 가능
- ✅ 재료가 있는 공구는 항상 재료 표시

### 개발자 경험 개선
- ✅ 코드 로직 단순화 (조건 분기 감소)
- ✅ 유지보수성 향상
- ✅ 버그 발생 가능성 감소

### 데이터 일관성 확보
- ✅ ingredients 필드 형식 통일 (JSON)
- ✅ 파싱 에러 감소
- ✅ Fallback으로 안정성 확보

---

## 📝 구현 체크리스트

### Frontend (form.html)
- [ ] 재료 섹션 조건 제거 (항상 표시)
- [ ] JavaScript 조건 제거 (모든 공구에서 동작)
- [ ] 재료 필수 검증 제거 (선택사항으로 변경)
- [ ] 안내 메시지 조건부 표시

### Frontend (detail.html)
- [ ] Fallback 표시 추가 (JSON 파싱 실패 시)

### Backend (선택)
- [ ] GroupBuyController.editPage 수정
- [ ] 폼 제출 엔드포인트 검증 로직 수정

### Data Migration (선택)
- [ ] 기존 공구 ingredients 필드 정리
- [ ] SQL 스크립트 작성 및 실행

---

## 🔄 이전 문서와의 차이점

### `groupbuy_ui_and_form_refactor_plan.md`의 한계
- **재료 기능을 레시피 기반 공구에만 제한**
- "일반 공구는 재료 불필요"라는 가정
- 조건부 표시 (`th:if="${recipe != null or ...}"`)

### 새로운 접근 방식
- **모든 공구에서 재료 지원**
- 재료는 선택사항이지만 모든 공구 유형에서 입력 가능
- 조건 최소화 → 코드 단순화
- Fallback으로 안정성 확보

---

## 📌 요약

**핵심 변경사항**:
1. ✅ **form.html**: 재료 섹션/JavaScript를 모든 공구에서 동작하도록 조건 제거
2. ✅ **detail.html**: JSON 파싱 실패 시에도 재료 표시 (Fallback)
3. ✅ **일관성**: 모든 공구 유형에서 동일한 재료 입력/표시 방식

**결과**: 사용자는 공구 유형에 관계없이 재료를 자유롭게 추가/수정/조회할 수 있음
