# 공동구매 후기 및 레시피 관련 후기 표시 시스템 구현 계획 (v5 - 최종)

## 1. 목표

- **공동구매 후기**: 사용자가 참여하여 `마감(CLOSED)`된 공동구매에 대해 별점과 내용이 포함된 후기를 작성, 조회, 수정, 삭제하는 기능을 구현한다.
- **레시피 관련 후기 표시**: 레시피 상세 페이지에서 해당 레시피를 기반으로 진행된 공동구매 목록과 각 공동구매의 개별 평점을 표시하여, 레시피와 관련된 사용자 경험 정보를 제공한다.
- **UI/UX**: 작성된 후기 및 평점 정보를 사용자가 명확히 인지하고, 일관된 경험 하에 탐색할 수 있도록 관련 페이지에 표시하고 통합한다.

## 2. 설계 결정

### 2.1. 데이터 모델: `Review` 엔티티 활용

- 기존에 설계된 `Review` 엔티티를 그대로 활용합니다. `Review`는 `GroupBuy` 및 `User`와 직접적인 관계를 맺고, 별점(`rating`) 필드를 가지므로 후기 기능에 최적화되어 있습니다.

### 2.2. 후기 표시 전략: 전용 페이지 및 커뮤니티 탭 통합

- 기존 `PostCategory` Enum에 `REVIEW`가 포함되어 있으나, `Post` 모델은 별점, 공구 정보 등 후기에 필요한 핵심 필드가 없어 사용하지 않는 것이 바람직합니다.
- 따라서, **`PostCategory`에서 `REVIEW`를 제거**하여 모델의 역할을 명확히 분리합니다.
- 대신, 커뮤니티 목록 페이지의 기존 검색 UI를 **탭(Tab) UI로 개편**하여, 분리된 `Review` 데이터를 자연스럽게 통합합니다.

#### 커뮤니티 탭 구성

| 탭 이름 | 클릭 시 이동할 주소 | 내용 |
| :--- | :--- | :--- |
| **전체** | `/community-posts/list` | 모든 일반 게시글(`TIPS`, `FREE`)을 보여줍니다. |
| **팁** | `/community-posts/list?category=TIPS` | '팁' 카테고리의 게시글만 보여줍니다. |
| **자유** | `/community-posts/list?category=FREE` | '자유' 카테고리의 게시글만 보여줍니다. |
| **후기** | `/reviews` | **별도의 후기 전용 목록 페이지**로 이동하여, `Review` 데이터를 보여줍니다. |

### 2.3. 레시피 관련 공동구매 후기 표시 전략

- **레시피 평점 자동 계산 폐기**: 공동구매 후기 평점은 레시피 자체의 품질과 무관한 요소들을 포함할 수 있으므로, 이를 레시피의 직접적인 평점으로 자동 계산하지 않습니다.
- **표시 전략**: 레시피 상세 페이지에서는 해당 레시피를 기반으로 진행된 공동구매 목록을 보여주고, 각 공동구매의 **개별 평균 평점과 후기 개수**를 함께 표시합니다.

## 3. 세부 구현 계획

### 3.1. 백엔드 (Backend)

#### 3.1.1. Enum 수정

- `com.recipemate.global.common.PostCategory`: `REVIEW` 멤버를 제거합니다.

#### 3.1.2. DTO 수정

- `com.recipemate.domain.groupbuy.dto.GroupBuyResponse`: `averageRating`, `reviewCount` 필드 추가.
- `com.recipemate.domain.recipe.dto.RecipeDetailResponse`: `relatedGroupBuys` (`List<GroupBuyResponse>`) 필드를 추가합니다.
- `com.recipemate.domain.groupbuy.dto.ParticipationDto` (신규 또는 수정): `Participation` 엔티티 조회 시 후기 작성 여부(`hasReview`)를 포함하도록 DTO를 설계합니다.

#### 3.1.3. Service 로직 보강

- `GroupBuyService`: `getGroupBuyDetail` 시 `GroupBuyResponse`에 해당 공구의 평균 별점과 후기 개수를 계산하여 담습니다.
- `RecipeService`: `getRecipeDetailByApiId` 시 `RecipeDetailResponse`에 해당 레시피를 기반으로 한 공동구매 목록(`relatedGroupBuys`)을 조회하고, 각 공동구매의 평균 별점과 후기 개수를 포함하여 DTO에 담습니다.
- `ReviewService`: 특정 공구의 후기 목록(`getReviewsByGroupBuyId`), 전체 후기 목록(`getAllReviews`)을 페이징하여 조회하는 메소드를 추가합니다.

#### 3.1.4. Controller 신규/수정

- `ReviewController`: 후기 CRUD 및 **전체 후기 목록 페이지(`GET /reviews`)**를 렌더링하는 핸들러를 포함합니다.
- `GroupBuyController`: `detail.html`에서 htmx로 호출할 후기 목록(HTML 조각)을 제공하는 `GET /group-purchases/{groupBuyId}/reviews` 엔드포인트를 추가합니다.

