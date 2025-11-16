package com.recipemate.domain.notification.service;

import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.notification.dto.NotificationResponse;
import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.domain.notification.repository.NotificationRepository;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User recipient;
    private User actor;
    private GroupBuy groupBuy;
    private Post post;

    @BeforeEach
    void setUp() {
        recipient = User.builder()
                .id(1L)
                .email("recipient@test.com")
                .nickname("수신자")
                .build();

        actor = User.builder()
                .id(2L)
                .email("actor@test.com")
                .nickname("행동자")
                .build();

        groupBuy = GroupBuy.builder()
                .id(100L)
                .host(recipient)
                .title("김치 공동구매")
                .status(GroupBuyStatus.RECRUITING)
                .deadline(LocalDateTime.now().plusDays(5))
                .build();

        post = Post.builder()
                .id(200L)
                .author(recipient)
                .title("테스트 게시글")
                .category(PostCategory.FREE)
                .build();
    }

    @Test
    @DisplayName("공구 참여 알림 생성 - 성공")
    void createNotification_JoinGroupBuy_Success() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = actor.getId();
        Long groupBuyId = groupBuy.getId();

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.JOIN_GROUP_BUY,
                actorId,
                groupBuyId,
                EntityType.GROUP_BUY
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getUser()).isEqualTo(recipient);
        assertThat(savedNotification.getActor()).isEqualTo(actor);
        assertThat(savedNotification.getType()).isEqualTo(NotificationType.JOIN_GROUP_BUY);
        assertThat(savedNotification.getContent()).contains("행동자님이", "참여했습니다");
        assertThat(savedNotification.getUrl()).isEqualTo("/group-purchases/100");
        assertThat(savedNotification.getRelatedEntityId()).isEqualTo(groupBuyId);
        assertThat(savedNotification.getRelatedEntityType()).isEqualTo(EntityType.GROUP_BUY);
        assertThat(savedNotification.getIsRead()).isFalse();
    }

    @Test
    @DisplayName("공구 참여 취소 알림 생성 - 성공")
    void createNotification_CancelParticipation_Success() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = actor.getId();
        Long groupBuyId = groupBuy.getId();

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.CANCEL_PARTICIPATION,
                actorId,
                groupBuyId,
                EntityType.GROUP_BUY
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getContent()).contains("행동자님이", "참여를 취소했습니다");
    }

    @Test
    @DisplayName("공구 댓글 알림 생성 - 성공")
    void createNotification_CommentGroupBuy_Success() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = actor.getId();
        Long commentId = 300L;

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.COMMENT_GROUP_BUY,
                actorId,
                commentId,
                EntityType.COMMENT
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getContent()).contains("행동자님이", "댓글을 작성했습니다");
        assertThat(savedNotification.getUrl()).isEqualTo("/comments/300");
    }

    @Test
    @DisplayName("게시글 댓글 알림 생성 - 성공")
    void createNotification_CommentPost_Success() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = actor.getId();
        Long commentId = 300L;

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.COMMENT_POST,
                actorId,
                commentId,
                EntityType.COMMENT
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getContent()).contains("행동자님이", "댓글을 작성했습니다");
    }

    @Test
    @DisplayName("대댓글 알림 생성 - 성공")
    void createNotification_ReplyComment_Success() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = actor.getId();
        Long replyId = 301L;

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.REPLY_COMMENT,
                actorId,
                replyId,
                EntityType.COMMENT
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getContent()).contains("행동자님이", "답글을 작성했습니다");
    }

    @Test
    @DisplayName("후기 알림 생성 - 성공")
    void createNotification_ReviewGroupBuy_Success() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = actor.getId();
        Long reviewId = 400L;

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.REVIEW_GROUP_BUY,
                actorId,
                reviewId,
                EntityType.REVIEW
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getContent()).contains("행동자님이", "후기를 작성했습니다");
        assertThat(savedNotification.getUrl()).isEqualTo("/reviews/400");
    }

    @Test
    @DisplayName("본인 행동은 알림 생성 안 함")
    void createNotification_SameUserShouldNotCreateNotification() {
        // given
        Long userId = recipient.getId();

        // when
        notificationService.createNotification(
                userId,
                NotificationType.JOIN_GROUP_BUY,
                userId, // actor와 recipient가 같음
                groupBuy.getId(),
                EntityType.GROUP_BUY
        );

        // then
        then(notificationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("알림 생성 실패 - 수신자 사용자 없음")
    void createNotification_RecipientNotFound_ThrowsException() {
        // given
        Long recipientId = 999L;
        Long actorId = actor.getId();

        given(userRepository.findById(recipientId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.createNotification(
                recipientId,
                NotificationType.JOIN_GROUP_BUY,
                actorId,
                groupBuy.getId(),
                EntityType.GROUP_BUY
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        then(notificationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("알림 생성 실패 - 행동자 사용자 없음")
    void createNotification_ActorNotFound_ThrowsException() {
        // given
        Long recipientId = recipient.getId();
        Long actorId = 999L;

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));
        given(userRepository.findById(actorId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.createNotification(
                recipientId,
                NotificationType.JOIN_GROUP_BUY,
                actorId,
                groupBuy.getId(),
                EntityType.GROUP_BUY
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        then(notificationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("알림 템플릿 - 공구 마감 알림")
    void createNotification_GroupBuyDeadline_Success() {
        // given
        Long recipientId = recipient.getId();

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.GROUP_BUY_DEADLINE,
                null, // 시스템 알림이므로 actor 없음
                groupBuy.getId(),
                EntityType.GROUP_BUY
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getActor()).isNull();
        assertThat(savedNotification.getContent()).contains("찜한 공구가 곧 마감됩니다");
    }

    @Test
    @DisplayName("알림 템플릿 - 공구 목표 달성 알림")
    void createNotification_GroupBuyCompleted_Success() {
        // given
        Long recipientId = recipient.getId();

        given(userRepository.findById(recipientId)).willReturn(Optional.of(recipient));

        // when
        notificationService.createNotification(
                recipientId,
                NotificationType.GROUP_BUY_COMPLETED,
                null,
                groupBuy.getId(),
                EntityType.GROUP_BUY
        );

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        then(notificationRepository).should().save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getContent()).contains("참여한 공구가 목표 인원을 달성했습니다");
    }

    @Test
    @DisplayName("알림 목록 조회 - 전체 조회")
    void getNotifications_AllNotifications_Success() {
        // given
        Long userId = recipient.getId();
        List<Notification> notifications = createMockNotifications();

        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .willReturn(notifications);

        // when
        List<NotificationResponse> result = notificationService.getNotifications(userId, null);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).contains("참여했습니다");
        assertThat(result.get(1).getContent()).contains("댓글을 작성했습니다");
        assertThat(result.get(2).getContent()).contains("곧 마감됩니다");
    }

    @Test
    @DisplayName("알림 목록 조회 - 읽지 않은 알림만")
    void getNotifications_UnreadOnly_Success() {
        // given
        Long userId = recipient.getId();
        List<Notification> unreadNotifications = createMockNotifications().stream()
                .filter(n -> !n.getIsRead())
                .toList();

        given(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false))
                .willReturn(unreadNotifications);

        // when
        List<NotificationResponse> result = notificationService.getNotifications(userId, false);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(n -> !n.getIsRead());
    }

    @Test
    @DisplayName("알림 목록 조회 - 읽은 알림만")
    void getNotifications_ReadOnly_Success() {
        // given
        Long userId = recipient.getId();
        List<Notification> readNotifications = createMockNotifications().stream()
                .filter(Notification::getIsRead)
                .toList();

        given(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, true))
                .willReturn(readNotifications);

        // when
        List<NotificationResponse> result = notificationService.getNotifications(userId, true);

        // then
        assertThat(result).hasSize(1);
        assertThat(result).allMatch(NotificationResponse::getIsRead);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void markNotificationAsRead_Success() {
        // given
        Long userId = recipient.getId();
        Long notificationId = 1L;

        Notification notification = Notification.builder()
                .id(notificationId)
                .user(recipient)
                .actor(actor)
                .content("테스트 알림")
                .type(NotificationType.JOIN_GROUP_BUY)
                .isRead(false)
                .build();

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

        // when
        notificationService.markNotificationAsRead(userId, notificationId);

        // then
        assertThat(notification.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 처리 - 알림 없음")
    void markNotificationAsRead_NotificationNotFound_ThrowsException() {
        // given
        Long userId = recipient.getId();
        Long notificationId = 999L;

        given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.markNotificationAsRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 권한 없음")
    void markNotificationAsRead_Unauthorized_ThrowsException() {
        // given
        Long userId = 999L;
        Long notificationId = 1L;

        Notification notification = Notification.builder()
                .id(notificationId)
                .user(recipient)
                .actor(actor)
                .content("테스트 알림")
                .type(NotificationType.JOIN_GROUP_BUY)
                .isRead(false)
                .build();

        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.markNotificationAsRead(userId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("전체 알림 삭제 - 성공")
    void deleteAllNotifications_Success() {
        // given
        Long userId = recipient.getId();

        // when
        notificationService.deleteAllNotifications(userId);

        // then
        then(notificationRepository).should().deleteByUserId(userId);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 - 성공")
    void getUnreadCount_Success() {
        // given
        Long userId = recipient.getId();
        Long expectedCount = 5L;

        given(notificationRepository.countByUserIdAndIsReadFalse(userId)).willReturn(expectedCount);

        // when
        Long result = notificationService.getUnreadCount(userId);

        // then
        assertThat(result).isEqualTo(expectedCount);
    }

    private List<Notification> createMockNotifications() {
        List<Notification> notifications = new ArrayList<>();

        notifications.add(Notification.builder()
                .id(1L)
                .user(recipient)
                .actor(actor)
                .content("행동자님이 공구에 참여했습니다.")
                .type(NotificationType.JOIN_GROUP_BUY)
                .url("/group-purchases/100")
                .isRead(false)
                .relatedEntityId(100L)
                .relatedEntityType(EntityType.GROUP_BUY)
                .build());

        notifications.add(Notification.builder()
                .id(2L)
                .user(recipient)
                .actor(actor)
                .content("행동자님이 공구에 댓글을 작성했습니다.")
                .type(NotificationType.COMMENT_GROUP_BUY)
                .url("/comments/300")
                .isRead(false)
                .relatedEntityId(300L)
                .relatedEntityType(EntityType.COMMENT)
                .build());

        notifications.add(Notification.builder()
                .id(3L)
                .user(recipient)
                .actor(null)
                .content("찜한 공구가 곧 마감됩니다.")
                .type(NotificationType.GROUP_BUY_DEADLINE)
                .url("/group-purchases/100")
                .isRead(true)
                .relatedEntityId(100L)
                .relatedEntityType(EntityType.GROUP_BUY)
                .build());

        return notifications;
    }
}
