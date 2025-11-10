# 레시피 재료 파서 개선 계획 (3차 최종안)

## 1. 현황 분석: 1차 구현 코드 리뷰

사용자가 1차로 구현한 `RecipeMapper.java`와 `form.html`의 코드를 분석했습니다.

- **`form.html`**: 계획대로 재료명과 용량을 모두 수정할 수 있도록 `<input>` 필드로 변경되었고, 관련 JavaScript 로직 또한 올바르게 수정되었습니다. **클라이언트 측 수정은 완료된 것으로 판단됩니다.**
- **`RecipeMapper.java`**: `normalizeIngredientString`, `splitByCommaOutsideParentheses`, `extractIngredientInfo` 등의 메서드를 포함한 다단계 파싱 로직이 구현되어 있습니다. 이는 이전에 제안된 v4.0 계획과 일치합니다.

`api_parsing_log.md`의 로그는 현재 구현된 이 파서의 실행 결과입니다. 로그 분석 결과, 파싱 정확도가 향상되었지만 아래와 같은 핵심 문제가 여전히 남아있습니다.

### 잔여 핵심 문제: "하나의 토큰 = 하나의 재료" 가정의 한계

현재 로직은 전처리 및 분리 단계를 거쳐 생성된 각 재료 토큰(token)을 단일 재료로 간주하고 파싱을 시도합니다. 하지만 API 응답 데이터에는 아래 예시처럼 **하나의 토큰에 여러 재료가 포함**된 경우가 많습니다.

- **로그 예시**: `대파 2.5g 다진 마늘 1.25g`
- **현재 동작**: 위 문자열 전체가 하나의 토큰으로 `extractIngredientInfo`에 전달됩니다. `pattern2` (`^(.+?)\s+(\d+.*)$`)가 매칭되어, `이름: 대파`, `용량: 2.5g 다진 마늘 1.25g` 라는 **잘못된 결과**를 반환합니다.
- **기대 동작**: 위 문자열에서 `대파 2.5g`와 `다진 마늘 1.25g`라는 두 개의 독립된 재료를 추출해야 합니다.

## 2. 최종 개선 전략: "반복 추출" 방식으로의 전환

이 문제를 해결하기 위해, "하나의 토큰을 통째로 분석"하는 현재 방식에서 **"하나의 토큰 안에서 여러 재료 패턴을 반복적으로 찾아 추출"**하는 방식으로 `extractIngredientInfo` 메서드의 패러다임을 전환해야 합니다.

### `extractIngredientInfo` 메서드 개선안

- 이 메서드는 이제 `IngredientWithMeasure` 단일 객체가 아닌, `List<IngredientWithMeasure>`를 반환하도록 변경합니다.
- `while (matcher.find())` 루프를 사용하여, 전달된 `text` 토큰에서 재료 패턴이 더 이상 발견되지 않을 때까지 반복적으로 재료를 추출합니다.

```java
// 제안되는 새로운 extractIngredientInfo 로직 (의사 코드)

private List<IngredientWithMeasure> extractIngredientsFromToken(String token) {
    List<IngredientWithMeasure> foundIngredients = new ArrayList<>();
    
    // "이름 + 양" 패턴을 찾는 정규식
    // 이름: 한글/영문/공백. 양: 숫자나 괄호로 시작.
    Pattern ingredientPattern = Pattern.compile("([가-힣A-Za-z\s]+?)\s*([\d(].*?)(?=[가-힣A-Za-z\s]+[\d(]|$)");
    
    Matcher matcher = ingredientPattern.matcher(token);
    
    while (matcher.find()) {
        String name = matcher.group(1).trim();
        String measure = matcher.group(2).trim();
        
        // 후처리: 이름에서 불필요한 마커나 찌꺼기 제거
        name = name.replaceAll(":$", "").trim();

        if (!name.isEmpty()) {
            foundIngredients.add(new IngredientWithMeasure(name, measure));
        }
    }
    
    // 만약 위 루프에서 아무것도 찾지 못했다면, 토큰 전체를 하나의 재료로 간주 (기존 fallback 로직)
    if (foundIngredients.isEmpty() && !token.isEmpty()) {
        foundIngredients.add(new IngredientWithMeasure(token, "적당량"));
    }
    
    return foundIngredients;
}

// parseIngredients 메서드는 이제 List를 받아서 addAll로 추가해야 함
// ...
// for (String token : ingredientTokens) {
//     List<IngredientWithMeasure> ingredients = extractIngredientsFromToken(trimmed);
//     ingredients.addAll(ingredients);
// }
// ...
```
*참고: 위 정규식은 예시이며, 실제 구현 시에는 로그에 나타난 다양한 엣지 케이스(`200cc. 망고 50g` 등)를 처리하기 위해 더 정교한 패턴이 필요할 수 있습니다.*

### `normalizeIngredientString` 메서드 미세 조정

- 현재 정규식은 `[ 2인분 ]`과 같은 마커를 처리할 때 대괄호만 제거하고 `2인분`이라는 텍스트를 남길 수 있습니다.
- `result = result.replaceAll("\[[^\]]+\]", "\n");` 부분을 `result = result.replaceAll("\[[^\]]*\]", "\n");` 와 같이 수정하여 대괄호와 그 안의 모든 내용을 확실히 제거하도록 개선합니다.

## 3. 최종 구현 계획

1.  **`RecipeMapper.java` 수정**:
    - `extractIngredientInfo` 메서드의 이름을 `extractIngredientsFromToken`으로 변경하고, 반환 타입을 `List<IngredientWithMeasure>`로 수정합니다.
    - 메서드 내 로직을 `while (matcher.find())`를 사용하는 **반복 추출 방식**으로 전면 재작성합니다.
    - `parseIngredients` 메서드에서 `extractIngredientsFromToken`가 반환한 `List`를 최종 결과 리스트에 `addAll` 하도록 수정합니다.
    - `normalizeIngredientString`의 대괄호 제거 정규식을 미세 조정합니다.
2.  **테스트 및 검증**: 수정한 로직을 `api_parsing_log.md`에 기록된 실패 케이스들에 대해 다시 테스트하여 개선 여부를 확인합니다. (이는 제가 직접 수행할 수 없으므로, 코드 수정 후 사용자에게 요청해야 할 수 있습니다.)

이것이 파싱 문제를 해결하기 위한 최종적이고 가장 확실한 계획입니다. 이 계획을 실행하여 코드를 수정하겠습니다.