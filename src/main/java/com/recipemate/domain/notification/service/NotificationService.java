package com.recipemate.domain.notification.service;

import com.recipemate.domain.notification.dto.NotificationResponse;
import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.domain.notification.repository.NotificationRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     * 알림 목록 조회
     *
     * @param userId 사용자 ID
     * @param isRead 읽음 여부 (null이면 전체 조회)
     * @return 알림 목록
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId, Boolean isRead) {
        List<Notification> notifications;
        
        if (isRead == null) {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        }
        
        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 알림 목록 조회 with 페이징
     *
     * @param userId 사용자 ID
     * @param isRead 읽음 여부 (null이면 전체 조회)
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Boolean isRead, Pageable pageable) {
        Page<Notification> notifications;
        
        if (isRead == null) {
            notifications = notificationRepository.findByUserIdWithActor(userId, pageable);
        } else {
            notifications = notificationRepository.findByUserIdAndIsReadWithActor(userId, isRead, pageable);
        }
        
        return notifications.map(NotificationResponse::from);
    }

    /**
     * 알림 읽음 처리
     *
     * @param userId         사용자 ID
     * @param notificationId 알림 ID
     */
    public void markNotificationAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        notification.markAsRead();
    }

    /**
     * 전체 알림 삭제
     *
     * @param userId 사용자 ID
     */
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    /**
     * 읽지 않은 알림 개수 조회
     *
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
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
            case RECIPE -> "/recipes/" + entityId;
        };
    }
}
