# 공동구매-레시피 연결 오류 분석 및 해결 방안

## 1. 현상

- `http://localhost:8080/recipes/food-3285` 페이지를 통해 '시금치 후무스' 레시피로 공동구매를 생성했으나, 해당 공동구매 상세 페이지에 다음과 같은 경고 메시지가 나타납니다.
  > **삭제된 레시피**
  > 이 공동구매와 연관된 레시피가 삭제되었습니다.

- 하지만 제공된 SQL 쿼리 결과에 따르면 해당 레시피는 `RECIPES` 테이블에 `DELETED_AT` 컬럼이 `null`인 상태로, 실제로는 삭제되지 않았습니다.

## 2. 원인 분석

문제의 핵심 원인은 **공동구매(`GROUP_BUYS`) 테이블과 레시피(`RECIPES`) 테이블 간의 식별자 불일치**입니다.

1.  **ID 저장 방식의 차이**:
    -   공동구매 생성 시, `GROUP_BUYS` 테이블의 `recipe_api_id` 컬럼에는 `food-3285`와 같이 **API 출처와 ID가 조합된 문자열**이 저장됩니다.
    -   반면, `RECIPES` 테이블에는 API 출처(`source_api`)와 ID(`source_api_id`)가 각각 별도의 컬럼에 저장됩니다. (예: `source_api = 'FOOD_SAFETY'`, `source_api_id = '3285'`)

2.  **조회 로직의 한계**:
    -   공동구매 상세 페이지를 불러올 때, 시스템은 `GROUP_BUYS` 테이블에 저장된 `recipe_api_id` 값(`food-3285`)을 가지고 `RECIPES` 테이블에서 일치하는 레시피를 찾으려고 시도할 가능성이 높습니다.
    -   `RECIPES` 테이블에는 `food-3285`라는 `source_api_id`를 가진 레코드가 존재하지 않으므로 조회는 실패합니다.
    -   레시피 조회에 실패하자, 시스템은 이를 '레시피가 삭제된 경우'로 간주하여 사용자에게 경고 메시지를 표시하는 것으로 보입니다.

## 3. 데이터 확인

제공해주신 SQL 쿼리 결과가 위의 분석을 뒷받침합니다.

-   **GROUP_BUYS 테이블 (ID: 34)**
    -   `recipe_api_id`: `'food-3285'`

-   **RECIPES 테이블 (ID: 885)**
    -   `source_api`: `'FOOD_SAFETY'`
    -   `source_api_id`: `'3285'`
    -   `deleted_at`: `null`

`'food-3285'`와 `'3285'`는 서로 다른 값이므로 직접적인 연결이 불가능합니다.

## 4. 해결 방안

가장 이상적인 해결책은 데이터베이스 스키마 변경 없이 **애플리케이션의 조회 로직을 수정**하는 것입니다.

**권장 해결 방안: 레시피 조회 로직 수정**

공동구매 상세 정보를 가져와서 연관된 레시피를 화면에 표시하는 서비스 로직을 다음과 같이 수정해야 합니다.

1.  `GroupBuy` 엔티티에서 `getRecipeApiId()`를 통해 `'food-3285'` 값을 가져옵니다.
2.  가져온 문자열을 `-`를 기준으로 분리하여 API 출처 접두사(`'food'`)와 숫자 ID(`'3285'`)로 파싱합니다.
3.  API 출처 접두사를 `RECIPES` 테이블의 `source_api` 컬럼 값과 매칭되는 Enum 타입(e.g., `SourceApi.FOOD_SAFETY`)으로 변환합니다.
4.  파싱된 `sourceApi`와 `sourceApiId`를 사용하여 `RecipeRepository`에서 레시피를 조회합니다. (e.g., `findBySourceApiAndSourceApiId()`)

### 장점
-   데이터베이스 스키마를 변경할 필요가 없어 안전하고 적용이 간단합니다.
-   데이터 저장 방식과 조회 방식의 불일치라는 근본적인 문제를 해결합니다.
-   향후 다른 API 출처(e.g., `meal-xxxxx`)가 추가되더라도 유연하게 대응할 수 있습니다.

## 5. 코드 예시 (Pseudo-code)

수정이 필요한 서비스 레이어의 코드 예시는 다음과 같을 수 있습니다. (Java/Kotlin 기반으로 가정)

