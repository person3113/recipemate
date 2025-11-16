//package com.recipemate.domain.notification;
//
//import com.recipemate.domain.notification.entity.Notification;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.global.common.EntityType;
//import com.recipemate.global.common.NotificationType;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class NotificationTest {
//
//    @Test
//    @DisplayName("Notification 생성 시 isRead 기본값은 false이다")
//    void defaultIsReadFalse() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                "새로운 알림입니다",
//                NotificationType.COMMENT_POST,
//                "/posts/1",
//                1L,
//                EntityType.POST
//        );
//
//        assertThat(notification.getIsRead()).isFalse();
//    }
//
//    @Test
//    @DisplayName("Notification 생성 시 수신자 정보가 저장된다")
//    void createWithRecipient() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                "새로운 알림입니다",
//                NotificationType.COMMENT_POST,
//                "/posts/1",
//                1L,
//                EntityType.POST
//        );
//
//        assertThat(notification.getUser()).isEqualTo(recipient);
//    }
//
//    @Test
//    @DisplayName("Notification 생성 시 actor 정보가 저장된다")
//    void createWithActor() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//        User actor = User.create(
//                "actor@example.com",
//                "encodedPassword",
//                "행동자",
//                "010-9999-8888"
//        );
//
//        Notification notification = Notification.create(
//                recipient,
//                actor,
//                "새로운 댓글이 달렸습니다",
//                NotificationType.COMMENT_POST,
//                "/posts/1",
//                1L,
//                EntityType.POST
//        );
//
//        assertThat(notification.getActor()).isEqualTo(actor);
//    }
//
//    @Test
//    @DisplayName("Notification 생성 시 actor가 없을 수 있다")
//    void createWithoutActor() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                "공구 마감일이 임박했습니다",
//                NotificationType.GROUP_BUY_DEADLINE,
//                "/group-buys/1",
//                1L,
//                EntityType.GROUP_BUY
//        );
//
//        assertThat(notification.getActor()).isNull();
//    }
//
//    @Test
//    @DisplayName("Notification 생성 시 content와 type이 저장된다")
//    void createWithContentAndType() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        String content = "새로운 댓글이 달렸습니다";
//        NotificationType type = NotificationType.COMMENT_POST;
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                content,
//                type,
//                "/posts/1",
//                1L,
//                EntityType.POST
//        );
//
//        assertThat(notification.getContent()).isEqualTo(content);
//        assertThat(notification.getType()).isEqualTo(type);
//    }
//
//    @Test
//    @DisplayName("Notification 생성 시 관련 엔티티 정보가 저장된다")
//    void createWithRelatedEntity() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        Long relatedEntityId = 1L;
//        EntityType relatedEntityType = EntityType.POST;
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                "새로운 댓글이 달렸습니다",
//                NotificationType.COMMENT_POST,
//                "/posts/1",
//                relatedEntityId,
//                relatedEntityType
//        );
//
//        assertThat(notification.getRelatedEntityId()).isEqualTo(relatedEntityId);
//        assertThat(notification.getRelatedEntityType()).isEqualTo(relatedEntityType);
//    }
//
//    @Test
//    @DisplayName("Notification 생성 시 URL이 저장된다")
//    void createWithUrl() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        String url = "/posts/1";
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                "새로운 댓글이 달렸습니다",
//                NotificationType.COMMENT_POST,
//                url,
//                1L,
//                EntityType.POST
//        );
//
//        assertThat(notification.getUrl()).isEqualTo(url);
//    }
//
//    @Test
//    @DisplayName("Notification을 읽음으로 표시할 수 있다")
//    void markAsRead() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        Notification notification = Notification.create(
//                recipient,
//                null,
//                "새로운 알림입니다",
//                NotificationType.COMMENT_POST,
//                "/posts/1",
//                1L,
//                EntityType.POST
//        );
//
//        notification.markAsRead();
//
//        assertThat(notification.getIsRead()).isTrue();
//    }
//
//    @Test
//    @DisplayName("여러 NotificationType을 사용하여 Notification을 생성할 수 있다")
//    void createWithVariousTypes() {
//        User recipient = User.create(
//                "recipient@example.com",
//                "encodedPassword",
//                "수신자",
//                "010-1234-5678"
//        );
//
//        Notification joinNotification = Notification.create(
//                recipient, null, "공구에 참여했습니다",
//                NotificationType.JOIN_GROUP_BUY, "/group-buys/1", 1L, EntityType.GROUP_BUY
//        );
//
//        Notification cancelNotification = Notification.create(
//                recipient, null, "공구 참여가 취소되었습니다",
//                NotificationType.CANCEL_PARTICIPATION, "/group-buys/1", 1L, EntityType.GROUP_BUY
//        );
//
//        Notification reviewNotification = Notification.create(
//                recipient, null, "새로운 후기가 작성되었습니다",
//                NotificationType.REVIEW_GROUP_BUY, "/group-buys/1", 1L, EntityType.REVIEW
//        );
//
//        Notification completedNotification = Notification.create(
//                recipient, null, "공구가 완료되었습니다",
//                NotificationType.GROUP_BUY_COMPLETED, "/group-buys/1", 1L, EntityType.GROUP_BUY
//        );
//
//        assertThat(joinNotification.getType()).isEqualTo(NotificationType.JOIN_GROUP_BUY);
//        assertThat(cancelNotification.getType()).isEqualTo(NotificationType.CANCEL_PARTICIPATION);
//        assertThat(reviewNotification.getType()).isEqualTo(NotificationType.REVIEW_GROUP_BUY);
//        assertThat(completedNotification.getType()).isEqualTo(NotificationType.GROUP_BUY_COMPLETED);
//    }
//}
