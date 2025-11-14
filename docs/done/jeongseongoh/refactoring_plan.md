# Group-Buy 리팩토링 계획

## 1. 문제 상황

현재 `내 공구` 탭에서 아래와 같은 404 오류가 발생하고 있습니다.

1.  **참여자 관리 페이지 접근 불가**: `[참여자 관리]` 버튼 클릭 시 `GET /group-purchases/{id}/participants` 요청이 404 오류를 반환합니다.
2.  **공구 삭제 기능 오작동**: `[삭제]` 버튼 클릭 시 `DELETE /api/group-purchases/{id}` 요청이 404 오류를 반환합니다.

## 2. 원인 분석

`codebase_investigator`를 통해 분석한 결과, 원인은 다음과 같습니다.

- **참여자 관리 (404)**: `GroupBuyController`에 `GET /group-purchases/{id}/participants` 요청을 처리할 핸들러 메소드가 구현되어 있지 않습니다. `URL_DESIGN.md` 명세와 프론트엔드 호출은 올바르지만, 백엔드 구현이 누락된 상태입니다.

- **공구 삭제 (404)**: 프론트엔드(`my-group-purchases.html`)에서 `URL_DESIGN.md` 명세와 다르게 잘못된 URL로 요청을 보내고 있습니다.
    - **기대 동작 (명세)**: `POST /group-purchases/{id}/delete`
    - **실제 동작 (프론트엔드)**: `DELETE /api/group-purchases/{id}`
    - `/api` 접두사, `DELETE` 메소드 사용, `/delete` 경로 누락 등 여러 부분이 명세와 일치하지 않습니다.

## 3. 리팩토링 제안 (Tasks)

### Task 1: 참여자 관리 페이지 구현

- **목표**: 누락된 참여자 관리 페이지 및 핸들러를 구현합니다.
- **기획 요구사항** (`7-1_내공구탭.md` 기반):
    - **화면**: 참여자 목록을 팝업 또는 페이지 형태로 표시합니다.
    - **표시 정보**: 참여자 닉네임, 매너온도, 참여일, 수령 방법, 수량
    - **액션**: 1:1 쪽지 보내기(todo로 주석처리하고 넘어가기), 강제 탈퇴 기능
- **수정 파일**:
    1.  `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
    2.  `src/main/resources/templates/group-purchases/participants.html` (신규 생성)
- **작업 내용**:
    1.  `GroupBuyController`에 `@GetMapping("/{purchaseId}/participants")` 핸들러 메소드를 추가합니다.
    2.  메소드는 `purchaseId`를 받아 해당 공구의 참여자 목록을 조회합니다.
    3.  조회된 데이터를 모델에 담아 `group-purchases/participants` 뷰를 렌더링합니다.
    4.  `participants.html` 템플릿을 새로 생성하여 기획에 맞게 참여자 정보를 표시합니다.

### Task 2: 공구 삭제 기능 수정

- **목표**: 프론트엔드의 삭제 요청을 `URL_DESIGN.md` 명세에 맞게 수정하고, 기획 요구사항을 반영합니다.
- **기획 요구사항** (`7-1_내공구탭.md` 기반):
    - **참여자 확인**: 삭제 시도 시, 참여자가 있는지 확인해야 합니다.
        - 참여자가 있으면 "참여자가 있어 삭제할 수 없습니다"와 같은 메시지를 표시하고 삭제를 막아야 합니다.
        - 참여자가 없으면 "정말 삭제하시겠습니까?" 확인 후 삭제를 진행합니다.
    - **소프트 삭제**: DB에서 데이터를 완전히 삭제하는 것이 아니라, 상태를 '취소'로 변경합니다.
- **수정 파일**:
    1.  `src/main/resources/templates/user/my-group-purchases.html`
    2.  `src/main/java/com/recipemate/domain/groupbuy/service/GroupBuyService.java` (삭제 로직 수정)
- **작업 내용**:
    1.  **프론트엔드**: `my-group-purchases.html`의 삭제 form을 수정합니다.
        - `th:action`을 `@{/group-purchases/{id}/delete(id=${groupBuy.id})}`로 변경합니다.
        - `_method` hidden input을 제거합니다.
    2.  **백엔드**: `GroupBuyService`의 `deleteGroupBuy` 메소드 로직을 수정합니다.
        - 삭제 전, 해당 공구에 참여자가 있는지 확인하는 로직을 추가합니다.
        - 참여자가 있으면 `CustomException`을 발생시켜 사용자에게 알립니다.
        - 참여자가 없으면 기존 로직대로 상태를 변경하여 소프트 삭제를 수행합니다.

### Task 3: 아키텍처 일관성 확보

- **논의**: 현재 코드베이스에는 `_method`를 이용한 RESTful 방식과 `POST`만 사용하는 방식이 혼재되어 있습니다. `URL_DESIGN.md`는 `POST` 사용을 권장하므로, 팀의 합의를 통해 하나의 전략으로 통일해야 합니다.
- **권장 사항**: `URL_DESIGN.md`의 가이드라인에 따라, Thymeleaf 폼에서는 `POST`를 사용하여 작업을 처리하는 방식으로 통일합니다. 이를 위해 다른 부분에서도 `_method`를 사용하고 있다면 점진적으로 제거해나가는 것이 좋습니다.

## 4. 기대 효과

- `내 공구` 탭의 참여자 관리 및 삭제 기능이 기획에 맞게 정상적으로 동작합니다.
- 프론트엔드와 백엔드의 URL 호출 방식이 `URL_DESIGN.md` 명세와 일치하여 코드의 일관성과 예측 가능성이 향상됩니다.
- 향후 유사 기능 개발 시 발생할 수 있는 혼란을 방지합니다.
