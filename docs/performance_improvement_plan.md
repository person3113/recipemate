# 성능 개선 계획서

SQL 쿼리 로그 분석을 통해 발견된 주요 성능 병목 현상과 그에 대한 해결 방안을 정리합니다.

## 1. 리뷰 통계 N+1 문제 (심각도: 🔴 Critical)

가장 시급하게 해결해야 할 문제입니다. 공동구매 목록 조회 시, 각 항목의 리뷰 통계(평균 평점, 리뷰 수)를 얻기 위해 200개 이상의 추가 쿼리가 발생하고 있습니다.

### 원인 분석

현재 로직은 공동구매 목록을 먼저 조회한 후, `for` 루프를 돌며 각 공동구매 ID(`groupBuy.getId()`)를 사용해 `ReviewRepository`에 통계 쿼리를 개별적으로 전송합니다. 이 과정은 `GroupBuyConverter`의 `toDto` 메소드 내부에서 `reviewService.getReviewStats`를 호출하며 발생할 가능성이 높습니다.

```java
// 예상되는 문제 코드 (서비스 또는 컨버터)
for (GroupBuy groupBuy : groupBuys) {
    // 각 groupBuy에 대해 별도의 쿼리 발생
    Double avgRating = reviewRepository.getAverageRatingByGroupBuyId(groupBuy.getId()); // Query 1
    Long reviewCount = reviewRepository.countByGroupBuyId(groupBuy.getId());         // Query 2
    // ... DTO 생성 로직 ...
}
```

### 해결 방안: 통계 정보를 한 번의 쿼리로 조회

`ReviewRepository`에 `List<Long> groupBuyIds`를 인자로 받아, ID별 통계 정보를 `Map` 형태로 반환하는 메소드를 추가합니다. 이를 통해 단 한 번의 쿼리로 모든 공동구매의 리뷰 통계를 가져올 수 있습니다.

**1. `ReviewStatsDto` 생성**

리뷰 통계를 담을 DTO를 만듭니다.

```java
// ReviewStatsDto.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDto {
    private Long groupBuyId;
    private Double averageRating;
    private Long reviewCount;
}
```

**2. `ReviewRepository`에 사용자 정의 메소드 추가**

`GROUP BY`를 사용하여 ID 목록에 해당하는 모든 통계를 한 번에 계산합니다.

```java
// ReviewRepository.java
@Query("SELECT new com.recipemate.domain.review.dto.ReviewStatsDto(r.groupBuy.id, AVG(r.rating), COUNT(r.id)) " +
       "FROM Review r " +
       "WHERE r.groupBuy.id IN :groupBuyIds " +
       "GROUP BY r.groupBuy.id")
List<ReviewStatsDto> findReviewStatsByGroupBuyIds(@Param("groupBuyIds") List<Long> groupBuyIds);
```

**3. 서비스 로직 수정**

기존의 반복문 방식 대신, ID 목록으로 통계 `Map`을 미리 생성하고 DTO 변환 시 이를 활용합니다.

```java
// GroupBuyService.java (또는 관련 서비스)
public Page<GroupBuyListDto> searchGroupBuys(...) {
    Page<GroupBuy> groupBuys = groupBuyRepository.searchByKeyword(...);
    List<Long> groupBuyIds = groupBuys.getContent().stream()
                                      .map(GroupBuy::getId)
                                      .collect(Collectors.toList());

    // 1. ID 목록으로 모든 리뷰 통계를 한 번에 조회
    Map<Long, ReviewStatsDto> statsMap = reviewRepository.findReviewStatsByGroupBuyIds(groupBuyIds)
            .stream()
            .collect(Collectors.toMap(ReviewStatsDto::getGroupBuyId, Function.identity()));

    // 2. DTO 변환 시 Map을 사용하여 쿼리 없이 데이터 삽입
    return groupBuys.map(groupBuy -> {
        ReviewStatsDto stats = statsMap.getOrDefault(groupBuy.getId(), new ReviewStatsDto(groupBuy.getId(), 0.0, 0L));
        return GroupBuyConverter.toDto(groupBuy, stats.getAverageRating(), stats.getReviewCount());
    });
}
```

## 2. 사용자 로딩 N+1 문제 (심각도: 🟡 Medium)

공동구매 목록 조회 시, 각 공동구매의 주최자(`host`) 정보를 가져오기 위해 불필요한 추가 쿼리가 발생합니다.