```java
// GroupBuyService.java 또는 관련 서비스 클래스

// AS-IS: 현재 예상되는 실패하는 로직
public GroupBuyDetailDto getGroupBuyDetail(Long groupBuyId) {
    GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow();
    // groupBuy.getRecipeApiId()는 "food-3285"를 반환
    // 아래 메서드는 "food-3285"를 ID로 찾아 실패하고 null을 반환할 가능성이 높음
    Recipe recipe = recipeRepository.findBySourceApiId(groupBuy.getRecipeApiId()); 

    if (recipe == null) {
        // 이 부분에서 '삭제된 레시피'로 처리하게 됨
    }
    // ...
}

// TO-BE: 수정 제안 로직
public GroupBuyDetailDto getGroupBuyDetail(Long groupBuyId) {
    GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId).orElseThrow();
    
    // 1. 복합 ID 파싱
    String combinedApiId = groupBuy.getRecipeApiId(); // "food-3285"
    String[] parts = combinedApiId.split("-");
    if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid recipe API ID format: " + combinedApiId);
    }
    String sourcePrefix = parts[0]; // "food"
    String recipeApiId = parts[1]; // "3285"

    // 2. API 출처 접두사를 Enum으로 변환하는 로직
    SourceApi sourceApi = convertPrefixToSourceApiEnum(sourcePrefix); 

    // 3. 올바른 파라미터로 레시피 조회
    // findBySourceApiAndSourceApiId 메서드가 RecipeRepository에 구현되어 있어야 함
    Recipe recipe = recipeRepository.findBySourceApiAndSourceApiId(sourceApi, recipeApiId)
                                    .orElse(null); // 레시피가 없을 경우를 대비해 orElse 처리

    if (recipe == null) {
        // 진짜 레시피가 없거나 삭제된 경우
    }
    // ... DTO 조립 로직
}

// sourcePrefix를 SourceApi Enum으로 변환하는 헬퍼 메서드
private SourceApi convertPrefixToSourceApiEnum(String prefix) {
    if ("food".equalsIgnoreCase(prefix)) {
        return SourceApi.FOOD_SAFETY;
    } else if ("meal".equalsIgnoreCase(prefix)) {
        return Sourceİ.THE_MEAL_DB;
    }
    // ... 기타 API 출처 처리
    throw new IllegalArgumentException("Unknown API source prefix: " + prefix);
}
```

## 6. 요약
`GROUP_BUYS` 테이블의 `recipe_api_id`는 `'food-3285'` 형식인데, `RECIPES` 테이블은 `source_api`와 `source_api_id`로 나누어 저장하고 있어 조회가 실패합니다. `GroupBuy` 상세 정보를 조회하는 로직에서 `recipe_api_id`를 파싱하여 두 개의 분리된 컬럼으로 레시피를 찾도록 수정하면 문제가 해결됩니다.

---

## 7. 추가 분석 및 최종 결론 (2025-11-25)

코드에 대한 심층 분석 결과, 문제의 더 정확한 원인이 밝혀졌습니다.

### 최종 원인: 잘못된 서비스 메서드 호출

`GroupBuyController`의 상세 페이지 로직(`detailPage` 메서드)에서 연관 레시피의 존재 여부를 확인할 때, **잘못된 서비스 메서드를 호출**하고 있었습니다.

`RecipeService`에는 레시피를 조회하는 두 가지 주요 메서드가 있습니다.
1.  `getRecipeDetail(String apiId)`: **오직 외부 API 클라이언트(`FoodSafetyClient`, `TheMealDBClient`)만을 호출**하여 레시피 정보를 가져옵니다. 이 메서드는 우리 애플리케이션의 **로컬 데이터베이스(`RECIPES` 테이블)를 조회하지 않습니다.**
2.  `getRecipeDetailByApiId(String apiId)`: **로컬 데이터베이스를 먼저 조회**하고, 만약 DB에 레시피 정보가 없을 경우에만 외부 API를 호출하는 `getRecipeDetail`을 예비(fallback)로 사용합니다. 훨씬 안정적이고 효율적인 방식입니다.

문제의 `GroupBuyController` 코드는 다음과 같습니다.

```java
// GroupBuyController.java의 detailPage 메서드 내부
// ...
} else {
    // 접두사 있음 = API 레시피 (API ID로 조회)
    recipeService.getRecipeDetail(recipeApiId); // <- 문제의 원인!
}
// ...
```

이 코드는 DB를 먼저 확인하는 `getRecipeDetailByApiId`를 호출해야 함에도 불구하고, 외부 API만 확인하는 `getRecipeDetail`을 호출하고 있습니다.

'시금치 후무스'의 경우, `food-3285`를 인자로 받은 `getRecipeDetail` 메서드가 외부 `FoodSafetyClient`를 통해 레시피를 조회했지만, 어떤 이유로든 (일시적 API 오류, 클라이언트 로직 문제 등) 결과를 받지 못했습니다. `getRecipeDetail`은 DB를 확인하지 않으므로, 최종적으로 레시피를 찾지 못했다는 예외(`RECIPE_NOT_FOUND`)를 발생시켰고, 이것이 사용자에게 "삭제된 레시피" 경고로 나타난 것입니다.

### 최종 해결 방안

**`GroupBuyController`에서 호출하는 서비스 메서드를 올바르게 변경**하는 것입니다.

-   **파일**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
-   **위치**: `detailPage` 메서드 내부
-   **수정 전**: `recipeService.getRecipeDetail(recipeApiId);`
-   **수정 후**: `recipeService.getRecipeDetailByApiId(recipeApiId);`

이렇게 수정하면, 시스템은 이미 로컬 데이터베이스에 저장된 '시금치 후무스' 레시피 정보를 정상적으로 찾아오게 되어 문제가 해결됩니다.
