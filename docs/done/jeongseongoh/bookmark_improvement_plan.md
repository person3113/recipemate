# 찜 목록 개선 및 구현 계획

## 1. 현황 분석

- 현재 '찜' 기능은 'Wishlist'라는 이름으로 구현되어 있으며, **공동구매(GroupBuy)에 대해서만 동작**합니다.
- 레시피(Recipe)에 대한 찜 기능은 존재하지 않습니다.
- `src/main/java/com/recipemate/domain/wishlist/entity/Wishlist.java` 엔티티는 `User`와 `GroupBuy`만 연관관계로 맺고 있습니다.
- `src/main/resources/templates/recipes/detail.html`의 레시피 상세 페이지에 있는 '북마크 추가' 버튼은 현재 아무런 기능이 구현되어 있지 않은 상태입니다.
- '공유하기' 버튼 또한 기능이 구현되어 있지 않습니다.

## 2. 제안 사항

### 2.1. 레시피 찜 기능 추가

- **접근 방식:** 기존 `Wishlist`를 수정하기보다는, 레시피 찜 기능을 위한 별도의 `RecipeWishlist` 엔티티, 리포지토리, 서비스, 컨트롤러를 새로 생성하는 것을 제안합니다. 이는 공동구매 찜 기능과의 복잡성을 분리하고 향후 유지보수성을 높이는 데 유리합니다.

- **구현 내용:**
    1.  **`RecipeWishlist` 엔티티 생성:** `User`와 `Recipe`를 다대일 관계로 매핑합니다.
    2.  **`RecipeWishlistRepository` 생성:** `JpaRepository`를 상속받아 기본적인 CRUD 기능을 구현합니다.
    3.  **`RecipeWishlistService` 생성:** 레시피 찜 추가/삭제, 내 찜 목록 조회 등의 비즈니스 로직을 구현합니다.
    4.  **`RecipeWishlistController` 생성:** 레시피 찜 관련 API 엔드포인트를 구현합니다. (e.g., `POST /recipes/{recipeId}/bookmarks`, `POST /recipes/{recipeId}/bookmarks/cancel`)
    5.  **프론트엔드 연동:** `recipes/detail.html`의 '북마크 추가' 버튼에 위에서 구현한 API를 연동하여, 클릭 시 찜 추가/삭제가 비동기적으로 동작하도록 수정합니다.

### 2.2. 공유하기 기능 구현

- **접근 방식:** JavaScript의 Web Share API (`navigator.share`)를 사용하여 사용자가 모바일 기기에서 자연스러운 공유 경험을 할 수 있도록 구현합니다. Web Share API를 지원하지 않는 브라우저(e.g., 데스크톱)를 위해 현재 페이지의 URL을 클립보드에 복사하는 기능을 대체(fallback)로 제공합니다.

- **구현 내용:**
    1.  `recipes/detail.html`의 '공유하기' 버튼에 클릭 이벤트를 추가합니다.
    2.  이벤트 핸들러 내에서 `navigator.share`가 사용 가능한지 확인합니다.
    3.  **사용 가능 시:** `navigator.share`를 호출하여 제목, 텍스트, URL을 전달합니다.
    4.  **사용 불가 시:** `navigator.clipboard.writeText(window.location.href)`를 사용하여 현재 URL을 클립보드에 복사하고, 사용자에게 "URL이 복사되었습니다."와 같은 알림을 띄워줍니다.

## 3. 단계별 구현 계획

1.  **백엔드 구현:**
    - `com.recipemate.domain.recipewishlist` 패키지 생성
    - `RecipeWishlist` 엔티티, 리포지토리, 서비스, 컨트롤러 클래스 작성
    - `User` 엔티티에 `recipeWishlists` 필드 추가

2.  **프론트엔드 구현:**
    - `recipes/detail.html`의 '북마크 추가' 버튼에 `onclick` 이벤트 핸들러 추가 및 API 연동
    - '공유하기' 버튼에 `onclick` 이벤트 핸들러 및 Web Share API / 클립보드 복사 로직 추가

3.  **테스트 및 검증:**
    - 레시피 찜 추가/삭제 기능이 정상적으로 동작하는지 확인
    - 내 찜 목록 페이지(`GET /users/me/bookmarks`) 컨트롤러 로직을 수정하여 공동구매 찜과 레시피 찜 목록을 모두 조회하도록 변경하고, 템플릿에 두 목록을 함께 렌더링하도록 수정
