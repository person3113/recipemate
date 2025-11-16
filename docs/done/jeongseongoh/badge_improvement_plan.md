# 배지 시스템 개선 계획

## 1. 현행 배지 시스템 분석

현재 레시피메이트의 배지 시스템은 특정 조건을 만족했을 때 이벤트를 발생시켜 배지를 부여하는 방식으로 구현되어 있습니다.

- **이벤트 리스너**: `BadgeEventListener`가 `GroupBuyCreatedEvent`, `ParticipationCreatedEvent`, `ReviewCreatedEvent`를 수신하여 배지 부여 조건을 확인하고 `BadgeService`를 호출합니다.
- **배지 종류**: `BadgeType` enum에 총 4가지 배지가 정의되어 있습니다.

| 배지 종류 (Enum)     | 배지 이름        | 획득 조건 (설명)                     | 관련 이벤트                 |
| ---------------------- | ---------------- | ------------------------------------ | --------------------------- |
| `FIRST_GROUP_BUY`    | 첫 공구 주최자   | 첫 번째 공동구매 생성                | `GroupBuyCreatedEvent`      |
| `TEN_PARTICIPATIONS` | 열혈 참여자      | 공동구매 10회 참여                   | `ParticipationCreatedEvent` |
| `REVIEWER`           | 후기 마스터      | 후기 5개 작성                        | `ReviewCreatedEvent`        |
| `POPULAR_HOST`       | 인기 호스트      | 매너온도 40도 이상 달성              | `ReviewCreatedEvent`        |

- **로직**: `BadgeEventListener`는 각 이벤트가 발생할 때마다 관련 엔티티의 개수(공구 개설, 참여, 후기 작성 횟수)를 계산하여 조건 충족 시 배지를 부여합니다.

## 2. 문제점

현재 `마이페이지 > 내 배지` 화면에서는 사용자가 획득한 배지만을 보여줍니다. 어떤 종류의 배지가 있는지, 그리고 어떻게 하면 배지를 얻을 수 있는지에 대한 정보가 전혀 없어 사용자가 배지 시스템을 활용하기 어렵습니다. 이는 사용자의 참여를 유도하는 '도전과제'로서의 기능을 제대로 수행하지 못하는 문제입니다.

## 3. 개선 제안

사용자가 모든 배지 목록과 획득 조건, 그리고 자신의 달성 현황을 한눈에 볼 수 있는 **'배지 도전과제'** 페이지를 도입할 것을 제안합니다.

- **UI/UX 개선**:
    - 현재의 '내 배지' 페이지를 '배지 도전과제' 페이지로 변경합니다.
    - 페이지 상단에는 내가 획득한 배지 목록을 보여줍니다.
    - 페이지 하단에는 획득하지 못한 배지 목록을 '도전과제' 형태로 보여줍니다.
    - 각 도전과제에는 배지 아이콘, 이름, 획득 조건, 그리고 현재 진행 상황(예: "공동구매 참여 7/10회")을 명확하게 표시합니다.
    - 획득한 배지는 활성화된 상태로, 미획득 배지는 비활성화된 상태로 시각적으로 구분합니다.

- **기대 효과**:
    - **사용자 참여 증대**: 명확한 목표(배지)와 보상(진행률 표시)을 통해 사용자의 서비스 활동을 장려합니다.
    - **서비스 이해도 향상**: 배지 획득 조건을 통해 사용자가 서비스의 다양한 기능을 자연스럽게 탐색하고 사용해보도록 유도합니다.

## 4. 구현 계획

### 4.1. 백엔드 (Backend)

'배지 도전과제' 페이지에 필요한 모든 정보를 한 번에 제공하는 API 엔드포인트를 신설합니다.

**1. DTO (Data Transfer Object) 추가**

사용자의 배지 도전과제 현황을 전달할 `BadgeChallengeResponse` DTO를 생성합니다.

