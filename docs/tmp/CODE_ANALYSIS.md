# 코드 분석 및 리팩터링 제안 보고서

## 3. 구조적 문제 및 리팩터링 제안

### 3.1. 도메인 모델과 비즈니스 로직 (Anemic Domain Model)
- **문제점**: `GroupBuy`, `User`, `Post` 등 핵심 엔티티들이 대부분 필드와 Getter만 가진 '빈약한 도메인 모델(Anemic Domain Model)'입니다. `increaseParticipant()`, `close()` 같은 일부 로직은 엔티티에 존재하지만, 생성(`createGeneral`), 수정(`update`) 등 핵심 비즈니스 로직 대부분이 **서비스 계층(`GroupBuyService`)에 위임**되어 있습니다. 이로 인해 엔티티는 단순 데이터 전달 객체(DTO)처럼 사용되고, 서비스 계층은 점점 비대해져 응집도가 낮아지고 유지보수가 어려워집니다.
- **제안**:
    1. **- [x] 엔티티에 비즈니스 로직 위임**: `GroupBuyService`의 참여/취소 로직을 `GroupBuy` 엔티티로 옮겨 풍부한 도메인 모델을 구축했습니다.
        ```java
        // GroupBuy.java
        public Participation addParticipant(User user, ...) { ... }
        public void cancelParticipation(Participation participation) { ... }
        ```
    2. **- [ ] 정적 팩토리 메서드 활용**: 현재 `GroupBuy.createGeneral()`, `createRecipeBased()` 같은 정적 팩토리 메서드는 좋은 시도입니다. 이를 더 적극적으로 활용하여 서비스의 생성 로직을 엔티티로 옮겨 응집도를 높일 수 있습니다.

### 3.2. 서비스 계층의 과도한 책임 (Fat Service)
- **문제점**: `GroupBuyService`와 `ParticipationService`가 너무 많은 책임을 가지고 있습니다. 예를 들어, `GroupBuyService`는 공구 생성 로직 외에도 이미지 업로드, 뱃지 수여, 포인트 적립까지 직접 처리합니다. 이는 단일 책임 원칙(SRP)에 위배됩니다.
- **제안**:
    1. **- [x] 도메인 이벤트 발행**: `Spring ApplicationEventPublisher`를 사용하여 도메인 이벤트를 발행하는 구조로 리팩터링했습니다.
        - `GroupBuyService`, `ReviewService` 등은 자신의 핵심 로직을 처리한 후 `GroupBuyCreatedEvent`, `ReviewCreatedEvent` 같은 이벤트를 발행합니다.
        - `BadgeService`, `PointService`, `NotificationService`는 각각 이 이벤트를 구독(`@EventListener`)하여 자신의 책임에 맞는 로직을 처리하여 서비스 간 **결합도(Coupling)를 크게 낮췄습니다.**
    2. **- [x] 유틸리티 및 헬퍼 분리**: `ImageUploadUtil`처럼, 도메인과 직접 관련 없는 부가 기능은 이미 별도의 유틸리티 클래스로 잘 분리되어 있으며, 이 원칙을 계속 유지합니다.

### 3.3. 컨트롤러의 역할
- **문제점**: `CommentController`에서 `Validator`를 직접 주입받아 수동으로 DTO를 검증하고 있습니다. 또한 `UserDetails`에서 사용자를 조회하는 로직이 반복적으로 나타납니다.
- **제안**:
    1. **- [ ] `@Valid` 어노테이션 활용**: 컨트롤러 메서드 파라미터에 `@Valid`를 사용하여 유효성 검증을 프레임워크에 위임하고, `BindingResult`로 결과를 처리하는 것이 표준적인 방법입니다. `CustomAuthenticationFailureHandler`처럼 `BindingResult`를 처리하는 로직을 추가하면 코드가 간결해집니다.
    2. **- [ ] Argument Resolver 활용**: `@AuthenticationPrincipal`에 `UserDetails` 대신 커스텀한 `User` 객체를 직접 주입받도록 `HandlerMethodArgumentResolver`를 구현하면, 컨트롤러에서 `userRepository.findByEmail(...)` 호출 코드를 제거하여 중복을 줄일 수 있습니다.

