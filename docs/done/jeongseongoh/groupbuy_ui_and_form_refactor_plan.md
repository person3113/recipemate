# 공동구매 UI 및 폼 리팩터링 계획

이 문서는 공동구매 기능의 UI 및 폼 로직 개선을 위한 리팩터링 계획을 설명합니다.

## 1. 공동구매 상세 페이지 개선 (`group-purchases/detail.html`)

### 1.1. 배송 정보 표시 누락 문제 해결

**문제점:**
- 현재 상세 페이지에서 배송 방법(`deliveryMethod`)이 '택배' 또는 '직거래/택배'일 때 '택배비'(`parcelFee`)가 표시되지 않습니다.
- '직거래' 또는 '직거래/택배'일 때 '만남 장소'(`meetupLocation`)가 표시되지 않습니다.

**해결 방안:**
1.  `group-purchases/detail.html` 템플릿에서 구매 정보 사이드바 섹션을 찾습니다.
2.  `th:if` 조건을 사용하여 `groupBuy.deliveryMethod`의 값에 따라 '택배비'와 '만남 장소' 정보가 올바르게 표시되도록 수정합니다.
    -   `deliveryMethod`가 `PARCEL` 또는 `BOTH`일 때 택배비 표시
    -   `deliveryMethod`가 `DIRECT` 또는 `BOTH`일 때 만남 장소 표시

### 1.2. 재료 목록 UI 스타일 개선

**문제점:**
- 공동구매 상세 페이지의 '필요한 재료' 섹션이 단순 텍스트로 표시되어 가독성이 떨어집니다.
- `recipes/detail.html`의 재료 표시 방식과 통일성이 없습니다.

**해결 방안:**
1.  `GroupBuyController`의 `detailPage` 메서드에서 `GroupBuyResponse` DTO에 포함된 `ingredients` JSON 문자열을 서버 단에서 파싱하여 `List<SelectedIngredient>` 형태로 모델에 추가합니다.
2.  `group-purchases/detail.html` 템플릿을 수정합니다.
3.  기존의 단순 텍스트로 재료를 표시하는 `<p>` 태그를 제거합니다.
4.  `recipes/detail.html`의 재료 표시 부분 HTML 구조를 복사하여 붙여넣습니다.
5.  모델에 추가된 재료 리스트를 `th:each`로 순회하며, 각 재료의 이름(`name`)과 양(`measure`)을 표시하도록 수정합니다.

## 2. 공동구매 생성 및 수정 폼 통일 및 개선 (`group-purchases/form.html`)

**문제점:**
- 레시피 기반 공동구매와 일반 공동구매의 수정 폼이 구분되지 않고, 재료를 수정할 수 없습니다.
- 일반 공동구매 생성 시 재료를 추가하는 기능이 불완전합니다.
- 전체적으로 생성/수정 로직이 통일되어 있지 않습니다.

**해결 방안:**

### 2.1. 백엔드 수정

1.  **`UpdateGroupBuyRequest.java` DTO 수정:**
    -   `CreateGroupBuyRequest.java`와 동일하게, 재료 목록을 JSON 문자열로 받기 위한 `selectedIngredientsJson` 필드를 추가합니다.
2.  **`GroupBuyService.java` 수정:**
    -   `updateGroupBuy` 메서드가 `selectedIngredientsJson`을 받아서 파싱하고, 기존 재료 정보를 갱신하도록 로직을 수정합니다.
3.  **`GroupBuyController.java` 수정:**
    -   `editPage` 메서드:
        -   `GroupBuy` 엔티티의 `ingredients` (JSON 문자열)를 `List<SelectedIngredient>` 객체로 파싱합니다.
        -   파싱된 재료 리스트를 모델에 담아 `form.html` 템플릿으로 전달합니다.
        -   레시피 기반 공구일 경우, 레시피 정보도 함께 전달합니다.
    -   `updateGroupBuy` 메서드:
        -   수정된 `UpdateGroupBuyRequest` DTO를 사용하도록 시그니처를 변경합니다.
        -   폼에서 전송된 `selectedIngredientsJson` 데이터를 서비스 레이어로 전달합니다.

### 2.2. 프론트엔드 수정 (`form.html`)

1.  **폼 템플릿 통일:**
    -   `th:if` 조건을 활용하여 하나의 폼으로 세 가지 경우를 모두 처리합니다.
        -   **일반 생성:** `recipe` 객체와 `groupBuy` 객체가 모두 `null`인 경우. 재료 목록은 비어있음.
        -   **레시피 기반 생성:** `recipe` 객체가 `not null`인 경우. 레시피 정보 카드 표시 및 재료 목록 자동 채우기.
        -   **수정:** `groupBuy` 객체가 `not null`인 경우. 컨트롤러에서 전달받은 재료 목록을 `th:each`로 채우기. 레시피 기반 공구일 경우 레시피 정보 카드도 표시.
2.  **재료 선택 섹션 개선:**
    -   `th:if="${recipe != null}"`로 감싸진 재료 선택 섹션을 항상 표시되도록 변경합니다.
    -   컨트롤러에서 전달된 재료 목록(생성 시에는 레시피 재료, 수정 시에는 공구의 기존 재료)을 기반으로 동적으로 렌더링합니다.
    -   일반 생성 시에는 이 목록이 비어있게 됩니다.
3.  **JavaScript 로직 개선:**
    -   '재료 추가' 버튼 클릭 시 비어있는 새 재료 입력 필드를 동적으로 생성하는 기능을 확인하고 없으면 구현합니다. (있는 걸로 알고 있긴 함)
    -   폼 제출 시, 모든 재료(기존, 수정, 신규 추가된 재료)를 `selectedIngredientsJson` 형식의 JSON 문자열로 직렬화하여 `hidden input`에 담아 서버로 전송하는 로직을 검토하고 완성합니다. 이 로직은 생성/수정 모드에서 모두 동작해야 합니다.