```java
// package com.recipemate.domain.badge.dto;

@Getter
@Builder
public class BadgeChallengeResponse {
    private String displayName;      // 배지 이름 (예: "열혈 참여자")
    private String description;      // 획득 조건 (예: "공동구매에 10회 참여했습니다")
    private String iconUrl;          // 배지 아이콘 URL (추후 확장을 위해 필드 추가)
    private boolean isAcquired;      // 획득 여부
    private String progress;         // 진행 상황 (예: "1/1", "7/10", "3/5")
    private long currentCount;       // 현재 달성 횟수
    private long goalCount;          // 목표 횟수
}
```

**2. 서비스 로직 수정 (`BadgeService`)**

모든 배지 타입에 대한 사용자의 현재 진행 상황을 계산하여 `BadgeChallengeResponse` 리스트를 반환하는 메서드를 추가합니다.

```java
// in BadgeService.java

public List<BadgeChallengeResponse> getBadgeChallenges(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    List<Badge> acquiredBadges = badgeRepository.findByUserId(userId);

    return Arrays.stream(BadgeType.values())
            .map(badgeType -> {
                boolean isAcquired = acquiredBadges.stream()
                        .anyMatch(b -> b.getBadgeType() == badgeType);

                long currentCount = 0;
                long goalCount = 0;

                switch (badgeType) {
                    case FIRST_GROUP_BUY:
                        currentCount = groupBuyRepository.countByHostId(user.getId());
                        goalCount = 1;
                        break;
                    case TEN_PARTICIPATIONS:
                        currentCount = participationRepository.countByUserId(user.getId());
                        goalCount = 10;
                        break;
                    case REVIEWER:
                        currentCount = reviewRepository.countByReviewerId(user.getId());
                        goalCount = 5;
                        break;
                    case POPULAR_HOST:
                        currentCount = user.getMannerTemperature(); // User 엔티티에 mannerTemperature 필드 가정
                        goalCount = 40;
                        break;
                }
                
                // 획득했으면 현재 카운트를 목표 카운트로 설정
                if (isAcquired && currentCount < goalCount) {
                    currentCount = goalCount;
                }

                return BadgeChallengeResponse.builder()
                        .displayName(badgeType.getDisplayName())
                        .description(badgeType.getDescription())
                        .isAcquired(isAcquired)
                        .progress(String.format("%d/%d", Math.min(currentCount, goalCount), goalCount))
                        .currentCount(currentCount)
                        .goalCount(goalCount)
                        .build();
            })
            .collect(Collectors.toList());
}
```

**3. 컨트롤러 수정 (`UserController` 또는 `BadgeController`)**

기존의 배지 페이지 라우팅 메서드가 새로운 서비스 로직을 호출하여 `BadgeChallengeResponse` 리스트를 모델에 담아 뷰로 전달하도록 수정합니다.

```java
// in UserController.java or new BadgeController.java

@GetMapping("/me/badges")
public String getMyBadgesPage(Model model, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    List<BadgeChallengeResponse> badgeChallenges = badgeService.getBadgeChallenges(userPrincipal.getId());
    
    List<BadgeChallengeResponse> acquiredBadges = badgeChallenges.stream()
        .filter(BadgeChallengeResponse::isAcquired)
        .collect(Collectors.toList());

    List<BadgeChallengeResponse> challenges = badgeChallenges.stream()
        .filter(b -> !b.isAcquired())
        .collect(Collectors.toList());

    model.addAttribute("acquiredBadges", acquiredBadges);
    model.addAttribute("challenges", challenges);
    
    return "user/badges"; // 뷰 템플릿 경로
}
```

### 4.2. 프론트엔드 (Frontend)

Thymeleaf 템플릿을 수정하여 도전과제 목록을 렌더링합니다.

