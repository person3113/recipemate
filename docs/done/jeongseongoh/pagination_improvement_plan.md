# 페이지네이션 개선 계획

## 1. 문제점 분석

현재 애플리케이션의 페이지네이션 기능에는 세 가지 주요 문제가 있습니다.

### 1.1. 불필요한 필터 파라미터 포함
- **현상**: 레시피 목록 (`/recipes`) 및 공동구매 목록 (`/group-purchases/list`)에서 페이지를 이동할 때, 필터 값이 비어있음에도 불구하고 URL에 `keyword=`, `category=` 와 같은 빈 파라미터가 포함됩니다.
- **원인**: Thymeleaf의 `th:href` 속성이 모든 파라미터를 조건 없이 URL에 추가하고 있기 때문입니다. 이로 인해 불필요한 "필터 해제" 버튼이 표시되는 부작용이 발생합니다.
- **영향 받는 파일**:
    - `src/main/resources/templates/recipes/list.html`
    - `src/main/resources/templates/group-purchases/list.html`

### 1.2. 페이지네이션 스타일 가독성 저하
- **현상**: 현재 페이지를 나타내는 페이지네이션 숫자의 배경색과 글자색이 모두 주황색으로 설정되어 있어 숫자가 보이지 않습니다.
- **원인**: `styles.css` 파일에서 활성화된 페이지 링크(.pagination .page-item.active .page-link)에 배경색만 지정하고 글자색은 지정하지 않아, 기본 링크 색상인 주황색이 그대로 적용되고 있습니다.
- **영향 받는 파일**:
    - `src/main/resources/static/css/styles.css`

### 1.3. 공동구매 목록 페이지네이션의 비일관성
- **현상**: 공동구매 목록 페이지는 숫자 기반 페이지네이션이 아닌 '이전/다음' 링크만 제공하며, 레시피 목록과 동일하게 불필요한 URL 파라미터 문제를 겪고 있습니다.
- **원인**: 레시피 목록과 다른 별도의 페이지네이션 UI를 사용하고 있으며, URL 생성 로직의 결함도 공유하고 있습니다.
- **영향 받는 파일**:
    - `src/main/resources/templates/group-purchases/list.html`

---

## 2. 해결 방안

### 2.1. 페이지네이션 URL 생성 로직 수정

Thymeleaf의 유틸리티 객체 `#strings`를 사용하여 파라미터 값이 비어있지 않을 때만 URL에 포함되도록 수정합니다. 이렇게 하면 필터가 적용되지 않은 상태에서 페이지를 이동할 때 더 이상 빈 파라미터가 추가되지 않습니다.

**적용 대상**: `recipes/list.html`, `group-purchases/list.html`

**예시 (recipes/list.html):**

```html
<!-- 수정 전 -->
<a class="page-link" th:href="@{/recipes(page=${pageNumber - 1}, keyword=${keyword}, ...)}"></a>

<!-- 수정 후 -->
<a class="page-link" th:href="@{/recipes(page=${pageNumber - 1},
    keyword=${!#strings.isEmpty(keyword) ? keyword : null},
    category=${!#strings.isEmpty(category) ? category : null},
    ...
)}"></a>
```
- 모든 필터 파라미터에 `!#strings.isEmpty(param) ? param : null` 조건을 적용하여, 값이 있는 경우에만 URL에 추가되도록 합니다.
- "필터 해제" 버튼의 `th:if` 조건 또한 `!#strings.isEmpty(param)`를 사용하도록 강화하여 불필요한 표시를 방지합니다.

### 2.2. 활성 페이지 번호 스타일 수정

활성화된 페이지 번호의 가독성을 높이기 위해 CSS 파일을 수정합니다.

**적용 대상**: `src/main/resources/static/css/styles.css`

```css
/* 수정 전 */
.pagination .page-item.active .page-link {
    z-index: 3;
    background-color: #fd7e14; /* 주황색 배경 */
    border-color: #fd7e14;
}

/* 수정 후 */
.pagination .page-item.active .page-link {
    z-index: 3;
    color: #fff; /* 흰색 글자 */
    background-color: #fd7e14; /* 주황색 배경 */
    border-color: #fd7e14;
}
```
- `.pagination .page-item.active .page-link` 선택자에 `color: #fff;` 속성을 추가하여 텍스트를 흰색으로 만듭니다.

### 2.3. 페이지네이션 컴포넌트 공통화 및 일관성 확보

유지보수성과 일관성을 높이기 위해 페이지네이션 UI를 공통의 Thymeleaf 프래그먼트(조각)로 분리합니다.

**1. 공통 프래그먼트 생성 (`fragments/pagination.html`)**

페이지네이션 로직을 담은 `src/main/resources/templates/fragments/pagination.html` 파일을 생성합니다. 이 프래그먼트는 기본 URL, 페이지 정보, 필터 파라미터를 파라미터로 받아 동적으로 페이지네이션 링크를 생성합니다.

**2. 기존 템플릿 수정**

`recipes/list.html`과 `group-purchases/list.html`의 기존 페이지네이션 코드를 삭제하고, 생성한 공통 프래그먼트를 `th:replace`를 사용하여 삽입합니다.

**적용 대상**: `recipes/list.html`, `group-purchases/list.html`

**예시 (recipes/list.html):**
```html
<!-- 기존 페이지네이션 코드 삭제 -->

<!-- 공통 프래그먼트 삽입 -->
<div th:replace="~{fragments/pagination :: pagination(
    url='/recipes',
    page=${recipes},
    keyword=${keyword},
    category=${category},
    ...
)}"></div>
```

**예시 (group-purchases/list.html):**
```html
<!-- 기존 페이지네이션 코드 삭제 -->

<!-- 공통 프래그먼트 삽입 -->
<div th:replace="~{fragments/pagination :: pagination(
    url='/group-purchases/list',
    page=${groupBuys},
    keyword=${searchCondition.keyword},
    status=${searchCondition.status},
    ...
)}"></div>
```

이러한 방식으로 수정하면, 세 가지 문제가 모두 해결되어 사용자 경험이 개선되고 코드의 유지보수성이 향상될 것입니다.
