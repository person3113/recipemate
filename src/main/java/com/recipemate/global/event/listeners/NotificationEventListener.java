package com.recipemate.global.event.listeners;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final GroupBuyRepository groupBuyRepository;
    private final ParticipationRepository participationRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
            log.debug("공구 참여 알림 생성 완료: groupBuyId={}, userId={}", event.getGroupBuyId(), event.getUserId());
        } else {
            log.warn("공구를 찾을 수 없어 알림 생성 실패: groupBuyId={}", event.getGroupBuyId());
        }
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleParticipationCancelledEvent(ParticipationCancelledEvent event) {
        GroupBuy groupBuy = groupBuyRepository.findById(event.getGroupBuyId()).orElse(null);
        if (groupBuy != null) {
            notificationService.createNotification(
                    groupBuy.getHost().getId(),
                    NotificationType.CANCEL_PARTICIPATION,
                    event.getUserId(),
                    event.getGroupBuyId(),
                    EntityType.GROUP_BUY
            );
            log.debug("공구 참여 취소 알림 생성 완료: groupBuyId={}, userId={}", event.getGroupBuyId(), event.getUserId());
        } else {
            log.warn("공구를 찾을 수 없어 알림 생성 실패: groupBuyId={}", event.getGroupBuyId());
        }
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        notificationService.createNotification(
                event.getHostId(),
                NotificationType.REVIEW_GROUP_BUY,
                event.getReviewerId(),
                event.getReview().getId(),
                EntityType.REVIEW
        );
        log.debug("공구 후기 알림 생성 완료: reviewId={}, hostId={}", event.getReview().getId(), event.getHostId());
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        Comment comment = event.getComment();
        Long authorId = comment.getAuthor().getId();

        // 대댓글 알림 - 부모 댓글이 속한 게시글/공구로 이동
        if (comment.getParent() != null) {
            Long relatedEntityId;
            EntityType relatedEntityType;
            
            if (comment.getGroupBuy() != null) {
                relatedEntityId = comment.getGroupBuy().getId();
                relatedEntityType = EntityType.GROUP_BUY;
            } else if (comment.getPost() != null) {
                relatedEntityId = comment.getPost().getId();
                relatedEntityType = EntityType.POST;
            } else {
                log.warn("대댓글의 연결된 게시글/공구를 찾을 수 없음: commentId={}", comment.getId());
                return;
            }
            
            notificationService.createNotification(
                    comment.getParent().getAuthor().getId(),
                    NotificationType.REPLY_COMMENT,
                    authorId,
                    relatedEntityId,
                    relatedEntityType
            );
            log.debug("대댓글 알림 생성 완료: commentId={}, parentAuthorId={}, entityType={}, entityId={}", 
                    comment.getId(), comment.getParent().getAuthor().getId(), relatedEntityType, relatedEntityId);
            return; // 대댓글은 원댓글 작성자에게만 알림
        }

        // 공구 댓글 알림
        if (comment.getGroupBuy() != null) {
            notificationService.createNotification(
                    comment.getGroupBuy().getHost().getId(),
                    NotificationType.COMMENT_GROUP_BUY,
                    authorId,
                    comment.getGroupBuy().getId(),
                    EntityType.GROUP_BUY
            );
            log.debug("공구 댓글 알림 생성 완료: commentId={}, groupBuyId={}", comment.getId(), comment.getGroupBuy().getId());
        }

        // 게시글 댓글 알림
        if (comment.getPost() != null) {
            notificationService.createNotification(
                    comment.getPost().getAuthor().getId(),
                    NotificationType.COMMENT_POST,
                    authorId,
                    comment.getPost().getId(),
                    EntityType.POST
            );
            log.debug("게시글 댓글 알림 생성 완료: commentId={}, postId={}", comment.getId(), comment.getPost().getId());
        }
    }

    /**
     * 공구 목표 달성 이벤트 처리
     * 참여자 전원에게 알림 발송
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGroupBuyCompletedEvent(GroupBuyCompletedEvent event) {
        GroupBuy groupBuy = groupBuyRepository.findById(event.getGroupBuyId()).orElse(null);
        if (groupBuy != null) {
            List<Participation> participations = participationRepository.findByGroupBuyId(event.getGroupBuyId());
            
            for (Participation participation : participations) {
                notificationService.createNotification(
                        participation.getUser().getId(),
                        NotificationType.GROUP_BUY_COMPLETED,
                        null, // 시스템 알림이므로 actor는 null
                        event.getGroupBuyId(),
                        EntityType.GROUP_BUY
                );
            }
            log.info("공구 목표 달성 알림 발송 완료: groupBuyId={}, 참여자 수={}", event.getGroupBuyId(), participations.size());
        } else {
            log.warn("공구를 찾을 수 없어 목표 달성 알림 생성 실패: groupBuyId={}", event.getGroupBuyId());
        }
    }

    /**
     * 공구 마감 이벤트 처리
     * 참여자 전원에게 알림 발송
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGroupBuyDeadlineEvent(GroupBuyDeadlineEvent event) {
        GroupBuy groupBuy = groupBuyRepository.findById(event.getGroupBuyId()).orElse(null);
        if (groupBuy != null) {
            List<Participation> participations = participationRepository.findByGroupBuyId(event.getGroupBuyId());
            
            for (Participation participation : participations) {
                notificationService.createNotification(
                        participation.getUser().getId(),
                        NotificationType.GROUP_BUY_DEADLINE,
                        null, // 시스템 알림이므로 actor는 null
                        event.getGroupBuyId(),
                        EntityType.GROUP_BUY
                );
            }
            log.info("공구 마감 임박 알림 발송 완료: groupBuyId={}, 참여자 수={}", event.getGroupBuyId(), participations.size());
        } else {
            log.warn("공구를 찾을 수 없어 마감 알림 생성 실패: groupBuyId={}", event.getGroupBuyId());
        }
    }
}