### 3.2. 프론트엔드 (Frontend)

#### 3.2.1. `community-posts/list.html` (커뮤니티 목록)

- 기존 카테고리 검색 `form`을 상단 **탭 UI**로 변경합니다. 각 탭은 위 `2.2. 커뮤니티 탭 구성` 표에 명시된 링크를 가집니다.

#### 3.2.2. `user/participations.html` (참여한 공구)

- `participation.groupBuyStatus`가 `CLOSED`이고, `participation.hasReview`가 `false`일 때 '후기 작성' 버튼을 표시합니다.

#### 3.2.3. `group-purchases/detail.html` (공동구매 상세)

- 기존 댓글 표시 영역을 **탭(Tab) UI로 변경**합니다.
    - **[Q&A] 탭 (기본)**: 기존과 동일하게 htmx로 댓글 목록(`fragments/comments.html`)을 불러옵니다.
    - **[후기] 탭**: 클릭 시 htmx로 해당 공구의 후기 목록(`fragments/reviews.html`)을 불러옵니다. 탭 이름 옆에 후기 개수를 표시합니다. (예: `후기 (5)`)
- `groupBuy.reviewCount > 0`일 때만 평균 별점과 후기 개수를 페이지 상단에 표시합니다.

#### 3.2.4. `recipes/detail.html` (레시피 상세)

- **기존 구조 활용**: 해당 템플릿에는 이미 `recipe.relatedGroupBuys` 목록을 반복하여 표시하고, 관련 공구가 없을 때의 안내 문구를 처리하는 로직이 구현되어 있습니다.
- **수정 사항**: 기존 구조를 바탕으로, 각 공동구매 카드 내에 `averageRating`과 `reviewCount`를 추가로 표시하는 코드만 삽입합니다.
    ```html
    <!-- 예시: 각 groupBuy 카드 내부에 추가될 코드 -->
    <div th:if="${groupBuy.reviewCount > 0}" class="mt-2">
        <i class="bi bi-star-fill text-warning"></i>
        <span class="fw-bold ms-1" th:text="${#numbers.formatDecimal(groupBuy.averageRating, 1, 1, 'POINT')}">0.0</span>
        <span class="text-muted ms-1" th:text="'(' + ${groupBuy.reviewCount} + '개 후기)'"></span>
    </div>
    <div th:if="${groupBuy.reviewCount == 0}" class="mt-2 text-muted small">
        <i class="bi bi-chat-dots"></i> 아직 후기가 없습니다.
    </div>
    ```

#### 3.2.5. 신규 템플릿

- `reviews/list.html`: 전체 후기 목록 페이지. `community-posts/list.html`와 유사한 UI로 구현합니다.
- `reviews/form.html`: 후기 작성/수정 폼 페이지.
- `fragments/reviews.html`: 공동구매 상세 페이지의 '후기' 탭에 삽입될 HTML 조각.

## 4. 단계별 실행 계획 (Action Plan)

1.  **1단계: 기존 코드 리팩토링 및 UI 구조 변경**
    - `PostCategory` Enum에서 `REVIEW`를 제거합니다.
    - `community-posts/list.html`의 검색 폼을 탭 UI로 변경합니다.
2.  **2단계: DTO 및 서비스 로직 수정 (백엔드)**
    - `GroupBuyResponse`, `RecipeDetailResponse` DTO를 계획에 맞게 수정합니다.
    - `GroupBuyService`, `RecipeService`에서 각 DTO의 신규 필드를 채우도록 로직을 수정합니다.
    - `Participation` 조회 DTO에 `hasReview` 필드를 추가하고 관련 로직을 구현합니다.
3.  **3단계: 후기 조회 기능 구현 (프론트엔드)**
    - `group-purchases/detail.html`의 댓글/후기 영역을 **탭 UI로 리팩토링**합니다.
    - `recipes/detail.html`에 `relatedGroupBuys` 목록과 각 공구의 평점을 표시합니다.
    - 후기 목록 HTML 조각(`fragments/reviews.html`)과 이를 제공할 컨트롤러 엔드포인트를 구현합니다.
4.  **4단계: 전체 후기 목록 페이지 구현**
    - `ReviewService`에 전체 후기 목록 조회 로직을 추가합니다.
    - `ReviewController`에 `GET /reviews` 요청을 처리할 핸들러를 추가합니다.
    - `reviews/list.html` 템플릿을 작성합니다.
5.  **5단계: 후기 작성 흐름 구현**
    - `user/participations.html`에 조건부 '후기 작성' 버튼을 추가합니다.
    - 후기 작성/수정 폼(`reviews/form.html`)과 이를 렌더링/처리하는 컨트롤러 로직을 구현합니다.
6.  **6단계: 테스트 및 최종 검토**
    - 후기 작성 조건, 평점 계산 로직 등에 대한 테스트를 수행합니다.
    - 전체 사용자 흐름을 검토하고 UI/UX를 최종적으로 다듬습니다.
