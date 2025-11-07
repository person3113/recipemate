# RecipeMate Backlog

> 미구현 기능, 기술 부채, 개선 사항 기록 및 우선순위별 관리

📝 원칙
- **TDD**: 백로그 항목도 테스트 먼저 작성
- **YAGNI**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새 느껴질 때 백로그 항목 처리

---

## 🔴 HIGH Priority

- 레시피도 themealdb, 식품안전나라, 사용자 추가 이렇게 세 분류로 하고. 각각 레시피 목록 볼 수 있고. 각각 카테고리나 키워드나 재료나 필터링이나 그런 기능 추가해야 할 듯. 그럴려면 db에 api 정보 넣고 해야할 거 같은데 맞나?
    - 식품안전나라 API, MealDB API를 매번 실시간 호출
    - prod 환경에서만 Redis 캐싱 적용
- **문제점**:
    - API 호출 제한(rate limit) 리스크
    - 응답 속도가 외부 API에 의존적
    - 검색 기능 제약 (외부 API 검색 기능에만 의존)
- **개선 방안**:
    1. **DB 캐싱 전략**: 첫 호출 시 레시피 데이터를 자체 DB에 저장
    2. **배치 업데이트**: 주기적으로 인기 레시피/최신 데이터 동기화
    3. **하이브리드 접근**: 기본 검색은 자체 DB, 상세 정보는 API 재호출
    4. **TTL 관리**: 데이터 신선도 유지 (예: 7일마다 갱신)
- **장점**:
    - 응답 속도 개선
    - API 호출 횟수 감소 (비용/제한 대응)
    - 자체 검색 기능 고도화 가능

---

## 🟡 MEDIUM Priority

- [] 게시글 좋아요/댓글 기능
  1. Post 엔티티에 좋아요/댓글 수 필드 추가 또는 동적 계산
  2. 댓글(Comment) 엔티티와 관계 설정
  3. 좋아요(Like) 엔티티 생성 및 중복 방지 로직
  4. PostResponse DTO에 commentCount, likeCount 필드 추가
  5. UI 복원 (community-posts/list.html, detail.html, search/results.html)

-[] 게시글 수정 되는데, 기존 제목과 바디 내용은 그대로 유지되는데 카테고리는 안되네. 점검 좀.
    - **문제점 1: 게시글 수정 시 카테고리 미반영**
        - **원인**: `PostController`의 `editPage` 메서드에서 수정 폼으로 전달하는 `UpdatePostRequest` 객체에 `title`과 `content`만 설정하고, 기존 `category` 값을 설정해주지 않고 있습니다. 이로 인해 뷰 템플릿에서 카테고리 선택이 초기화되어, 사용자가 다시 선택하지 않으면 `@NotNull` 제약조건에 걸려 유효성 검증에 실패하게 됩니다.
  - **제안**:
    - **- [ ] `editPage` 메서드에서 `UpdatePostRequest`를 채울 때, `post.getCategory()` 값을 함께 설정해주어야 합니다.**
      ```java
      // PostController.java - editPage
      UpdatePostRequest formData = new UpdatePostRequest();
      formData.setTitle(post.getTitle());
      formData.setContent(post.getContent());
      formData.setCategory(post.getCategory()); // 이 코드 추가
      model.addAttribute("formData", formData);
      ```

-[] 게시글 조회수. 새로고침할 때마다 계속 증가하는데 세션 기반으로 중복 카운트 방지 기능 추가 필요.
    - **문제점 2: 조회수 중복 증가**
        - **원인**: `PostService`의 `getPostDetail` 메서드는 별도의 확인 로직 없이 호출될 때마다 `post.increaseViewCount()`를 실행합니다. 이로 인해 단순 새로고침만으로 조회수가 계속 증가합니다.
        - **제안**: **- [ ] 세션(Session)이나 쿠키(Cookie)를 사용하여 사용자가 특정 시간 내에 동일한 게시글을 다시 조회하는 것을 방지해야 합니다.** 예를 들어, 조회한 게시글 ID 목록을 세션에 저장하고, `getPostDetail` 호출 시 세션에 해당 ID가 있는지 확인하여 없는 경우에만 조회수를 증가시키고 세션에 ID를 추가하는 방식입니다.
    - **문제점 3: 서비스 로직 내 조회수 증가 처리**
        - **원인**: `getPostDetail`은 이름과 달리 데이터를 조회(`get`)하면서 상태를 변경(`increaseViewCount`)하는 두 가지 책임을 가지고 있습니다. 이는 명령-조회 분리(CQS) 원칙에 어긋납니다.
        - **제안**: **- [ ] 조회수 증가는 별도의 `increaseViewCount(Long postId)` 메서드로 분리하고, 컨트롤러에서 `getPostDetail` 호출과 `increaseViewCount` 호출을 분리하는 것이 더 명확한 설계입니다.** 중복 방지 로직은 `increaseViewCount` 메서드 내에서 처리합니다.

