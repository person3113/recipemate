//package com.recipemate.domain.notification.repository;
//
//import com.recipemate.domain.notification.entity.Notification;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.EntityType;
//import com.recipemate.global.common.NotificationType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//
//import java.lang.reflect.Field;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(com.recipemate.global.config.QueryDslConfig.class)
//class NotificationRepositoryTest {
//
//    @Autowired
//    private NotificationRepository notificationRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User recipient;
//    private User actor;
//
//    @BeforeEach
//    void setUp() {
//        recipient = User.create(
//                "recipient@example.com",
//                "password",
//                "수신자",
//                "010-1234-5678"
//        );
//        userRepository.save(recipient);
//
//        actor = User.create(
//                "actor@example.com",
//                "password",
//                "행동자",
//                "010-9999-8888"
//        );
//        userRepository.save(actor);
//    }
//
//    private void setCreatedAt(Notification notification, LocalDateTime createdAt) throws Exception {
//        Field createdAtField = notification.getClass().getSuperclass().getDeclaredField("createdAt");
//        createdAtField.setAccessible(true);
//        createdAtField.set(notification, createdAt);
//    }
//
//    @Test
//    @DisplayName("사용자의 읽지 않은 알림 목록을 최신순으로 조회할 수 있다")
//    void findUnreadNotificationsByUserId() throws InterruptedException {
//        // given
//        Notification notification1 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 1",
//                NotificationType.COMMENT_POST, "/posts/1", 1L, EntityType.POST
//        );
//        notificationRepository.save(notification1);
//        Thread.sleep(10);
//
//        Notification notification2 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 2",
//                NotificationType.COMMENT_POST, "/posts/2", 2L, EntityType.POST
//        );
//        notification2.markAsRead(); // notification2만 읽음 처리
//        notificationRepository.save(notification2);
//        Thread.sleep(10);
//
//        Notification notification3 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 3",
//                NotificationType.COMMENT_POST, "/posts/3", 3L, EntityType.POST
//        );
//        notificationRepository.save(notification3);
//
//        // when
//        List<Notification> unreadNotifications = notificationRepository
//                .findByUserIdAndIsReadOrderByCreatedAtDesc(recipient.getId(), false);
//
//        // then
//        assertThat(unreadNotifications).hasSize(2);
//        assertThat(unreadNotifications.get(0).getContent()).isEqualTo("댓글이 달렸습니다 3");
//        assertThat(unreadNotifications.get(1).getContent()).isEqualTo("댓글이 달렸습니다 1");
//    }
//
//    @Test
//    @DisplayName("사용자의 읽은 알림 목록을 최신순으로 조회할 수 있다")
//    void findReadNotificationsByUserId() {
//        // given
//        Notification notification1 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 1",
//                NotificationType.COMMENT_POST, "/posts/1", 1L, EntityType.POST
//        );
//        Notification notification2 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 2",
//                NotificationType.COMMENT_POST, "/posts/2", 2L, EntityType.POST
//        );
//        notification1.markAsRead();
//        notification2.markAsRead();
//
//        notificationRepository.save(notification1);
//        notificationRepository.save(notification2);
//
//        // when
//        List<Notification> readNotifications = notificationRepository
//                .findByUserIdAndIsReadOrderByCreatedAtDesc(recipient.getId(), true);
//
//        // then
//        assertThat(readNotifications).hasSize(2);
//        assertThat(readNotifications).allMatch(Notification::getIsRead);
//    }
//
//    @Test
//    @DisplayName("사용자의 읽지 않은 알림 개수를 조회할 수 있다")
//    void countUnreadNotificationsByUserId() {
//        // given
//        Notification notification1 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 1",
//                NotificationType.COMMENT_POST, "/posts/1", 1L, EntityType.POST
//        );
//        Notification notification2 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 2",
//                NotificationType.COMMENT_POST, "/posts/2", 2L, EntityType.POST
//        );
//        Notification notification3 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 3",
//                NotificationType.COMMENT_POST, "/posts/3", 3L, EntityType.POST
//        );
//        notification1.markAsRead();
//
//        notificationRepository.save(notification1);
//        notificationRepository.save(notification2);
//        notificationRepository.save(notification3);
//
//        // when
//        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(recipient.getId());
//
//        // then
//        assertThat(unreadCount).isEqualTo(2L);
//    }
//
//    @Test
//    @DisplayName("다른 사용자의 알림은 조회되지 않는다")
//    void findNotificationsByDifferentUser() {
//        // given
//        User otherUser = User.create(
//                "other@example.com",
//                "password",
//                "다른사용자",
//                "010-5555-5555"
//        );
//        userRepository.save(otherUser);
//
//        Notification notification1 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다",
//                NotificationType.COMMENT_POST, "/posts/1", 1L, EntityType.POST
//        );
//        Notification notification2 = Notification.create(
//                otherUser, actor, "다른 사용자의 알림",
//                NotificationType.COMMENT_POST, "/posts/2", 2L, EntityType.POST
//        );
//
//        notificationRepository.save(notification1);
//        notificationRepository.save(notification2);
//
//        // when
//        List<Notification> recipientNotifications = notificationRepository
//                .findByUserIdAndIsReadOrderByCreatedAtDesc(recipient.getId(), false);
//
//        // then
//        assertThat(recipientNotifications).hasSize(1);
//        assertThat(recipientNotifications.get(0).getUser()).isEqualTo(recipient);
//    }
//
//    @Test
//    @DisplayName("알림이 없을 때 읽지 않은 알림 개수는 0이다")
//    void countUnreadNotificationsWhenNoNotifications() {
//        // when
//        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(recipient.getId());
//
//        // then
//        assertThat(unreadCount).isEqualTo(0L);
//    }
//
//    @Test
//    @DisplayName("모든 알림을 읽었을 때 읽지 않은 알림 개수는 0이다")
//    void countUnreadNotificationsWhenAllRead() {
//        // given
//        Notification notification1 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 1",
//                NotificationType.COMMENT_POST, "/posts/1", 1L, EntityType.POST
//        );
//        Notification notification2 = Notification.create(
//                recipient, actor, "댓글이 달렸습니다 2",
//                NotificationType.COMMENT_POST, "/posts/2", 2L, EntityType.POST
//        );
//        notification1.markAsRead();
//        notification2.markAsRead();
//
//        notificationRepository.save(notification1);
//        notificationRepository.save(notification2);
//
//        // when
//        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(recipient.getId());
//
//        // then
//        assertThat(unreadCount).isEqualTo(0L);
//    }
//}
