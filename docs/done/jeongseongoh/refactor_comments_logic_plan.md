# 댓글 기능 오류 분석 및 리팩토링 계획

## 1. 문제 상황

- **현상**: 공동구매 및 커뮤니티 게시글 상세 페이지에서 댓글/대댓글 수정 및 삭제 시, 그리고 대댓글 작성 시 `MissingServletRequestParameterException` 오류가 발생하며 기능이 정상적으로 동작하지 않는다.
- **오류 로그**:
    - **대댓글 생성 시**: `Required request parameter 'targetType' for method parameter type EntityType is not present`
    - **댓글 수정/삭제 시**: `Required request parameter 'targetType' for method parameter type EntityType is present but converted to null`

## 2. 원인 분석

`codebase_investigator` 도구를 통해 코드 베이스를 분석한 결과, 오류의 원인은 프론트엔드 템플릿과 백엔드 컨트롤러 간의 데이터 불일치였습니다.

### 2.1. 대댓글 생성 오류: `targetType` 파라미터 누락

- **파일**: `src/main/resources/templates/fragments/comments.html`
- **문제**: 대댓글(reply)을 작성하는 폼(`id="reply-form"`)에는 댓글의 대상이 되는 엔티티(게시글 또는 공동구매)의 타입(`targetType`)과 ID(`targetId`)를 서버로 전송하는 hidden input 필드가 누락되어 있습니다.
- **결과**: `CommentController`의 `createComment` 메소드는 `@RequestParam("targetType")`을 필수 값으로 요구하므로, 해당 파라미터가 요청에 포함되지 않아 `MissingServletRequestParameterException` 예외가 발생합니다.

### 2.2. 댓글 수정/삭제 오류: `targetType` Enum 변환 실패

- **파일**:
    - `src/main/java/com/recipemate/global/common/EntityType.java`
    - `src/main/resources/templates/group-purchases/detail.html`
    - `src/main/resources/templates/community-posts/detail.html`
- **문제**:
    1.  `EntityType` Enum은 `GROUP_BUY`, `POST`와 같이 **대문자**로 상수가 정의되어 있습니다.
    2.  Spring의 기본 Enum 변환기는 대소문자를 엄격하게 구분합니다.
    3.  `group-purchases/detail.html`과 `community-posts/detail.html`에서 `comments.html` 프래그먼트를 포함할 때, `targetType` 변수에 각각 **소문자** `'group_buy'`와 `'post'`를 전달하고 있습니다.
- **결과**: 서버는 소문자로 된 `targetType` 값을 `EntityType` Enum으로 변환하지 못하고 `null`을 반환합니다. `CommentController`의 수정 및 삭제 메소드는 이 `targetType` 파라미터를 필수(`required=true`)로 간주하므로, `null` 값이 들어오자 `MissingServletRequestParameterException` 예외를 발생시킵니다.

## 3. 리팩토링 계획

프론트엔드 템플릿의 데이터 전송 방식을 수정하여 백엔드 요구사항에 맞게 변경합니다.

### 3.1. 대댓글 폼 수정

- **대상 파일**: `src/main/resources/templates/fragments/comments.html`
- **수정 사항**:
    - 대댓글 작성 폼(`id="reply-form"`) 내부에 `targetType`과 `targetId`를 전송할 수 있도록 두 개의 hidden input을 추가합니다.
    - 이 값들은 페이지 로드 시 전달된 `targetType`과 `targetId` 변수를 사용합니다.

    ```html
    <!-- 대댓글 폼 내부 -->
    <form th:action="@{/comments}" method="post" class="reply-form" style="display: none;">
        <!-- 기존 필드... -->
        <input type="hidden" name="parentId" class="parent-comment-id">
        <input type="hidden" name="targetId" th:value="${targetId}">
        <input type="hidden" name="targetType" th:value="${targetType}">
        <button type="submit">대댓글 등록</button>
    </form>
    ```

### 3.2. `targetType` 값 대문자로 변경

- **대상 파일**:
    1.  `src/main/resources/templates/group-purchases/detail.html`
    2.  `src/main/resources/templates/community-posts/detail.html`
- **수정 사항**:
    - `th:replace`를 사용하여 `comments.html` 프래그먼트를 호출하는 부분에서 `targetType`으로 전달하는 값을 **대문자**로 변경합니다.

    **`group-purchases/detail.html`**:
    ```html
    <!-- 변경 전 -->
    <div th:replace="~{fragments/comments :: commentsFragment(targetType='group_buy', targetId=${groupBuy.id}, comments=${comments})}"></div>

    <!-- 변경 후 -->
    <div th:replace="~{fragments/comments :: commentsFragment(targetType='GROUP_BUY', targetId=${groupBuy.id}, comments=${comments})}"></div>
    ```

    **`community-posts/detail.html`**:
    ```html
    <!-- 변경 전 -->
    <div th:replace="~{fragments/comments :: commentsFragment(targetType='post', targetId=${post.id}, comments=${comments})}"></div>

    <!-- 변경 후 -->
    <div th:replace="~{fragments/comments :: commentsFragment(targetType='POST', targetId=${post.id}, comments=${comments})}"></div>
    ```

## 4. 기대 효과

- 위 리팩토링을 통해 `targetType` 파라미터가 모든 댓글 관련 요청(생성, 수정, 삭제, 대댓글)에서 올바른 값으로 일관되게 서버에 전달됩니다.
- `MissingServletRequestParameterException` 오류가 해결되어 댓글 기능이 정상적으로 동작할 것입니다.
- API 명세와 클라이언트 구현 간의 불일치가 해소되어 코드의 안정성과 예측 가능성이 향상됩니다.
