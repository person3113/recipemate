# 알림 페이지 페이징 처리 리팩토링 계획

## 1. 문제 상황

- `/users/me/notifications` 페이지에 접속 시 500 Internal Server Error 발생
- 오류 로그: `org.springframework.expression.spel.SpelEvaluationException: EL1004E: Method call: Method hasPrevious() cannot be found on type java.util.ArrayList`
- 발생 위치: `templates/user/notifications.html`

## 2. 원인 분석

- Thymeleaf 템플릿(`notifications.html`)에서는 페이징 처리를 위해 `Page` 객체를 사용하고 있으나, 컨트롤러에서 `List` 타입의 객체를 전달하고 있어 `Page` 객체의 메서드(`hasPrevious`, `hasNext` 등)를 찾지 못해 오류가 발생.
- `UserController` -> `NotificationService` -> `NotificationRepository` 전반에 걸쳐 페이징(Paging) 기능이 구현되어 있지 않음.

## 3. 해결 방안

- 데이터 조회 계층부터 프레젠테이션 계층까지 페이징 처리를 적용하여 `Page` 객체를 뷰에 전달하도록 수정.

### 3.1. `NotificationRepository` 수정

- `Pageable`을 파라미터로 받아 `Page<Notification>`을 반환하는 메서드를 추가.
- N+1 문제를 방지하기 위해 `JOIN FETCH`를 사용하고, 정확한 페이지 수 계산을 위해 `countQuery`를 명시.

**AS-IS**
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    // ...
}
```

**TO-BE**
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findByUserIdWithActor(Long userId, Pageable pageable);
    // ...
}
```

### 3.2. `NotificationService` 수정

- `getNotifications` 메서드가 `Pageable` 객체를 인자로 받도록 수정.
- 새로 추가된 리포지토리의 페이징 메서드를 호출하고, 결과를 `Page<NotificationResponse>`로 변환하여 반환.

**AS-IS**
```java
@Transactional(readOnly = true)
public List<NotificationResponse> getNotifications(Long userId) {
    List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    return notifications.stream()
            .map(NotificationResponse::from)
            .toList();
}
```

**TO-BE**
```java
@Transactional(readOnly = true)
public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
    Page<Notification> notifications = notificationRepository.findByUserIdWithActor(userId, pageable);
    return notifications.map(NotificationResponse::from);
}
```

### 3.3. `UserController` 수정

- `myNotificationsPage` 메서드가 `Pageable`을 파라미터로 받도록 수정.
- `@PageableDefault`를 사용하여 기본 정렬 순서(최신순)를 지정.
- 서비스 계층에 `Pageable` 객체를 전달하고, 반환된 `Page` 객체를 모델에 추가.

**AS-IS**
```java
@GetMapping("/me/notifications")
public String myNotificationsPage(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
    List<NotificationResponse> notifications = notificationService.getNotifications(userDetails.getUser().getId());
    model.addAttribute("notifications", notifications);
    return "user/notifications";
}
```

**TO-BE**
```java
@GetMapping("/me/notifications")
public String myNotificationsPage(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<NotificationResponse> notifications = notificationService.getNotifications(userDetails.getUser().getId(), pageable);
    model.addAttribute("notifications", notifications);
    return "user/notifications";
}
```

## 4. 기대 효과

- 알림 페이지의 페이징 기능이 정상적으로 동작.
- `JOIN FETCH`를 통해 N+1 쿼리 문제를 예방하여 성능 개선.
- 대량의 알림 데이터가 있어도 페이징을 통해 안정적으로 조회 가능.
