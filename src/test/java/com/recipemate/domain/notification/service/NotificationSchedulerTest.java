//package com.recipemate.domain.notification.service;
//
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
//import com.recipemate.domain.notification.entity.Notification;
//import com.recipemate.domain.notification.repository.NotificationRepository;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import com.recipemate.global.common.NotificationType;
//import com.recipemate.global.common.UserRole;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class NotificationSchedulerTest {
//
//    @Autowired
//    private NotificationScheduler notificationScheduler;
//
//    @Autowired
//    private NotificationRepository notificationRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private GroupBuyRepository groupBuyRepository;
//
//    @Autowired
//    private EntityManager entityManager;
//
//    private User user1;
//    private User user2;
//    private GroupBuy groupBuy;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 사용자 생성
//        user1 = User.builder()
//                .email("test1@test.com")
//                .password("password123")
//                .nickname("테스터1")
//                .phoneNumber("010-1234-5678")
//                .role(UserRole.USER)
//                .build();
//        user1 = userRepository.save(user1);
//
//        user2 = User.builder()
//                .email("test2@test.com")
//                .password("password123")
//                .nickname("테스터2")
//                .phoneNumber("010-9876-5432")
//                .role(UserRole.USER)
//                .build();
//        user2 = userRepository.save(user2);
//
//        // 테스트용 공구 생성
//        groupBuy = GroupBuy.builder()
//                .host(user1)
//                .title("테스트 공구")
//                .content("테스트 설명")
//                .category("식재료")
//                .totalPrice(10000)
//                .targetHeadcount(10)
//                .currentHeadcount(1)
//                .deadline(LocalDateTime.now().plusDays(7))
//                .deliveryMethod(DeliveryMethod.PARCEL)
//                .meetupLocation(null)
//                .status(GroupBuyStatus.RECRUITING)
//                .isParticipantListPublic(false)
//                .build();
//        groupBuy = groupBuyRepository.save(groupBuy);
//    }
//
//    @Test
//    @DisplayName("30일 이상 지난 읽은 알림은 삭제된다")
//    void deleteOldReadNotifications_DeletesNotificationsOlderThan30Days() {
//        // given: 31일 전에 생성되고 읽은 알림
//        Notification oldReadNotification = createNotification(user1, 31, true);
//
//        // when
//        notificationScheduler.deleteOldReadNotifications();
//
//        // then
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).isEmpty();
//    }
//
//    @Test
//    @DisplayName("30일 이내의 읽은 알림은 삭제되지 않는다")
//    void deleteOldReadNotifications_DoesNotDeleteRecentReadNotifications() {
//        // given: 29일 전에 생성되고 읽은 알림
//        Notification recentReadNotification = createNotification(user1, 29, true);
//
//        // when
//        notificationScheduler.deleteOldReadNotifications();
//
//        // then
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).hasSize(1);
//        assertThat(notifications.get(0).getId()).isEqualTo(recentReadNotification.getId());
//    }
//
//    @Test
//    @DisplayName("읽지 않은 알림은 30일이 지나도 삭제되지 않는다")
//    void deleteOldReadNotifications_DoesNotDeleteUnreadNotifications() {
//        // given: 31일 전에 생성되었지만 읽지 않은 알림
//        Notification oldUnreadNotification = createNotification(user1, 31, false);
//
//        // when
//        notificationScheduler.deleteOldReadNotifications();
//
//        // then
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).hasSize(1);
//        assertThat(notifications.get(0).getIsRead()).isFalse();
//    }
//
//    @Test
//    @DisplayName("여러 사용자의 오래된 읽은 알림을 모두 삭제한다")
//    void deleteOldReadNotifications_DeletesOldReadNotificationsForMultipleUsers() {
//        // given: 두 사용자의 31일 전 읽은 알림
//        Notification user1OldRead = createNotification(user1, 31, true);
//        Notification user2OldRead = createNotification(user2, 31, true);
//
//        // when
//        notificationScheduler.deleteOldReadNotifications();
//
//        // then
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).isEmpty();
//    }
//
//    @Test
//    @DisplayName("다양한 상태의 알림을 올바르게 처리한다")
//    void deleteOldReadNotifications_HandlesVariousNotificationStates() {
//        // given: 다양한 상태의 알림들
//        Notification oldRead = createNotification(user1, 31, true); // 삭제 대상
//        Notification recentRead = createNotification(user1, 29, true); // 유지
//        Notification oldUnread = createNotification(user1, 31, false); // 유지
//        Notification recentUnread = createNotification(user1, 1, false); // 유지
//
//        Long oldReadId = oldRead.getId();
//        Long recentReadId = recentRead.getId();
//        Long oldUnreadId = oldUnread.getId();
//        Long recentUnreadId = recentUnread.getId();
//
//        // when
//        notificationScheduler.deleteOldReadNotifications();
//
//        // then
//        List<Notification> remainingNotifications = notificationRepository.findAll();
//        assertThat(remainingNotifications).hasSize(3);
//        assertThat(remainingNotifications.stream().map(Notification::getId))
//            .doesNotContain(oldReadId)
//            .contains(recentReadId, oldUnreadId, recentUnreadId);
//    }
//
//    @Test
//    @DisplayName("정확히 30일된 읽은 알림은 삭제되지 않는다")
//    void deleteOldReadNotifications_DoesNotDeleteExactly30DaysOldNotification() {
//        // given: 정확히 30일 전 읽은 알림 (하지만 밀리초 차이로 삭제될 수 있으므로 29일 23시간으로 테스트)
//        Notification notification = Notification.builder()
//                .user(user1)
//                .actor(null)
//                .type(NotificationType.GROUP_BUY_DEADLINE)
//                .content("테스트 알림 메시지")
//                .url("/group-buy/" + groupBuy.getId())
//                .relatedEntityType(com.recipemate.global.common.EntityType.GROUP_BUY)
//                .relatedEntityId(groupBuy.getId())
//                .isRead(false)
//                .build();
//
//        notification = notificationRepository.save(notification);
//        notification.markAsRead();
//
//        // 30일에서 1시간을 뺀 시간으로 설정 (30일 미만이므로 삭제되지 않아야 함)
//        entityManager.flush();
//        entityManager.createQuery(
//            "UPDATE Notification n SET n.createdAt = :createdAt WHERE n.id = :id")
//            .setParameter("createdAt", LocalDateTime.now().minusDays(30).plusHours(1))
//            .setParameter("id", notification.getId())
//            .executeUpdate();
//
//        entityManager.flush();
//        entityManager.clear();
//
//        // when
//        notificationScheduler.deleteOldReadNotifications();
//
//        // then
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).hasSize(1);
//    }
//
//    @Test
//    @DisplayName("삭제할 알림이 없을 때 정상적으로 처리된다")
//    void deleteOldReadNotifications_HandlesNoNotificationsToDelete() {
//        // given: 삭제 대상이 아닌 알림들만 존재
//        Notification recentRead = createNotification(user1, 10, true);
//        Notification recentUnread = createNotification(user1, 5, false);
//
//        // when & then: 예외 없이 정상 실행
//        notificationScheduler.deleteOldReadNotifications();
//
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).hasSize(2);
//    }
//
//    @Test
//    @DisplayName("알림이 전혀 없을 때 정상적으로 처리된다")
//    void deleteOldReadNotifications_HandlesEmptyNotifications() {
//        // given: 알림이 하나도 없음
//
//        // when & then: 예외 없이 정상 실행
//        notificationScheduler.deleteOldReadNotifications();
//
//        List<Notification> notifications = notificationRepository.findAll();
//        assertThat(notifications).isEmpty();
//    }
//
//    /**
//     * 테스트용 알림 생성 헬퍼 메서드
//     * @param user 알림을 받을 사용자
//     * @param daysAgo 며칠 전에 생성되었는지
//     * @param isRead 읽음 여부
//     * @return 생성된 알림
//     */
//    private Notification createNotification(User user, int daysAgo, boolean isRead) {
//        Notification notification = Notification.builder()
//                .user(user)
//                .actor(null)
//                .type(NotificationType.GROUP_BUY_DEADLINE)
//                .content("테스트 알림 메시지")
//                .url("/group-buy/" + groupBuy.getId())
//                .relatedEntityType(com.recipemate.global.common.EntityType.GROUP_BUY)
//                .relatedEntityId(groupBuy.getId())
//                .isRead(false)
//                .build();
//
//        // 먼저 저장
//        notification = notificationRepository.save(notification);
//
//        // 읽음 처리
//        if (isRead) {
//            notification.markAsRead();
//        }
//
//        // EntityManager를 통해 직접 createdAt 업데이트
//        entityManager.flush();
//        entityManager.createQuery(
//            "UPDATE Notification n SET n.createdAt = :createdAt WHERE n.id = :id")
//            .setParameter("createdAt", LocalDateTime.now().minusDays(daysAgo))
//            .setParameter("id", notification.getId())
//            .executeUpdate();
//
//        // 변경사항 반영 및 엔티티 재조회
//        entityManager.flush();
//        entityManager.clear();
//
//        return notificationRepository.findById(notification.getId()).orElseThrow();
//    }
//}
