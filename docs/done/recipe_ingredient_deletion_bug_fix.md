# 레시피 수정 시 재료 삭제 버그 수정

## 작업 일자
2025년 11월 14일

## 문제 상황
레시피 수정 페이지에서 기존 재료를 삭제 버튼으로 제거하고 "수정 완료"를 누르면:
- 삭제한 재료 칸이 빈 칸으로 남아있음
- 빈 칸 때문에 유효성 검증에 실패하여 수정이 완료되지 않음

## 원인 분석
1. **프론트엔드 문제**: 재료 삭제 시 DOM 요소는 제거되지만 인덱스가 재정렬되지 않음
   - 예: `ingredients[0]`, `ingredients[1]`, `ingredients[2]` 중 `ingredients[1]` 삭제 시
   - 결과: `ingredients[0]`, `ingredients[2]`가 전송됨
   - 서버에서는 `ingredients[1]`을 빈 값으로 인식

2. **백엔드 문제**: 빈 재료가 서버로 전송되어도 필터링되지 않음
   - `@NotBlank` 유효성 검증만 있어서 빈 값이 있으면 전체가 실패

## 해결 방법

### 1. 프론트엔드 수정 ✅
**파일**: `src/main/resources/templates/recipes/form.html`

**변경 내용**:
- `reindexIngredients()` 함수 추가: 재료 삭제 후 인덱스 자동 재정렬
- `removeIngredient()` 함수 수정: 삭제 후 `reindexIngredients()` 호출

```javascript
// 재료 삭제
function removeIngredient(button) {
    const ingredientItem = button.closest('.ingredient-item');
    if (document.querySelectorAll('.ingredient-item').length > 1) {
        ingredientItem.remove();
        // 삭제 후 인덱스 재정렬 추가
        reindexIngredients();
    } else {
        alert('최소 1개의 재료가 필요합니다.');
    }
}

// 재료 인덱스 재정렬 (새로 추가)
function reindexIngredients() {
    const ingredientItems = document.querySelectorAll('.ingredient-item');
    ingredientItems.forEach((item, index) => {
        const nameInput = item.querySelector('input[name*=".name"]');
        const measureInput = item.querySelector('input[name*=".measure"]');
        
        if (nameInput) nameInput.name = `ingredients[${index}].name`;
        if (measureInput) measureInput.name = `ingredients[${index}].measure`;
    });
    ingredientCount = ingredientItems.length;
}
```

### 2. 백엔드 수정 ✅
**파일**: `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`

**변경 내용**:
- 레시피 생성(`createRecipe`) 메서드에 빈 재료 필터링 로직 추가
- 레시피 수정(`updateRecipe`) 메서드에 빈 재료 필터링 로직 추가

```java
// 빈 재료 필터링 (name 또는 measure가 비어있는 경우 제거)
if (request.getIngredients() != null) {
    request.setIngredients(
        request.getIngredients().stream()
            .filter(ingredient -> ingredient.getName() != null && !ingredient.getName().trim().isEmpty()
                               && ingredient.getMeasure() != null && !ingredient.getMeasure().trim().isEmpty())
            .collect(java.util.stream.Collectors.toList())
    );
}
```

**적용 위치**:
- `createRecipe()`: 유효성 검증 전에 빈 재료 필터링
- `updateRecipe()`: 유효성 검증 전에 빈 재료 필터링

## 수정 효과

### Before (문제 상황)
1. 사용자가 재료 3개 중 2번째 재료 삭제
2. 서버로 전송: `ingredients[0]` (값 있음), `ingredients[1]` (빈 값), `ingredients[2]` (값 있음)
3. 유효성 검증 실패: `ingredients[1]`이 빈 값이므로 `@NotBlank` 위반
4. 수정 실패 ❌

### After (수정 후)
**프론트엔드 해결책**:
1. 사용자가 재료 3개 중 2번째 재료 삭제
2. 자동으로 인덱스 재정렬
3. 서버로 전송: `ingredients[0]` (값 있음), `ingredients[1]` (값 있음)
4. 수정 성공 ✅

**백엔드 해결책** (안전망):
1. 만약 빈 값이 전송되더라도
2. 서버에서 자동으로 필터링
3. 유효성 검증 통과
4. 수정 성공 ✅

## 테스트 체크리스트

- [ ] 레시피 작성 시 재료 추가/삭제가 정상 작동하는지 확인
- [ ] 레시피 수정 시 기존 재료 삭제 후 수정 완료가 되는지 확인
- [ ] 재료 1개만 있을 때 삭제 시 경고 메시지가 나타나는지 확인
- [ ] 재료를 여러 개 삭제한 후 제출 시 정상 작동하는지 확인
- [ ] 중간 재료를 삭제했을 때 인덱스가 자동으로 재정렬되는지 확인

## 기술적 세부사항

### 이중 방어 전략
1. **프론트엔드 방어**: 인덱스 재정렬로 빈 값 발생 방지
2. **백엔드 방어**: 만약 빈 값이 전송되어도 필터링으로 제거

이렇게 두 레이어에서 방어함으로써 더 안정적인 시스템 구현

### Stream API 사용
Java 8의 Stream API를 사용하여 간결하고 효율적으로 빈 재료 필터링:
- `filter()`: 조건에 맞는 요소만 선택
- `trim()`: 공백 제거 후 검증
- `isEmpty()`: 빈 문자열 체크

## 완료 ✅

프론트엔드와 백엔드 모두 수정 완료하여 레시피 수정 시 재료 삭제 버그가 해결되었습니다.

