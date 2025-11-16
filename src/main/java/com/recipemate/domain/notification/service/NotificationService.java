package com.recipemate.domain.notification.service;

import com.recipemate.domain.notification.dto.NotificationResponse;
import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.domain.notification.repository.NotificationRepository;
import com.recipemate.domain.recipe.entity.RecipeCorrection;
import com.recipemate.domain.recipe.repository.RecipeCorrectionRepository;
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
    private final RecipeCorrectionRepository recipeCorrectionRepository;

    /**
     * 알림 생성
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

        // 사용자 알림 설정 확인
        if (!shouldCreateNotification(recipient, type)) {
            return;
        }

        // 행동자 조회 (시스템 알림의 경우 null)
        User actor = null;
        if (actorId != null) {
            actor = userRepository.findById(actorId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        // 알림 내용 생성
        String content = generateNotificationContent(type, actor);

        // URL 생성
        String url = generateNotificationUrl(type, relatedEntityType, relatedEntityId);

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
     * 알림 타입과 사용자 설정에 따라 알림 생성 여부 결정
     */
    private boolean shouldCreateNotification(User recipient, NotificationType type) {
        return switch (type) {
            // 댓글 관련 알림 - commentNotification 설정 확인
            case COMMENT_POST, REPLY_COMMENT, COMMENT_GROUP_BUY -> recipient.getCommentNotification();
            
            // 공구 관련 알림 - groupPurchaseNotification 설정 확인
            case JOIN_GROUP_BUY, CANCEL_PARTICIPATION, GROUP_BUY_DEADLINE, 
                 GROUP_BUY_COMPLETED, REVIEW_GROUP_BUY -> recipient.getGroupPurchaseNotification();
            
            // 쪽지, 레시피 제안 처리 알림은 항상 생성
            case DIRECT_MESSAGE, RECIPE_CORRECTION_APPROVED, RECIPE_CORRECTION_REJECTED -> true;
        };
    }

    /**
     * 알림 목록 조회
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
     * 전체 알림 읽음 처리
     */
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        
        notifications.forEach(Notification::markAsRead);
    }

    /**
     * 개별 알림 삭제 (소프트 삭제)
     */
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 권한 확인
        if (!notification.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 소프트 삭제
        notification.softDelete();
    }

    /**
     * 전체 알림 삭제 (소프트 삭제)
     */
    public void deleteAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findActiveNotificationsByUserId(userId);
        notifications.forEach(Notification::softDelete);
    }

    /**
     * 읽지 않은 알림 개수 조회
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
            case DIRECT_MESSAGE -> actorName + "님이 쪽지를 보냈습니다.";
            case RECIPE_CORRECTION_APPROVED -> "레시피 개선 제안이 승인되었습니다.";
            case RECIPE_CORRECTION_REJECTED -> "레시피 개선 제안이 기각되었습니다.";
        };
    }

    /**
     * 알림 타입과 엔티티 타입에 따른 URL 생성
     */
    private String generateNotificationUrl(NotificationType notificationType, EntityType entityType, Long entityId) {
        if (entityId == null) {
            return null;
        }

        // RECIPE_CORRECTION 타입은 특별 처리: correction ID -> recipe API ID로 변환
        if (notificationType == NotificationType.RECIPE_CORRECTION_APPROVED || 
            notificationType == NotificationType.RECIPE_CORRECTION_REJECTED) {
            
            RecipeCorrection correction = recipeCorrectionRepository.findById(entityId)
                    .orElse(null);
            
            if (correction != null && correction.getRecipe() != null) {
                String recipeApiId = correction.getRecipe().getApiId();
                return "/recipes/" + recipeApiId;
            }
            return null;
        }

        // 일반적인 엔티티 타입별 URL 생성
        if (entityType == null) {
            return null;
        }

        return switch (entityType) {
            case GROUP_BUY -> "/group-purchases/" + entityId;
            case POST -> "/community-posts/" + entityId;
            case COMMENT -> "/comments/" + entityId;
            case REVIEW -> "/reviews/" + entityId;
            case DIRECT_MESSAGE -> "/direct-messages/conversation/" + entityId;
            case RECIPE -> "/recipes/" + entityId;
        };
    }
}