**`templates/user/badges.html` (가칭)**

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <title>내 배지 및 도전과제</title>
    <style>
        .badge-card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
        .badge-card.acquired { background-color: #e0f7fa; }
        .progress-bar { background-color: #e0e0e0; border-radius: 5px; overflow: hidden; }
        .progress { background-color: #007bff; height: 20px; color: white; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <h1>획득한 배지</h1>
        <div th:if="${#lists.isEmpty(acquiredBadges)}">
            <p>아직 획득한 배지가 없습니다. 도전과제를 확인해보세요!</p>
        </div>
        <div class="badge-grid" th:unless="${#lists.isEmpty(acquiredBadges)}">
            <!-- 획득한 배지 카드 -->
            <div th:each="badge : ${acquiredBadges}" class="badge-card acquired">
                <h3 th:text="${badge.displayName}">배지 이름</h3>
                <p th:text="${badge.description}">배지 설명</p>
            </div>
        </div>

        <hr>

        <h1>도전과제</h1>
        <div class="challenge-grid">
            <!-- 미획득 배지 (도전과제) 카드 -->
            <div th:each="challenge : ${challenges}" class="badge-card">
                <h3 th:text="${challenge.displayName}">도전과제 이름</h3>
                <p th:text="${challenge.description}">도전과제 설명</p>
                <div class="progress-bar">
                    <div class="progress" th:style="'width: ' + (${challenge.currentCount} * 100 / ${challenge.goalCount}) + '%;'" th:text="${challenge.progress}">
                        7/10
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

## 5. 추가 고려사항

- **`mannerTemperature` 필드 설계**: `POPULAR_HOST` 배지 조건인 '매너온도' 계산과 관련하여, `MannerTempHistory` 엔티티가 이미 존재하므로 두 가지 접근을 고려할 수 있습니다.
    1.  **실시간 계산**: 조회 시마다 `MannerTempHistory` 기록을 모두 읽어 계산합니다. 데이터 정합성은 보장되나, 기록이 많아지면 성능이 저하될 수 있습니다.
    2.  **역정규화 (Denormalization)**: `User` 엔티티에 `mannerTemperature` 필드를 추가하고, `MannerTempHistory`에 변동이 생길 때마다 이 필드를 업데이트합니다. 조회가 매우 빠르지만, 데이터 동기화 로직이 추가로 필요합니다.
    - **권장 방식**: 성능 이점을 위해 **역정규화** 방식을 추천합니다. `User` 엔티티에 `mannerTemperature` 필드를 추가하고, 이벤트 리스너 등을 활용해 `MannerTempHistory` 변경 시 `User`의 필드를 업데이트하여 데이터 일관성을 유지하는 것이 효율적입니다.

- **성능 (캐싱 전략)**: `getBadgeChallenges` 메서드는 현재 여러 번의 DB 조회를 유발합니다. 향후 성능 개선이 필요할 경우, Spring의 캐시 추상화를 활용할 수 있습니다.
    - **구현 예시**:
        1.  `@EnableCaching` 어노테이션을 메인 애플리케이션에 추가하여 캐시 기능을 활성화합니다.
        2.  `BadgeService`의 `getBadgeChallenges` 메서드에 `@Cacheable`을 추가하여 메서드 결과를 캐시에 저장합니다.
            ```java
            @Cacheable(value = "badgeChallenges", key = "#userId")
            public List<BadgeChallengeResponse> getBadgeChallenges(Long userId) { ... }
            ```
        3.  배지 진행도에 영향을 주는 활동(예: 후기 작성, 공구 참여)이 발생했을 때, 관련 로직에서 `@CacheEvict` 어노테이션을 사용해 해당 사용자의 캐시를 무효화해야 합니다.
            ```java
            @CacheEvict(value = "badgeChallenges", key = "#userId")
            public void someActivityThatAffectsBadges(Long userId) { ... }
            ```
    - 이는 동일한 사용자에 대한 반복적인 요청이 발생할 때 DB 부하 없이 캐시된 데이터를 즉시 반환하여 응답 속도를 크게 향상시킬 수 있는 일반적인 최적화 방법입니다.

- **아이콘**: `BadgeChallengeResponse`에 `iconUrl` 필드를 추가하여, 향후 각 배지에 고유한 아이콘을 부여할 수 있도록 확장성을 확보했습니다.
