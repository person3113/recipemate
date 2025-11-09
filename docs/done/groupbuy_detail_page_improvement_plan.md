# 공동구매 상세 페이지 개선 계획

## 1. 개요
본 문서는 공동구매 상세 페이지의 사용자 경험(UX) 및 기능적 문제점을 해결하기 위한 개선 계획을 다룬다. 주요 개선 사항은 다음과 같다.
- 구매 정보 UI 위치 및 반응형 동작 개선
- 데이터와 일치하는 배송 방법 텍스트 표시
- 사용자의 참여 상태(주최자, 참여자, 비참여자)에 따른 버튼 노출 로직 수정
- 주최자의 자기 공구 참여 허용
- 참여 취소 불가 시 버튼 비활성화
- 주최자 전용 기능(수정/삭제) 버튼 추가

```text

http://localhost:8080/group-purchases/545 이런 공구 상세보기에서 
아래 부분이 화면에 딱 고정돼서. 스크롤해도 그대로 보이지만. 일반적인 pc 화면에서도 위치가 상단헤더 부분을 가려서 별로임. 알맞은 위치에 위치하도록. 추가로 너비가 줄어들 때는 아예 공구 콘텐츠 바로 위에 위치가 고정돼서 더 불편. 이럴 때는 그냥 화면 제일 밑에 고정해서 할 수 있는 식으로 하든 뭘 하든 해서 해결해야 할 듯. ui는 실무와 best practice 기준으로 판단해줘.
<div class="card shadow-sm mb-4 sticky-top floating-buttons">
                    <div class="card-header bg-white">
                        <h5 class="mb-0 fw-bold">구매 정보</h5>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted">총 금액</span>
                                <span class="fs-4 fw-bold text-primary">0원</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center bg-light p-2 rounded">
                                <span class="text-muted small">
                                    <i class="bi bi-calculator"></i> 1인당 예상 가격
                                </span>
                                <span class="fw-semibold text-success">0원</span>
                            </div>
                        </div>
                        
                        <hr>
                        
                        <div class="mb-3">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted"><i class="bi bi-people"></i> 목표 인원</span>
                                <span class="fw-semibold">2명</span>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted"><i class="bi bi-person-check"></i> 현재 인원</span>
                                <span class="fw-semibold">0명</span>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted"><i class="bi bi-calendar-event"></i> 마감일</span>
                                <span class="fw-semibold">2025-11-23</span>
                            </div>
                        </div>
                        
                        <hr>
                        
                        <div class="mb-3">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted"><i class="bi bi-truck"></i> 배송 방법</span>
                                <span class="fw-semibold">택배</span>
                            </div>
                            
                            
                        </div>
                        
                        <!-- Action Buttons -->
                        <div>
                            <div class="d-grid gap-2 mb-2">
                                <button type="button" class="btn btn-primary btn-lg" data-bs-toggle="modal" data-bs-target="#participateModal">
                                    <i class="bi bi-basket3"></i> 참여하기
                                </button>
                            </div>
                            <div class="d-grid gap-2">
                                <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#cancelModal">
                                    <i class="bi bi-x-circle"></i> 참여 취소
                                </button>
                            </div>
                        </div>
                        
                        
                    </div>
                </div>

-[] 공동구매 수령방법 택배 -> 택배/직거래 가능 이렇게 바꿨는데. 상세 페이지에서는 배송 방법이 그대로 택배로 보이네.

-[] 공구 참여했으면 참여하기 버튼은 안 보이고 참여 취소 버튼만 보이게. + 참여안했으면 참여하기 버튼만 보이고 참여 취소 버튼은 안 보이게 (http://localhost:8080/group-purchases/34) + 주최자는 기본적으로 참여자로 치지 않고. 현재는 주최자가 개설하면 참여자가 0명으로 나오는데(주최자 제외). 그리고  참여자 목록 에서도 주최자는 표시안되니까. 그리고 현재 에러가 " 주최자는 자신의 공동구매에 참여할 수 없습니다." 이렇게 되어있는데 .이거 없애고 자기 공동구매 참여 가능하도록 하자. + http://localhost:8080/group-purchases/545/participants 이런 데서 참여자 목록 볼 때 자기 자신이 참여한 경우는 강제 탈퇴나 쪽지 보내기 버튼은 없어야 겠지.

- [] 현재 http://localhost:8080/group-purchases/194 이런공구 상세보기에서 마감이거든. 상태가. 근데 참여 취소 버튼이 보이네. 물론 에러 처리는 "마감 1일 전에는 참여를 취소할 수 없습니다." 이렇게 처리되어서 좋은데. 마감 1인 전부터는 아예 참여 취소 안 보이도록 하기.
```