-[] 공동구매 상세 페이지/커뮤니티포스트 상세 페이지 속 댓글 등록은 되는데. 수정/삭제/대댓글 달 때 오류. 로그 확인하고 해결 필요

-[] **레시피 기반 공구 생성**: 기획의 핵심인 '원클릭 공구 생성' 기능이 일부 구현되었으나(`GroupBuyController`의 `/new` 엔드포인트), 재료 목록을 JSON 문자열로 수동 파싱하는 등(`createRecipeBasedGroupBuy` 메서드) 다소 복잡하고 불안정한 방식에 의존하고 있습니다. 프론트엔드와 상호작용이 매끄럽지 않을 수 있습니다.

- [] 검증 로직이나 권한 체크는 따로 permissionChecker나 ValidationUtil 같은 걸로 모아서 재사용하는 게 나을 거 같은데 어떻게 생각해?
    - 횡단 관심사 분리 (AOP 및 공통 모듈)
    - **문제점**: 여러 서비스에 걸쳐 권한 검사 로직(`comment.getAuthor().getId().equals(userId)`)이 중복되고 있습니다. 이는 전형적인 **횡단 관심사(Cross-Cutting Concern)**이며, 코드를 파편화시키고 유지보수를 어렵게 만듭니다.
    - **제안**:
        1. **- [ ] `PermissionEvaluator` 도입**: Spring Security가 제공하는 `PermissionEvaluator`를 구현하고 `@PreAuthorize("hasPermission(#commentId, 'comment', 'delete')")`와 같은 어노테이션을 사용하여 권한 검사를 선언적으로 처리하는 것을 강력히 권장합니다. 이를 통해 서비스 로직에서 권한 검사 코드를 완전히 분리할 수 있습니다.
        2. **- [ ] `ValidationUtil` 도입**: 여러 계층에서 사용될 수 있는 복잡한 비즈니스 유효성 검증 로직(예: 마감일 정책, 참여 조건 등)은 별도의 `ValidationUtil` 또는 `Policy` 클래스로 분리하여 중앙에서 관리하는 것이 좋습니다. 이는 로직의 재사용성을 높이고 일관성을 보장합니다.
        3. **- [ ] AOP 활용**: `@CheckPermission`과 같은 커스텀 어노테이션과 AOP를 결합하여, 메서드 실행 전후에 공통 로직(권한 검사, 로깅 등)을 주입하는 것도 좋은 방법입니다.
    - [] **`ErrorCode.java`**: 도메인별로 에러 코드가 잘 그룹화되어 있고 HTTP 상태 코드와 메시지가 명확하게 정의되어 있어 전반적으로 좋은 구조입니다. 다만, `UNAUTHORIZED_COMMENT_ACCESS`, `UNAUTHORIZED_COMMENT_UPDATE` 처럼 동일 도메인 내 권한 에러가 세분화되어 있는데, 향후 `PermissionEvaluator` 도입 시 `UNAUTHORIZED_ACTION`과 같이 더 일반적인 코드로 통합하여 관리 복잡성을 줄이는 것을 고려해볼 수 있습니다.

