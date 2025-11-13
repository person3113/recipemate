# 통합 검색 리팩토링 계획 (htmx 제거 및 SSR 전환)

## 1. 배경 및 목표

### 1.1. 문제점
`htmx`를 사용한 동적 UI 구현이 브라우저의 기본 동작(캐시, 뒤로가기)과 충돌하여 다음과 같은 복합적인 문제를 야기했습니다.
-   '뒤로가기' 시 페이지 상태가 오염되거나 400 에러 발생
-   탭의 결과 개수와 실제 내용 불일치
-   불필요한 복잡성 증가 및 예측 불가능한 동작

### 1.2. 목표
**htmx를 완전히 제거**하고, 모든 요청에 대해 서버가 완전한 HTML 페이지를 새로 렌더링하는 **전통적인 서버 사이드 렌더링(SSR) 방식으로 전환**하여 기능의 안정성과 예측 가능성을 확보하는 것을 목표로 합니다.

## 2. 리팩토링 계획

### 2.1. 파일 및 코드 제거

-   **`search/fragments.html` 파일 삭제**: htmx 전용 프래그먼트 템플릿은 더 이상 필요 없으므로 프로젝트에서 완전히 삭제합니다.
-   **`results.html` 내 htmx 관련 코드 삭제**:
    -   `<script>` 태그로 포함된 htmx 라이브러리 제거
    -   `hx-*` 속성 (e.g., `hx-get`, `hx-target`, `hx-push-url`) 전체 제거
    -   `htmx:afterSwap`, `popstate` 등 htmx와 상태 관리를 위해 추가된 모든 JavaScript 코드 블록 제거

### 2.2. 백엔드 수정 (`SearchController.java`)

-   **`/search/fragments` 엔드포인트 삭제**: `searchFragments` 메소드를 컨트롤러에서 삭제합니다.
-   **`GET /search` 엔드포인트 단일화**:
    -   이 엔드포인트가 `keyword`, `type`, `pageable` 파라미터를 받아 모든 검색 UI 요청을 처리합니다.
    -   `SearchService`를 호출하여 `type`에 맞는 데이터를 조회하고, 그 결과를 모델에 담아 항상 `search/results.html` 템플릿 전체를 렌더링하여 반환합니다.
    -   `HttpServletResponse`를 통해 `Cache-Control` 헤더를 설정하는 로직은, SSR 방식에서도 '뒤로가기' 시 항상 최신 데이터를 보장해주므로 **유지하는 것을 권장**합니다.

### 2.3. 프론트엔드 수정 (`results.html`)

-   **탭 UI 변경**:
    -   `<ul>`, `<li>` 구조의 탭을 `<button>`이 아닌 일반 링크 `<a>` 태그로 변경합니다.
    -   `th:href`를 사용하여 각 탭에 맞는 `type` 파라미터를 포함한 URL을 생성합니다.
    -   현재 활성화된 탭은 `type` 파라미터 값을 비교하여 `th:classappend="'active'"`를 적용합니다.
    -   **예시**:
        ```html
        <a class="nav-link" 
           th:href="@{/search(keyword=${keyword}, type='RECIPE')}"
           th:classappend="${type == 'RECIPE'} ? 'active'">레시피</a>
        ```

-   **콘텐츠 표시 로직 변경**:
    -   `th:if`를 사용하여 `type` 값에 따라 표시할 콘텐츠 블록을 결정합니다.
    -   **'전체' 탭**: `th:if="${type == 'ALL'}"` 블록 안에 각 카테고리별 미리보기(상위 5개) 내용을 표시합니다.
    -   **'개별' 탭**: `th:if="${type == 'RECIPE'}"` 블록 안에 해당 카테고리의 전체 목록과 페이지네이션 UI를 표시합니다. 다른 카테고리도 동일한 패턴으로 구현합니다.

-   **페이지네이션 UI 변경**:
    -   페이지 번호 버튼을 `<a>` 태그로 변경합니다.
    -   `th:href`를 사용하여 현재 `keyword`와 `type`을 유지한 채 `page` 파라미터만 변경된 URL을 생성합니다.
    -   **예시**:
        ```html
        <a class="page-link" 
           th:href="@{/search(keyword=${keyword}, type=${type}, page=${i})}"
           th:text="${i + 1}"></a>
        ```

## 3. 단계별 실행 계획

1.  **1단계: 코드 정리**
    -   `search/fragments.html` 파일을 삭제합니다.
    -   `results.html`에서 htmx 라이브러리, 관련 JavaScript, 모든 `hx-*` 속성을 깨끗하게 제거합니다.

2.  **2단계: 백엔드 단순화**
    -   `SearchController`에서 `searchFragments` 메소드를 삭제합니다.
    -   `SearchService`의 로직을 검토하여, `GET /search` 요청에 필요한 데이터만 명확하게 반환하도록 정리합니다. (페이지 객체 또는 미리보기 리스트)

3.  **3단계: 프론트엔드 SSR 구현**
    -   `results.html`의 탭과 페이지네이션을 `<a>` 태그와 `th:href`를 사용하는 방식으로 전면 수정합니다.
    -   `th:if`를 사용하여 `type` 파라미터에 따라 올바른 콘텐츠가 표시되는지 확인합니다.

4.  **4단계: 최종 테스트**
    -   탭 전환, 페이지 이동, '뒤로가기' 등 모든 기능이 전체 페이지 새로고침을 통해 안정적으로 동작하는지 검증합니다.