## 2. 관련 파일
- **Controller**: `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
- **Service**: 
  - `src/main/java/com/recipemate/domain/groupbuy/service/GroupBuyService.java`
  - `src/main/java/com/recipemate/domain/groupbuy/service/ParticipationService.java`
- **Entity**: `src/main/java/com/recipemate/domain/groupbuy/entity/GroupBuy.java`
- **DTO**: 
  - `src/main/java/com/recipemate/domain/groupbuy/dto/GroupBuyResponse.java`
  - `src/main/java/com/recipemate/domain/groupbuy/dto/ParticipantResponse.java`
- **Template**:
  - `src/main/resources/templates/group-purchases/detail.html`
  - `src/main/resources/templates/group-purchases/participants.html`
- **CSS**: `src/main/resources/static/css/styles.css`

## 3. 상세 개선 계획

### 이슈 1: 구매 정보 박스 UI 개선
- **문제**: `sticky-top` 클래스로 인해 PC 화면에서 헤더를 가리고, 모바일 화면에서는 레이아웃이 부자연스러움.
- **해결 방안**:
    1. **`detail.html` 수정**:
        - 기존 `<div class="card shadow-sm mb-4 sticky-top floating-buttons">` 에서 `sticky-top` 클래스를 제거한다.
    2. **`styles.css` 수정**:
        - PC 화면에서는 자연스럽게 스크롤되도록 두고, 모바일 화면(e.g., `max-width: 991px`)에서는 화면 하단에 고정되는 UI를 적용한다.
        ```css
        /* styles.css 파일에 추가 */
        @media (max-width: 991.98px) {
            .floating-buttons {
                position: fixed;
                bottom: 0;
                left: 0;
                right: 0;
                z-index: 1030; /* 헤더와 겹치지 않도록 */
                margin-bottom: 0 !important;
                border-radius: 0;
                border-top: 1px solid #dee2e6;
            }
        
            /* 콘텐츠가 하단 버튼에 가려지지 않도록 main 영역에 padding-bottom 추가 */
            main.container {
                padding-bottom: 200px; /* floating-buttons 높이에 맞춰 조정 */
            }
        }
        ```
    3. **`detail.html` 수정 (모바일용 레이아웃)**:
        - 모바일 하단 고정 시에는 버튼만 보이도록 card-body의 일부 내용을 숨기는 것을 고려할 수 있다. 예를 들어, `d-lg-block` 클래스를 활용해 PC에서만 보이게 처리한다.

### 이슈 2: 배송 방법 표시 오류
- **문제**: `DeliveryMethod`가 `BOTH`("택배/직거래 가능")인 경우에도 "택배"로만 표시됨.
- **분석**: `detail.html`의 `th:text` 조건문이 `DIRECT`만 확인하고 나머지를 모두 '택배'로 처리하고 있음.
- **해결 방안**:
    1. **`detail.html` 수정**:
        - `th:text` 표현식을 `DeliveryMethod`의 모든 케이스(`DIRECT`, `PARCEL`, `BOTH`)를 처리하도록 수정한다.
        ```html
        <!-- 기존 코드 -->
        <span class="fw-semibold" th:text="${groupBuy.deliveryMethod == 'DIRECT' ? '직거래' : '택배'}">직거래</span>

        <!-- 수정 후 코드 -->
        <span class="fw-semibold" 
              th:text="${groupBuy.deliveryMethod.displayName}">택배/직거래</span>
        ```
    2. **`DeliveryMethod.java` Enum 수정**:
        - 각 Enum 상수에 `displayName` 필드를 추가하여 UI에 표시할 텍스트를 관리한다.
        ```java
        // global/common/DeliveryMethod.java
        @Getter
        @RequiredArgsConstructor
        public enum DeliveryMethod {
            DIRECT("직거래"),
            PARCEL("택배"),
            BOTH("택배/직거래");
        
            private final String displayName;
        }
        ```

### 이슈 3: 참여 관련 로직 수정
- **문제**: 주최자 참여 불가, 참여 여부와 관계없이 버튼이 모두 노출됨, 참여자 목록에서 자신에게 불필요한 버튼 노출.
- **해결 방안**:
    1. **주최자 참여 허용**:
        - **`GroupBuy.java` 수정**: `addParticipant` 메서드에서 주최자 참여를 막는 코드를 제거한다.
        ```java
        // GroupBuy.java > addParticipant()
        // 3. 주최자 본인 참여 불가 검증 (해당 라인 제거)
        // if (isHost(participant)) {
        //     throw new CustomException(ErrorCode.HOST_CANNOT_PARTICIPATE);
        // }
        ```
    2. **사용자 상태(주최자/참여자)에 따른 버튼 노출**:
        - **`GroupBuyResponse.java` 수정**: 사용자의 상태를 담을 boolean 필드를 추가한다.
        ```java
        // GroupBuyResponse.java
        // ... 기존 필드 ...
        private boolean isHost;
        private boolean isParticipant;
        
        // 빌더에 추가
        public static GroupBuyResponse from(GroupBuy groupBuy, List<String> imageUrls, boolean isHost, boolean isParticipant) {
            return GroupBuyResponse.builder()
                // ... 기존 빌더 설정 ...
                .isHost(isHost)
                .isParticipant(isParticipant)
                .build();
        }
        ```
        - **`GroupBuyService.java` 수정**: `getGroupBuyDetail` 메서드에서 `isHost`, `isParticipant` 값을 계산하여 `GroupBuyResponse`를 생성하도록 수정한다.
        ```java
        // GroupBuyService.java > getGroupBuyDetail(Long groupBuyId, UserDetails userDetails)
        // ...
        User user = /* userDetails로부터 User 객체 조회 */;
        boolean isHost = groupBuy.isHost(user);
        boolean isParticipant = participationRepository.existsByUserIdAndGroupBuyId(user.getId(), groupBuyId);
        
        return GroupBuyResponse.from(groupBuy, imageUrls, isHost, isParticipant);
        ```
        - **`GroupBuyController.java` 수정**: `detailPage` 메서드에서 `userDetails`를 `groupBuyService`로 넘겨준다.
        - **`detail.html` 수정**: `th:if`를 사용하여 버튼 노출 로직을 구현한다.
        ```html
        <!-- Action Buttons -->
        <div sec:authorize="isAuthenticated()">
            <!-- 참여하기 버튼: 모집중이고, 참여자가 아닐 때 -->
            <div th:if="${groupBuy.status.name() == 'RECRUITING' and !groupBuy.isParticipant}" class="d-grid gap-2 mb-2">
                <button type="button" class="btn btn-primary btn-lg" data-bs-toggle="modal" data-bs-target="#participateModal">
                    <i class="bi bi-basket3"></i> 참여하기
                </button>
            </div>
            <!-- 참여 취소 버튼: 참여자이고, 취소 가능할 때 -->
            <div th:if="${groupBuy.isParticipant and groupBuy.isCancellable}" class="d-grid gap-2">
                <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#cancelModal">
                    <i class="bi bi-x-circle"></i> 참여 취소
                </button>
            </div>
        </div>
        ```
    3. **참여자 목록에서 자신에게 버튼 숨기기**:
        - **`participants.html` 수정**: 참여자 목록을 순회할 때, 현재 사용자와 참여자가 동일한 경우 버튼이 보이지 않도록 `th:if` 조건을 추가한다.
        ```html
        <!-- participants.html 내 참여자 목록 루프 -->
        <div th:if="${#authentication.principal.id != participant.userId}" class="ms-auto">
            <!-- 강제 탈퇴, 쪽지 보내기 등 버튼 -->
        </div>
        ```
        - **`ParticipantResponse.java` 수정**: `userId` 필드가 DTO에 포함되어 있는지 확인한다. (현재 구조상 포함되어 있을 것으로 예상)

### 이슈 4: 마감 임박 시 참여 취소 버튼 비활성화
- **문제**: 마감 1일 전이라 취소가 불가능함에도 '참여 취소' 버튼이 노출됨.
- **분석**: `GroupBuy.java`의 `cancelParticipation` 메서드에 `deadline.minusDays(1)` 로직이 이미 존재함. 이 상태를 View에 전달해야 함.
- **해결 방안**:
    1. **`GroupBuyResponse.java` 수정**: 취소 가능 여부를 담을 `isCancellable` 필드를 추가한다.
        ```java
        // GroupBuyResponse.java
        private boolean isCancellable;
        ```
    2. **`GroupBuyService.java` 수정**: `getGroupBuyDetail`에서 `isCancellable` 값을 계산한다.
        ```java
        // GroupBuyService.java > getGroupBuyDetail
        boolean isCancellable = groupBuy.getStatus() == GroupBuyStatus.RECRUITING && 
                                LocalDateTime.now().isBefore(groupBuy.getDeadline().minusDays(1));
        // Response DTO 생성 시 이 값을 포함
        ```
    3. **`detail.html` 수정**: '참여 취소' 버튼의 `th:if` 조건에 `groupBuy.isCancellable`을 추가한다. (위 3-2 항목의 코드 예시 참고)

### 이슈 5: 주최자 전용 기능 버튼 추가
- **문제**: 주최자가 자신의 공구 상세 페이지에 들어갔을 때 수정/삭제 등 관리 기능이 없음.
- **해결 방안**:
    1. **`detail.html` 수정**: `isHost` 플래그를 사용하여 주최자에게만 '수정'과 '삭제' 버튼이 보이도록 한다. 이 버튼들은 기존 `GroupBuyController`의 `editPage`, `deleteGroupBuy` 엔드포인트와 연결한다.
    ```html
    <!-- detail.html, 구매 정보 카드 하단 또는 페이지 상단 등 적절한 위치에 추가 -->
    <div th:if="${groupBuy.isHost}" class="d-grid gap-2 mt-3 border-top pt-3">
        <h6 class="text-muted fw-bold">주최자 메뉴</h6>
        <a th:href="@{/group-purchases/{id}/edit(id=${groupBuy.id})}" class="btn btn-outline-secondary">
            <i class="bi bi-pencil-square"></i> 공구 수정
        </a>
        <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#deleteModal">
            <i class="bi bi-trash"></i> 공구 삭제
        </button>
    </div>

    <!-- 삭제 확인 Modal 추가 -->
    <div th:if="${groupBuy.isHost}" class="modal fade" id="deleteModal" tabindex="-1">
        <!-- ... 삭제 확인 Modal 구현 ... -->
        <form method="post" th:action="@{/group-purchases/{id}/delete(id=${groupBuy.id})}">
            <!-- ... form content ... -->
        </form>
    </div>
    ```

이 계획에 따라 수정하면 요청된 모든 사항이 해결될 것으로 기대된다.
