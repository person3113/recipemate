# 공동구매 기능 개선 계획 (수정본 v2)

## 1. 개요

제안에 대한 피드백을 반영하여 계획을 수정했습니다. '삭제'와 '취소'의 개념을 명확히 분리하고, `ReviewController`를 재활용하는 방향으로 설계를 개선했습니다.

1.  **공동구매 상태 재정의**: `GroupBuyStatus` Enum에 '완료'와 '취소' 상태를 추가합니다.
2.  **'참여자 관리' 기능 개선**: 마감된 공구의 상태가 '모집중'으로 변경되는 심각한 버그를 수정하고, 버튼 표시 규칙을 명확히 합니다.
3.  **'공구 취소' 및 '기록 지우기' 기능 구현**: 기존 '삭제' 기능을 '취소'와 '기록 지우기' 두 단계로 분리하여 사용자 경험과 데이터 관리 효율을 높입니다.
4.  **'후기 보기' 기능 구현**: `ReviewController`를 활용하여 '완료'된 공구의 후기 목록을 보여주는 기능을 구현합니다.

## 2. 세부 개선 방안

### 1) 공동구매 상태 (GroupBuyStatus) 재정의

- **개선 방안**:
    - `src/main/java/com/recipemate/global/common/GroupBuyStatus.java` Enum에 아래 두 상태를 추가합니다.
        - `COMPLETED("모집 성공")`
        - `CANCELLED("판매자 취소")`
    - `CLOSED`는 '모집 실패(기간 만료)'의 의미로 사용합니다.
    - **(추가 제안)** `GroupBuyService`에 스케줄러(`@Scheduled`)를 구현하여 마감된 공구 상태를 `COMPLETED` 또는 `CLOSED`로 자동 변경합니다.

### 2) '참여자 관리' 기능 개선 및 버그 수정

- **개선 방안**:
    - **버그 수정**: `src/main/java/com/recipemate/domain/groupbuy/entity/GroupBuy.java`의 `forceRemoveParticipant`와 `cancelParticipation` 메서드에서, `COMPLETED`나 `CANCELLED`, `CLOSED` 같은 최종 상태에서는 `reopen()`이 호출되지 않도록 방어 로직을 강화합니다.
    - **버튼 표시 규칙 수정**: `group-purchases/detail.html`에서 '참여자 관리' 버튼이 공구 주최인에게만, 그리고 `status`가 `RECRUITING` 또는 `IMMINENT`일 때만 보이도록 `th:if` 조건을 수정합니다.

### 3) '공구 취소' 및 '기록 지우기' 기능 구현

- **현황**: `deleteGroupBuy` 메서드가 soft-delete를 수행하지만, '취소'라는 비즈니스적 상태 변경과 '삭제'라는 데이터 관리 기능이 혼재되어 있습니다.
- **개선 방안**: 두 가지 기능으로 분리합니다.
    1.  **공구 취소 (1단계)**
        - **UI**: `detail.html`의 기존 '삭제' 버튼을 **'공구 취소'**로 변경합니다.
        - **조건**: 이 버튼은 주최자가 `RECRUITING` 또는 `IMMINENT` 상태의 공구에서만 볼 수 있습니다.
        - **로직**: `GroupBuyService`에 `cancelGroupBuy` 라는 새 메서드를 만듭니다. 이 메서드는 참여자가 없는지 확인한 후, `groupBuy.updateStatus(GroupBuyStatus.CANCELLED)`를 호출하여 상태만 변경합니다. (이미지 등 데이터는 모두 유지)
    2.  **기록 지우기 (2단계)**
        - **UI**: `user/my-group-purchases.html` (내 공구 목록)에 **'기록 지우기'** 버튼을 새로 추가합니다.
        - **조건**: 이 버튼은 `CLOSED` 또는 `CANCELLED` 상태인 공구에만 표시됩니다.
        - **로직**: 이 버튼은 기존의 `GroupBuyService.deleteGroupBuy` 메서드를 호출하도록 연결합니다. 이 메서드는 `deleted_at`을 기록하고(soft-delete), 연관된 이미지도 Cloudinary에서 삭제하는 등, 데이터를 정리하는 역할을 수행합니다.

### 4) '후기 보기' 기능 구현 (SSR, ReviewController 재활용)

- **현황**: `ReviewController`가 후기 관련 기능을 이미 담당하고 있으며, `GroupBuyService`는 후기 요약 정보를 조회하고 있습니다.
- **문제점**: 후기 전체 목록을 보여주는 페이지가 없습니다.
- **개선 방안**: **`GroupBuyController`가 아닌 `ReviewController`를 재활용하여 구현합니다.**
    - **Backend**:
        1.  `src/main/java/com/recipemate/domain/review/controller/ReviewController.java`에 후기 목록 전체 페이지를 보여줄 새 GET 메서드를 추가합니다.
            ```java
            // ReviewController.java 에 추가
            @GetMapping
            public String listReviewsForGroupBuy(@PathVariable Long purchaseId, Model model) {
                // 1. 공구 정보 조회 (페이지 제목 등에 사용)
                GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
                // 2. 후기 목록 조회
                List<ReviewResponse> reviews = reviewService.getReviewsByGroupBuy(purchaseId);
                
                model.addAttribute("groupBuy", groupBuy);
                model.addAttribute("reviews", reviews);
                
                return "reviews/list"; // 후기 목록 전체 페이지
            }
            ```
        2.  `ReviewService`의 `getReviewsByGroupBuy` 메서드가 `List<ReviewResponse>`를 반환하는지 확인하고, 없다면 구현합니다.
    - **Frontend**:
        1.  `group-purchases/detail.html`과 `user/my-group-purchases.html`에 '후기 보기' 버튼을 추가하고, `groupPurchase.status == 'COMPLETED'`일 때만 표시되도록 합니다.
        2.  버튼 클릭 시 `@{/group-purchases/{id}/reviews}` URL로 이동하도록 링크를 설정합니다.
        3.  `src/main/resources/templates/reviews/list.html` 뷰 파일을 생성하여, `th:each`로 후기 목록을 표시합니다.

## 3. 수정 대상 파일 (정확한 경로)

- **Java (Backend)**:
    - `src/main/java/com/recipemate/global/common/GroupBuyStatus.java`
    - `src/main/java/com/recipemate/domain/groupbuy/entity/GroupBuy.java`
    - `src/main/java/com/recipemate/domain/groupbuy/service/GroupBuyService.java`
    - `src/main/java/com/recipemate/domain/review/controller/ReviewController.java`
    - `src/main/java/com/recipemate/domain/review/service/ReviewService.java`

- **Templates (Frontend)**:
    - `src/main/resources/templates/group-purchases/detail.html`
    - `src/main/resources/templates/user/my-group-purchases.html`
    - `src/main/resources/templates/reviews/list.html`