### 1. htmx HTML Fragment 엔드포인트 구현
- **개선**:
    1. 컨트롤러에 htmx HTML Fragment 엔드포인트 추가 (TODO 주석으로 표시된 부분 구현)
    2. Thymeleaf Fragment 템플릿 생성 (list-items, detail-content, form-fields 등)
    3. 기존 템플릿의 htmx 호출 경로를 HTML Fragment 엔드포인트로 변경
  - **장점**:
      - htmx 철학 완전 준수 (서버가 HTML 반환)
      - JSON 파싱 오버헤드 제거
      - 서버 사이드 렌더링 완전 활용
  - **참고**: 기존 기능에 영향 없이 점진적 추가 가능
  - **htmx 부분 페이지 갱신**: `URL_DESIGN.md`와 컨트롤러 곳곳에 `htmx`를 위한 `/fragments` 엔드포인트가 설계되어 있으나, 실제 구현은 대부분 일반적인 `redirect` 방식으로 처리되고 있습니다. 이로 인해 사용자 경험(UX)이 저하될 수 있습니다.
- Task 4-3-3: htmx/Alpine.js 적용
  - [ ] htmx로 부분 페이지 갱신
      - 댓글 작성/삭제
      - 좋아요/찜하기
      - 참여하기/취소
  - [ ] Alpine.js로 클라이언트 상호작용
      - 드롭다운 메뉴
      - 모달 팝업
      - 이미지 갤러리

### 3.3. 컨트롤러의 역할
- **문제점**: `CommentController`에서 `Validator`를 직접 주입받아 수동으로 DTO를 검증하고 있습니다. 또한 `UserDetails`에서 사용자를 조회하는 로직이 반복적으로 나타납니다.
- **제안**:
    1. **- [ ] `@Valid` 어노테이션 활용**: 컨트롤러 메서드 파라미터에 `@Valid`를 사용하여 유효성 검증을 프레임워크에 위임하고, `BindingResult`로 결과를 처리하는 것이 표준적인 방법입니다. `CustomAuthenticationFailureHandler`처럼 `BindingResult`를 처리하는 로직을 추가하면 코드가 간결해집니다.
    2. **- [ ] Argument Resolver 활용**: `@AuthenticationPrincipal`에 `UserDetails` 대신 커스텀한 `User` 객체를 직접 주입받도록 `HandlerMethodArgumentResolver`를 구현하면, 컨트롤러에서 `userRepository.findByEmail(...)` 호출 코드를 제거하여 중복을 줄일 수 있습니다.

#### [ ] Task 4-5-5: 지도 API 연동 (만남 장소 표시)
- [ ] 테스트 작성
    - 주소 → 위도/경도 변환 (Geocoding)
    - 지도 표시 컴포넌트 렌더링
- [ ] 카카오/네이버 지도 API 설정
    - API Key 발급
    - JavaScript SDK 추가
- [ ] GroupBuy 엔티티 확장
    - latitude, longitude 필드 추가 (Nullable)
- [ ] MapService 구현
    - `geocodeAddress(String address)` (주소 → 좌표)
    - `reverseGeocode(Double lat, Double lng)` (좌표 → 주소)
- [ ] 프론트엔드 통합
    - 공구 작성 시 지도에서 장소 선택
    - 공구 상세 페이지에 지도 표시

---

## 🟢 LOW Priority

- 3.4. 성능 문제 (N+1)
  - **현황**: `application.yml`에 `default_batch_fetch_size: 100`이 이미 설정되어 있는 것을 확인했습니다. 이는 지연 로딩 시 N+1 문제를 완화하는 매우 효과적인 설정입니다.
  - **문제점**: 하지만 이 설정만으로 모든 N+1 문제가 해결되지는 않습니다. `UserService`의 `getMyGroupBuys`와 같이, `Page` 객체를 조회한 후 연관된 이미지 목록을 가져오기 위해 `groupBuyImageRepository.findByGroupBuyIdIn...`을 추가로 호출하는 패턴은 `batch_fetch_size`의 직접적인 적용 대상이 아니며, 여전히 별도의 쿼리를 발생시킵니다.
  - **제안**:
      1. **- [ ] `@EntityGraph` 활용**: Repository 메서드에 `@EntityGraph`를 사용하여 조회 시점에 함께 가져올 연관 엔티티를 명시적으로 지정할 수 있습니다. 이는 `batch_fetch_size`보다 더 명시적이고 확실한 해결책이 될 수 있습니다.