### 원인 분석

`GroupBuyRepository`의 여러 조회 메소드(`searchByKeyword`, `findByStatusOrderByDeadlineAsc` 등)가 `GroupBuy` 엔티티를 조회할 때, 연관된 `host`(`User`) 엔티티를 함께 `JOIN`하지 않습니다. `GroupBuy`의 `host` 필드가 `FetchType.LAZY`이므로, 이후 서비스나 템플릿에서 `groupBuy.getHost().getNickname()`과 같이 접근할 때마다 새로운 쿼리가 발생합니다.

```java
// GroupBuyRepository.java의 문제 쿼리 예시
@Query("SELECT g FROM GroupBuy g WHERE ...")
Page<GroupBuy> searchByKeyword(...); // host 정보가 로드되지 않음
```

### 해결 방안: `JOIN FETCH` 또는 `@EntityGraph` 사용

**1. `JOIN FETCH` 사용 (권장)**

JPQL 쿼리에 `JOIN FETCH`를 추가하여 `GroupBuy`를 조회할 때 `User` 정보도 함께 가져옵니다.

```java
// GroupBuyRepository.java 수정
@Query(value = "SELECT g FROM GroupBuy g JOIN FETCH g.host WHERE ...",
       countQuery = "SELECT count(g) FROM GroupBuy g WHERE ...")
Page<GroupBuy> searchByKeyword(...);
```
*참고: `Pageable`과 함께 `JOIN FETCH`를 사용할 경우, count query를 별도로 지정해야 정확한 페이지네이션이 동작합니다.*

**2. `@EntityGraph` 사용**

`@EntityGraph` 어노테이션을 사용하면 쿼리 수정 없이 특정 연관 관계를 Eager하게 로드할 수 있습니다.

```java
// GroupBuyRepository.java 수정
@Override
@EntityGraph(attributePaths = {"host"})
Page<GroupBuy> findAll(Specification<GroupBuy> spec, Pageable pageable);
```

## 3. 중복 레이아웃 쿼리 (심각도: 🟢 Low)

모든 페이지 요청마다 현재 로그인한 사용자 정보, 미확인 알림 수, 미확인 쪽지 수를 반복적으로 조회하여 3~5개의 중복 쿼리가 발생합니다.

### 원인 분석

이러한 공통 정보는 모든 뷰에 전달되어야 하므로, `@ControllerAdvice`나 `HandlerInterceptor`에서 처리될 가능성이 높습니다. 해당 컴포넌트가 요청마다 DB에 직접 쿼리하여 모델에 데이터를 추가하고 있습니다.

### 해결 방안: 요청 범위 캐싱 및 Spring Security Context 활용

**1. 사용자 정보 조회 개선**

컨트롤러나 서비스에서 사용자 정보가 필요할 때마다 `userRepository.findByEmail()`을 호출하는 대신, Spring Security의 `Authentication` 객체를 직접 활용합니다.

```java
// 컨트롤러 메소드에서 사용자 정보 가져오기
@GetMapping("/some-page")
public String somePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    // userDetails 객체는 이미 인증 시점에 로드된 사용자 정보를 담고 있음
    // 추가 쿼리 없이 사용자 닉네임, 이메일 등에 접근 가능
    model.addAttribute("currentUser", userDetails.getUser());
    return "view";
}
```

**2. 알림 및 쪽지 수 캐싱**

`HandlerInterceptor`를 사용하여 요청 처리 초기에 한 번만 개수를 조회하고, 그 결과를 `HttpServletRequest`의 속성(attribute)에 저장합니다. 이후 컨트롤러나 뷰에서 필요할 때 DB 재조회 없이 이 값을 사용합니다.

```java
// NotificationInterceptor.java
@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationService notificationService;
    private final DirectMessageService directMessageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // DB 조회 후 요청(request) 범위에 캐싱
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            long unreadDms = directMessageService.getUnreadDmCount(user);
            request.setAttribute("unreadNotifications", unreadNotifications);
            request.setAttribute("unreadDms", unreadDms);
        }
        return true;
    }
}
```

이제 뷰(타임리프)에서는 `request.getAttribute("unreadNotifications")`를 통해 값에 접근할 수 있습니다. `@ControllerAdvice`를 사용한다면 모델에 추가하는 로직을 위와 같이 수정할 수 있습니다.
