package com.recipemate.domain.notification.service;

import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.domain.notification.repository.NotificationRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성
     *
     * @param recipientId       알림 수신자 ID
     * @param type              알림 타입
     * @param actorId           행동자 ID (시스템 알림의 경우 null)
     * @param relatedEntityId   관련 엔티티 ID
     * @param relatedEntityType 관련 엔티티 타입
     */
    public void createNotification(
            Long recipientId,
            NotificationType type,
            Long actorId,
            Long relatedEntityId,
            EntityType relatedEntityType
    ) {
        // 본인 행동은 알림 생성하지 않음
        if (actorId != null && recipientId.equals(actorId)) {
            return;
        }

        // 수신자 조회
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 행동자 조회 (시스템 알림의 경우 null)
        User actor = null;
        if (actorId != null) {
            actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        // 알림 내용 생성
        String content = generateNotificationContent(type, actor);

        // URL 생성
        String url = generateNotificationUrl(relatedEntityType, relatedEntityId);

        // 알림 저장
        Notification notification = Notification.create(
                recipient,
                actor,
                content,
                type,
                url,
                relatedEntityId,
                relatedEntityType
        );

        notificationRepository.save(notification);
    }

    /**
     * 알림 타입에 따른 내용 생성
     */
    private String generateNotificationContent(NotificationType type, User actor) {
        String actorName = actor != null ? actor.getNickname() : null;

        return switch (type) {
            case JOIN_GROUP_BUY -> actorName + "님이 공구에 참여했습니다.";
            case CANCEL_PARTICIPATION -> actorName + "님이 공구 참여를 취소했습니다.";
            case COMMENT_GROUP_BUY -> actorName + "님이 공구에 댓글을 작성했습니다.";
            case COMMENT_POST -> actorName + "님이 게시글에 댓글을 작성했습니다.";
            case REPLY_COMMENT -> actorName + "님이 댓글에 답글을 작성했습니다.";
            case REVIEW_GROUP_BUY -> actorName + "님이 공구 후기를 작성했습니다.";
            case GROUP_BUY_DEADLINE -> "찜한 공구가 곧 마감됩니다.";
            case GROUP_BUY_COMPLETED -> "참여한 공구가 목표 인원을 달성했습니다.";
        };
    }

    /**
     * 엔티티 타입에 따른 URL 생성
     */
    private String generateNotificationUrl(EntityType entityType, Long entityId) {
        if (entityType == null || entityId == null) {
            return null;
        }

        return switch (entityType) {
            case GROUP_BUY -> "/group-purchases/" + entityId;
            case POST -> "/posts/" + entityId;
            case COMMENT -> "/comments/" + entityId;
            case REVIEW -> "/reviews/" + entityId;
        };
    }
}
