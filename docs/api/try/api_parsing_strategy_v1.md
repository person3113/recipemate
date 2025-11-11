# FoodSafety API 재료 파싱 개선 제안 (v1)

## 1. 현황 및 문제점 분석

현재 FoodSafety API로부터 받은 재료 정보(`RCP_PARTS_DTLS`)를 파싱하는 로직은 쉼표(`,`)만을 기준으로 문자열을 분리하고 있습니다.

하지만 실제 API 응답 데이터에는 쉼표(`,`) 외에도 **개행 문자(`\n`)** 가 재료를 구분하는 용도로 사용되는 경우가 많습니다. 이로 인해 개행 문자로 구분된 여러 재료가 하나의 항목으로 잘못 합쳐지는 문제가 발생합니다.

### 문제 발생 코드 (`CookRecipeResponse.java`)

`getIngredients` 메서드는 현재 아래와 같이 구현되어 있습니다.

```java
/**
 * 재료 정보를 파싱하여 리스트로 반환
 * RCP_PARTS_DTLS는 쉼표로 구분된 문자열
 */
public List<String> getIngredients() {
    List<String> ingredients = new ArrayList<>();
    
    if (rcpPartsDtls == null || rcpPartsDtls.trim().isEmpty()) {
        return ingredients;
    }

    String[] parts = rcpPartsDtls.split(","); // 쉼표로만 분리하는 로직
    for (String part : parts) {
        String trimmed = part.trim();
        if (!trimmed.isEmpty()) {
            ingredients.add(trimmed);
        }
    }

    return ingredients;
}
```

### 파싱 실패 사례

예를 들어, "새우 두부 계란찜"의 재료 정보는 다음과 같습니다.

```json
"RCP_PARTS_DTLS": "새우두부계란찜\n연두부 75g(3/4모), 칵테일새우 20g(5마리), 달걀 30g(1/2개), 생크림 13g(1큰술), 설탕 5g(1작은술), 무염버터 5g(1작은술)\n고명\n시금치 10g(3줄기)"
```

현재 로직은 이 문자열을 처리한 후 다음과 같은 잘못된 결과를 반환합니다.

- `"새우두부계란찜\n연두부 75g(3/4모)"` -> `새우두부계란찜`과 `연두부`가 합쳐짐
- `"무염버터 5g(1작은술)\n고명\n시금치 10g(3줄기)"` -> `무염버터`, `고명`, `시금치`가 모두 합쳐짐

## 2. 개선 방안 제안

가장 시급한 **개행 문자(`\n`) 처리 문제**를 해결하기 위해, 문자열 분리 기준을 쉼표(`,`)와 개행 문자(`\n`) 모두를 포함하도록 변경할 것을 제안합니다.

`String.split()` 메서드에 정규식을 사용하여 여러 구분자를 동시에 처리할 수 있습니다.

### 개선된 코드 (제안)

`getIngredients` 메서드를 아래와 같이 수정합니다.

```java
/**
 * 재료 정보를 파싱하여 리스트로 반환
 * RCP_PARTS_DTLS는 쉼표 또는 개행 문자로 구분된 문자열
 */
public List<String> getIngredients() {
    List<String> ingredients = new ArrayList<>();
    
    if (rcpPartsDtls == null || rcpPartsDtls.trim().isEmpty()) {
        return ingredients;
    }

    // 쉼표(,) 또는 개행(\n)을 기준으로 분리. 구분자가 여러 개 연속되어도 처리.
    String[] parts = rcpPartsDtls.split("[,\\n]+"); 
    for (String part : parts) {
        String trimmed = part.trim();
        if (!trimmed.isEmpty()) {
            ingredients.add(trimmed);
        }
    }

    return ingredients;
}
```

### 변경점 요약

- `rcpPartsDtls.split(",")` -> `rcpPartsDtls.split("[,\\n]+")`
- **`[,\\n]+`** 정규식은 "쉼표 또는 개행 문자가 하나 이상 연속되는 부분"을 기준으로 문자열을 분리합니다. 이를 통해 다양한 형식의 재료 목록을 효과적으로 처리할 수 있습니다.

## 3. 기대 효과

위와 같이 수정하면, 앞서 언급된 "새우 두부 계란찜"의 재료가 다음과 같이 정상적으로 분리됩니다.

- `새우두부계란찜`
- `연두부 75g(3/4모)`
- `칵테일새우 20g(5마리)`
- `달걀 30g(1/2개)`
- `생크림 13g(1큰술)`
- `설탕 5g(1작은술)`
- `무염버터 5g(1작은술)`
- `고명`
- `시금치 10g(3줄기)`

이 변경은 `\n`으로 인해 발생하던 다수의 파싱 오류를 해결하여 데이터 정합성을 크게 향상시킬 것입니다.

```