### 3.4. 성능 문제 (N+1)
- **현황**: `application.yml`에 `default_batch_fetch_size: 100`이 이미 설정되어 있는 것을 확인했습니다. 이는 지연 로딩 시 N+1 문제를 완화하는 매우 효과적인 설정입니다.
- **문제점**: 하지만 이 설정만으로 모든 N+1 문제가 해결되지는 않습니다. `UserService`의 `getMyGroupBuys`와 같이, `Page` 객체를 조회한 후 연관된 이미지 목록을 가져오기 위해 `groupBuyImageRepository.findByGroupBuyIdIn...`을 추가로 호출하는 패턴은 `batch_fetch_size`의 직접적인 적용 대상이 아니며, 여전히 별도의 쿼리를 발생시킵니다.
- **제안**:
    1. **- [ ] `@EntityGraph` 활용**: Repository 메서드에 `@EntityGraph`를 사용하여 조회 시점에 함께 가져올 연관 엔티티를 명시적으로 지정할 수 있습니다. 이는 `batch_fetch_size`보다 더 명시적이고 확실한 해결책이 될 수 있습니다.

### 3.5. 횡단 관심사 분리 (AOP 및 공통 모듈)
- **문제점**: `BACKLOG.md`에도 언급되었듯이, 여러 서비스에 걸쳐 권한 검사 로직(`comment.getAuthor().getId().equals(userId)`)이 중복되고 있습니다. 이는 전형적인 **횡단 관심사(Cross-Cutting Concern)**이며, 코드를 파편화시키고 유지보수를 어렵게 만듭니다.
- **제안**:
    1. **- [ ] `PermissionEvaluator` 도입**: Spring Security가 제공하는 `PermissionEvaluator`를 구현하고 `@PreAuthorize("hasPermission(#commentId, 'comment', 'delete')")`와 같은 어노테이션을 사용하여 권한 검사를 선언적으로 처리하는 것을 강력히 권장합니다. 이를 통해 서비스 로직에서 권한 검사 코드를 완전히 분리할 수 있습니다.
    2. **- [ ] `ValidationUtil` 도입**: 여러 계층에서 사용될 수 있는 복잡한 비즈니스 유효성 검증 로직(예: 마감일 정책, 참여 조건 등)은 별도의 `ValidationUtil` 또는 `Policy` 클래스로 분리하여 중앙에서 관리하는 것이 좋습니다. 이는 로직의 재사용성을 높이고 일관성을 보장합니다.
    3. **- [ ] AOP 활용**: `@CheckPermission`과 같은 커스텀 어노테이션과 AOP를 결합하여, 메서드 실행 전후에 공통 로직(권한 검사, 로깅 등)을 주입하는 것도 좋은 방법입니다.

### 3.6. Post(게시글) 도메인 관련
- **문제점 1: 게시글 수정 시 카테고리 미반영 (`BACKLOG.md`)**
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
- **문제점 2: 조회수 중복 증가 (`BACKLOG.md`)**
    - **원인**: `PostService`의 `getPostDetail` 메서드는 별도의 확인 로직 없이 호출될 때마다 `post.increaseViewCount()`를 실행합니다. 이로 인해 단순 새로고침만으로 조회수가 계속 증가합니다.
    - **제안**: **- [ ] 세션(Session)이나 쿠키(Cookie)를 사용하여 사용자가 특정 시간 내에 동일한 게시글을 다시 조회하는 것을 방지해야 합니다.** 예를 들어, 조회한 게시글 ID 목록을 세션에 저장하고, `getPostDetail` 호출 시 세션에 해당 ID가 있는지 확인하여 없는 경우에만 조회수를 증가시키고 세션에 ID를 추가하는 방식입니다.
- **문제점 3: 서비스 로직 내 조회수 증가 처리**
    - **원인**: `getPostDetail`은 이름과 달리 데이터를 조회(`get`)하면서 상태를 변경(`increaseViewCount`)하는 두 가지 책임을 가지고 있습니다. 이는 명령-조회 분리(CQS) 원칙에 어긋납니다.
    - **제안**: **- [ ] 조회수 증가는 별도의 `increaseViewCount(Long postId)` 메서드로 분리하고, 컨트롤러에서 `getPostDetail` 호출과 `increaseViewCount` 호출을 분리하는 것이 더 명확한 설계입니다.** 중복 방지 로직은 `increaseViewCount` 메서드 내에서 처리합니다.