- **`ImageUploadUtil.java` & `ImageOptimizationService.java`**
    - **문제점**: 이미지 업로드 시 `ImageOptimizationService`를 통해 동기적으로 이미지를 최적화하고 있습니다. 이미지 처리(특히 리사이징, 압축)는 CPU를 많이 사용하는 작업이므로, 요청량이 많아지거나 큰 이미지를 처리할 때 요청 처리 스레드를 오래 점유하여 전체 애플리케이션의 응답성을 저하시킬 수 있습니다.
    - **제안**: 
        - [ ] 이미지 업로드 및 최적화 과정을 비동기(Asynchronous)로 처리하는 것을 권장합니다. `@Async` 어노테이션과 별도의 스레드 풀을 사용하여 이미지 처리를 백그라운드에서 수행하면, 사용자는 더 빠른 응답을 받을 수 있고 시스템의 처리량이 향상됩니다.
        - [ ] 원본 + 썸네일 2종 저장 (향후 개선)
        - [ ] 실제 WebP 형식 변환 (webp-imageio 라이브러리 추가)
        - [ ] CDN 연동 고려 (향후 개선)

- **`CacheConfig.java`**
    - **문제점**: `PostService`의 게시글 목록 캐시 키가 `category`, `keyword`, `pageNumber`, `pageSize`를 모두 조합하여 생성됩니다. 이는 잠재적으로 수많은 캐시 키를 생성하여 메모리를 비효율적으로 사용하고, 캐시 적중률(Hit Ratio)을 떨어뜨릴 수 있습니다.
    - **제안**: **- [ ] 검색 결과처럼 파라미터 조합이 다양한 경우, 캐싱의 효율이 떨어지므로 전략을 재검토해야 합니다.** 예를 들어, 자주 바뀌지 않는 첫 페이지만 캐싱하거나, 캐시 키를 단순화(예: 키워드의 해시값 사용)하는 방안을 고려해야 합니다. 또한 `@CacheEvict(allEntries = true)`는 관련 없는 캐시까지 모두 삭제하므로, 특정 캐시 항목만 선별적으로 제거하는 로직으로 개선하는 것이 좋습니다.

## ⚪ VERY LOW Priority

#### [ ] Task 4-5-6: 번역 API 연동 (선택)
- [ ] 테스트 작성
    - TheMealDB 영문 레시피명 → 한글 번역
    - 재료명 번역 캐싱
- [ ] Google Translation API 설정
    - API Key 발급 및 환경변수 설정
    - 번역 클라이언트 작성
- [ ] TranslationService 구현
    - `translateRecipeName(String englishName)`
    - `translateIngredient(String ingredient)`
    - 번역 결과 캐싱 (Redis 또는 로컬 캐시)
- [ ] RecipeService에 번역 로직 통합
    - 레시피 조회 시 자동 번역 옵션
    - 사용자 언어 설정에 따라 번역 제공

#### [ ] Task 4-5-7: 실시간 채팅 (선택 - 복잡도 높음)
- [ ] WebSocket 설정
    - Spring WebSocket 의존성 추가
    - WebSocketConfig 작성
    - STOMP 프로토콜 설정
- [ ] ChatMessage 엔티티 작성
    - 필드: id, sender(User FK), receiver(User FK), content, sentAt, isRead
    - 제약조건: 인덱스 (senderId, receiverId, sentAt)
- [ ] ChatService 구현
    - `sendMessage(Long senderId, Long receiverId, String content)`
    - `getMessageHistory(Long userId, Long otherUserId, Pageable pageable)`
    - `markAsRead(Long messageId)`
- [ ] WebSocket 컨트롤러 작성
    - `/topic/chat/{userId}` 구독 엔드포인트
    - `/app/chat.send` 메시지 전송 엔드포인트
- [ ] 프론트엔드 통합
    - SockJS + STOMP.js 클라이언트
    - 채팅방 UI 컴포넌트
    - 실시간 메시지 수신 처리
- [ ] 주의사항
    - 복잡도가 높아 시간 여유가 충분할 때만 구현
    - 대안: 기본 댓글 기능으로 대체 가능