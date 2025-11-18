# 지도 기능 추가 계획 (Task 4-5-5)

본 문서는 카카오 지도 API를 연동하여 공동구매 만남 장소를 지도에 표시하는 기능 구현 계획을 상세히 기술합니다.

## 1. 목표

- 공동구매 생성/수정 시 지도에서 만남 장소를 선택하고 좌표를 저장합니다.
- 공동구매 상세 페이지에서 저장된 만남 장소의 위치를 지도로 표시합니다.

## 2. 기술 스택 및 API

- **Backend:** Spring Boot, Java, JPA (Hibernate)
- **Frontend:** Thymeleaf, JavaScript, Kakao Maps JavaScript SDK
- **API:** Kakao Maps JavaScript SDK (주소 검색, 좌표-주소 변환) 및 Kakao Maps REST API (필요시)

## 3. 구현 계획

### 3.1. 백엔드 (Backend)

#### 3.1.1. `GroupBuy` 엔티티 확장

`GroupBuy` 엔티티에 위도(`latitude`)와 경도(`longitude`) 필드를 추가합니다. 장소 선택은 필수가 아닐 수 있으므로 `Nullable`로 설정합니다.

- **파일:** `src/main/java/com/recipemate/domain/groupbuy/entity/GroupBuy.java`
- **변경 사항:**
    - `private Double latitude;`
    - `private Double longitude;`
    - 두 필드에 `@Column` 어노테이션을 추가합니다.

#### 3.1.2. DTO 수정

`GroupBuy` 엔티티 변경에 따라 관련 DTO (Data Transfer Object)들을 수정합니다. `latitude`, `longitude` 필드를 추가해야 할 DTO를 검색하여 모두 수정합니다. (예: `GroupBuyForm`, `GroupBuyDetailResponse`)

#### 3.1.3. `GroupBuyService` 수정

공동구매 생성 및 수정 로직에서 DTO로부터 받은 `latitude`, `longitude` 값을 `GroupBuy` 엔티티에 저장하도록 수정합니다.

#### 3.1.4. API 키 보안 관리 (IDE 및 Gradle 실행 모두 지원)

카카오 지도 JavaScript API 키는 Git 저장소에 노출되지 않도록 안전하게 관리해야 합니다. IDE의 실행 버튼(▶)과 `gradle bootRun` 명령어 실행 모두에서 키가 동작하도록, `.env` 파일과 `application-dev.local.yml` 파일을 함께 사용하는 이중 설정 방식을 적용합니다.

1.  **`application.yml` 설정 추가:**
    `src/main/resources/application.yml` 파일에 카카오 JavaScript 키를 위한 속성을 추가합니다. 이 설정은 환경 변수 또는 로컬 프로필 파일에서 값을 가져옵니다.
    ```yml
    # application.yml 파일의 최상단 또는 kakao 관련 설정이 있다면 그 아래에 추가
    kakao:
      javascript-key: ${KAKAO_JAVASCRIPT_KEY}
    ```

2.  **`.env` 파일에 키 추가 (Gradle 실행용):**
    프로젝트 루트의 `.env` 파일에 키를 추가합니다. `build.gradle`의 `bootRun` 작업이 이 파일을 읽어 환경 변수로 설정해줍니다.
    ```
    # .env 파일 맨 아래에 추가
    KAKAO_JAVASCRIPT_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxx
    ```

3.  **`application-dev.local.yml`에 키 추가 (IDE 실행용):**
    `src/main/resources/application-dev.local.yml` 파일에도 키를 추가합니다. Spring Boot는 `dev` 프로필 실행 시 이 파일을 자동으로 로드하여 `kakao.javascript-key` 속성 값을 직접 설정해줍니다.
    ```yml
    # application-dev.local.yml 파일에 추가
    kakao:
      javascript-key: xxxxxxxxxxxxxxxxxxxxxxxxxxx
    ```
    *참고: `application.yml`의 `${...}` 환경 변수 방식보다 `application-dev.local.yml`에 직접 값을 넣는 것이 우선순위가 더 높을 수 있습니다. 두 방식 모두 설정해두면 어떤 방식으로 실행하든 안전하게 키를 사용할 수 있습니다.*

4.  **Controller에서 키 주입 및 Model에 추가:**
    지도가 필요한 페이지의 Controller에서 `@Value`로 키를 주입받아 Model에 추가합니다.
    ```java
    @Value("${kakao.javascript-key}")
    private String kakaoJavascriptKey;
    // ...
    model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);
    ```

5.  **Thymeleaf 템플릿 수정:**
    레이아웃 파일에서 `th:src`를 사용해 Model의 키 값을 읽어오도록 합니다.
    ```html
    <script th:if="${kakaoJavascriptKey != null}" th:src="|//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJavascriptKey}&libraries=services|"></script>
    ```

### 3.2. 프론트엔드 (Frontend)

#### 3.2.1. 카카오 지도 JavaScript SDK 추가

지도를 표시할 모든 페이지의 `<head>`에 SDK 스크립트를 추가합니다. 모든 페이지에 공통으로 적용되는 레이아웃 파일을 수정하는 것이 효율적입니다.