### 3.7. Global 패키지 (설정, 유틸리티) 관련
- **`ErrorCode.java`**: 도메인별로 에러 코드가 잘 그룹화되어 있고 HTTP 상태 코드와 메시지가 명확하게 정의되어 있어 전반적으로 좋은 구조입니다. 다만, `UNAUTHORIZED_COMMENT_ACCESS`, `UNAUTHORIZED_COMMENT_UPDATE` 처럼 동일 도메인 내 권한 에러가 세분화되어 있는데, 향후 `PermissionEvaluator` 도입 시 `UNAUTHORIZED_ACTION`과 같이 더 일반적인 코드로 통합하여 관리 복잡성을 줄이는 것을 고려해볼 수 있습니다.

- **`ImageUploadUtil.java` & `ImageOptimizationService.java`**
    - **문제점**: 이미지 업로드 시 `ImageOptimizationService`를 통해 동기적으로 이미지를 최적화하고 있습니다. 이미지 처리(특히 리사이징, 압축)는 CPU를 많이 사용하는 작업이므로, 요청량이 많아지거나 큰 이미지를 처리할 때 요청 처리 스레드를 오래 점유하여 전체 애플리케이션의 응답성을 저하시킬 수 있습니다.
    - **제안**: **- [ ] 이미지 업로드 및 최적화 과정을 비동기(Asynchronous)로 처리하는 것을 권장합니다.** `@Async` 어노테이션과 별도의 스레드 풀을 사용하여 이미지 처리를 백그라운드에서 수행하면, 사용자는 더 빠른 응답을 받을 수 있고 시스템의 처리량이 향상됩니다.

- **`CacheConfig.java`**
    - **문제점**: `PostService`의 게시글 목록 캐시 키가 `category`, `keyword`, `pageNumber`, `pageSize`를 모두 조합하여 생성됩니다. 이는 잠재적으로 수많은 캐시 키를 생성하여 메모리를 비효율적으로 사용하고, 캐시 적중률(Hit Ratio)을 떨어뜨릴 수 있습니다.
    - **제안**: **- [ ] 검색 결과처럼 파라미터 조합이 다양한 경우, 캐싱의 효율이 떨어지므로 전략을 재검토해야 합니다.** 예를 들어, 자주 바뀌지 않는 첫 페이지만 캐싱하거나, 캐시 키를 단순화(예: 키워드의 해시값 사용)하는 방안을 고려해야 합니다. 또한 `@CacheEvict(allEntries = true)`는 관련 없는 캐시까지 모두 삭제하므로, 특정 캐시 항목만 선별적으로 제거하는 로직으로 개선하는 것이 좋습니다.

---

## 4. 요약 및 권장 다음 단계

1.  **- [ ] [최우선] 레시피 데이터 DB 캐싱 전략 구현**: `BACKLOG.md`의 최우선 과제입니다. `Recipe` 엔티티를 만들고, 외부 API 첫 호출 시 DB에 저장한 후 주기적으로 동기화하는 배치(Batch) 작업을 구현해야 합니다. 이는 서비스 안정성의 핵심입니다.
2.  **- [x] `GroupBuy` 도메인 리팩터링**: 서비스 계층의 비즈니스 로직을 `GroupBuy` 엔티티로 옮겨 풍부한 도메인 모델을 구축하고, 이벤트 기반 아키텍처를 도입하여 서비스 간 결합도를 낮추는 작업을 진행합니다.
3.  **- [ ] 횡단 관심사 분리**: `PermissionEvaluator`를 도입하여 파편화된 권한 검사 로직을 중앙화하고, 서비스 코드의 가독성과 유지보수성을 높입니다.
4.  **- [ ] 성능 최적화**: `default_batch_fetch_size` 설정에 더해, `@EntityGraph` 또는 QueryDSL DTO 조회를 적용하여 추가 쿼리 발생을 최소화합니다.
5.  **- [ ] 컨트롤러 리팩터링**: `@Valid`와 커스텀 `ArgumentResolver`를 도입하여 중복 코드를 제거하고 가독성을 높입니다.
