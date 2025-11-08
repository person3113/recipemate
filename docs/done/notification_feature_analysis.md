# 알림 기능 현황 분석 및 최종 점검 보고서

## 1. 현황 요약

이전 분석 이후 코드베이스가 크게 개선되어, 기획서에 명시된 대부분의 알림 관련 기능이 **논리적으로 완벽하게 구현**되었습니다. 신규 `NotificationController` 도입, 누락되었던 이벤트 리스너 추가, API 경로 수정 등 모든 권장 사항이 정확히 반영된 것을 확인했습니다.

하지만, 이러한 올바른 로직에도 불구하고 실제 알림이 생성되지 않는 문제가 발생하고 있습니다. 이는 코드의 로직이 아닌, **이벤트 리스너의 트랜잭션 처리 방식**이라는 기술적인 문제에 기인합니다.

본 문서는 현재 코드의 높은 완성도를 전제로, 알림 생성을 막고 있는 마지막 기술적 원인을 명확히 진단하고 최종 해결책을 제시합니다.

---

## 2. 최종 진단: 알림이 생성되지 않는 근본 원인

### 2.1. 문제 현상

정상적으로 알림을 발생시켜야 하는 활동(타 사용자의 게시글에 댓글 작성, 공구 참여 등)을 수행했음에도 불구하고, 수신자에게 알림이 전혀 생성되지 않습니다. 이는 이벤트 기반 알림 시스템의 핵심 로직이 실행 과정에서 조용히 실패하고 있음을 의미합니다.

### 2.2. 원인 분석: `@TransactionalEventListener`와 트랜잭션의 분리 문제

알림 리스너(`NotificationEventListener`)는 `@TransactionalEventListener`를 사용합니다. 이 어노테이션의 기본 동작은 이벤트를 발생시킨 **메인 트랜잭션이 성공적으로 커밋된 후(`AFTER_COMMIT`)** 리스너 메서드를 실행하는 것입니다. 이것은 매우 안정적인 패턴이지만, 현재 코드에서는 한 가지 함정이 있습니다.

1.  **메인 트랜잭션 커밋**: `ParticipationService`에서 참여 정보가 저장되고 트랜잭션이 성공적으로 커밋됩니다.
2.  **리스너 실행**: 직후, `NotificationEventListener`의 `handleParticipationCreatedEvent` 메서드가 호출됩니다.
3.  **문제 발생**: 이 리스너 메서드는 메인 트랜잭션과 완전히 분리된 새로운 컨텍스트에서 동작하는데, 이때 **자체적인 트랜잭션 범위 없이 실행됩니다.**
4.  **데이터 조회 실패**: 리스너가 `groupBuyRepository.findById()`로 방금 커밋된 데이터를 조회하려 할 때, 데이터베이스의 트랜잭션 격리 수준(Isolation Level)에 따라 해당 데이터가 아직 현재 리스너에게 **보이지 않을 수 있습니다.**
5.  **조용한 실패**: 결국 `findById()`는 `Optional.empty()`를 반환하고, `if (groupBuy != null)` 조건문이 실패하여 리스너는 아무런 동작 없이 조용히 종료됩니다. 에러 로그가 남지 않아 원인을 파악하기 매우 어렵습니다.

이것이 모든 로직이 완벽함에도 알림이 생성되지 않는 현상의 근본적인 원인입니다.

---

## 3. 최종 해결 방안: 리스너에 독립적인 트랜잭션 보장

이 문제를 해결하는 가장 안정적이고 표준적인 방법은, 리스너 메서드가 실행될 때 **완전히 독립된 새로운 트랜잭션** 내에서 동작하도록 보장하는 것입니다.

**수정 대상 파일**: `src/main/java/com/recipemate/global/event/listeners/NotificationEventListener.java`

`NotificationEventListener` 클래스에 있는 모든 `@TransactionalEventListener` 메서드 위에 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 어노테이션을 추가합니다.

```java
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

// ...

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    // ...

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW) // <-- 이 부분 추가
    public void handleParticipationCreatedEvent(ParticipationCreatedEvent event) {
        GroupBuy groupBuy = groupBuyRepository.findById(event.getGroupBuyId()).orElse(null);
        if (groupBuy != null) {
            notificationService.createNotification(
                    groupBuy.getHost().getId(),
                    NotificationType.JOIN_GROUP_BUY,
                    event.getUserId(),
                    event.getGroupBuyId(),
                    EntityType.GROUP_BUY
            );
        }
    }

    // ... 다른 모든 @TransactionalEventListener 메서드에도 동일하게 추가 ...
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleParticipationCancelledEvent(ParticipationCancelledEvent event) { ... }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) { ... }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) { ... }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGroupBuyCompletedEvent(GroupBuyCompletedEvent event) { ... }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGroupBuyDeadlineEvent(GroupBuyDeadlineEvent event) { ... }
}
```

### 해결 원리

- `@Transactional(propagation = Propagation.REQUIRES_NEW)`는 해당 메서드가 호출될 때, 기존에 진행 중인 트랜잭션의 존재 여부와 상관없이 **항상 새로운 물리적 트랜잭션을 시작**하도록 강제합니다.
- 이로써 리스너는 메인 트랜잭션이 완전히 커밋된 결과를 명확히 볼 수 있는 상태에서, 자신만의 격리된 트랜잭션을 시작하게 됩니다.
- 데이터베이스 조회 시점에 데이터 정합성이 완벽히 보장되므로, `findById()`가 정상적으로 엔티티를 찾아오고 후속 알림 생성 로직이 안정적으로 실행될 수 있습니다.

이 수정은 현재 거의 완성된 알림 기능의 마지막 기술적 문제를 해결하고, 전체 시스템을 안정적으로 동작하게 만들 것입니다.