- **추정 파일:** `src/main/resources/templates/layout/default_layout.html` (또는 유사한 공통 레이아웃 파일)
- **변경 사항:**
  ```html
  <script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=발급받은_JAVASCRIPT_KEY&libraries=services"></script>
  ```
- `services` 라이브러리를 포함하여 주소 검색 및 좌표-주소 변환 기능을 사용합니다.
- **참고:** `발급받은_JAVASCRIPT_KEY`는 카카오 개발자 사이트에서 발급받은 JavaScript 키로 교체해야 합니다.

#### 3.2.2. 공동구매 생성/수정 페이지 (`form.html`) - 지도와 주소 입력 통합

- **파일:** `src/main/resources/templates/group-purchases/form.html`
- **UI 변경:**
    - 기존 `meetupLocation` 텍스트 입력 필드를 그대로 사용하고, 그 옆에 '주소 검색' 버튼을 추가하여 기능을 연동합니다.
    - 지도를 표시할 `<div id="map" style="width:100%;height:350px;"></div>` 영역을 `meetupLocation` 필드 아래에 추가합니다.
    - 선택된 좌표를 서버로 전송하기 위한 `hidden` input 필드를 `th:field`와 함께 추가합니다.
      ```html
      <input type="hidden" th:field="*{latitude}" id="latitude">
      <input type="hidden" th:field="*{longitude}" id="longitude">
      ```
- **JavaScript 구현 (통합 로직):**
    1.  **지도 생성:** 페이지 로드 시 기본 위치(또는 수정 시 기존 위치)로 지도를 생성하고 마커를 표시합니다.
    2.  **주소 검색 및 연동:**
        - '주소 검색' 버튼 클릭 시, `meetupLocation` 필드에 입력된 값으로 주소 검색(`addressSearch`)을 실행합니다.
        - 검색 결과가 있으면 해당 위치로 지도를 이동시키고 마커를 표시합니다.
        - 검색된 주소의 전체 주소 문자열을 `meetupLocation` 필드에 다시 채워 넣어 표준화된 주소로 업데이트합니다.
        - 해당 위치의 위도와 경도를 `hidden` input 필드(`latitude`, `longitude`)에 저장합니다.
    3.  **지도 클릭 및 연동:**
        - 지도를 클릭하면 해당 위치에 마커를 표시합니다.
        - `coord2Address`를 통해 좌표를 주소로 변환하고, 그 결과를 `meetupLocation` 필드에 채웁니다.
        - 클릭된 위치의 위도와 경도를 `hidden` input 필드에 저장합니다.
    4.  **최종 정보:** 사용자는 자동으로 채워진 `meetupLocation` 필드에 "3번 출구 앞"과 같은 상세 정보를 자유롭게 추가할 수 있습니다. 폼 제출 시, 이 상세 설명과 좌표가 함께 서버로 전송됩니다.

#### 3.2.3. 공동구매 상세 페이지 (`detail.html`)

- **파일:** `src/main/resources/templates/group-purchases/detail.html`
- **UI 변경:**
    - 만남 장소 정보를 표시할 영역에 지도를 표시할 `<div id="map" style="width:100%;height:350px;"></div>`를 추가합니다.
- **JavaScript 구현:**
    1.  Thymeleaf를 사용하여 서버에서 전달된 `groupBuy.latitude`와 `groupBuy.longitude` 값이 있는지 확인합니다.
        ```html
        <script th:inline="javascript">
            /*<![CDATA[*/
            var latitude = /*[[${groupBuy.latitude}]]*/ null;
            var longitude = /*[[${groupBuy.longitude}]]*/ null;
            /*]]>*/
        </script>
        ```
    2.  좌표 값이 있는 경우에만 해당 좌표를 중심으로 지도를 생성하고 마커를 표시하는 스크립트를 실행합니다.
    3.  좌표 값이 없으면 지도 영역이 보이지 않도록 처리합니다.

## 4. 작업 순서

1.  **카카오 개발자 사이트 설정:**
    - 애플리케이션 생성 및 JavaScript 키 발급.
    - `http://localhost:8080` (로컬 개발 환경)을 사이트 도메인으로 등록.
2.  **백엔드 개발:**
    - `GroupBuy` 엔티티 및 관련 DTO에 `latitude`, `longitude` 필드 추가.
    - `GroupBuyService`의 생성/수정 로직에 좌표 저장 기능 추가.
3.  **프론트엔드 개발 (공통):**
    - 공통 레이아웃에 카카오 지도 SDK 스크립트 태그 추가.
4.  **프론트엔드 개발 (생성/수정):**
    - `form.html`에 지도 관련 UI 요소 및 JavaScript 로직 추가.
5.  **프론트엔드 개발 (상세):**
    - `detail.html`에 저장된 좌표로 지도를 표시하는 UI 및 JavaScript 로직 추가.
6.  **테스트:**
    - 공동구매 생성/수정 시 장소 선택 및 저장이 정상적으로 동작하는지 확인.
    - 상세 페이지에서 지도가 올바르게 표시되는지 확인.
