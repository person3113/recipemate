package com.recipemate.global.event.listeners;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.event.CommentCreatedEvent;
import com.recipemate.global.event.ParticipationCancelledEvent;
import com.recipemate.global.event.ParticipationCreatedEvent;
import com.recipemate.global.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final GroupBuyRepository groupBuyRepository;

    @TransactionalEventListener
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

    @TransactionalEventListener
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
        }
    }

    @TransactionalEventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        notificationService.createNotification(
                event.getHostId(),
                NotificationType.REVIEW_GROUP_BUY,
                event.getReviewerId(),
                event.getReview().getId(),
                EntityType.REVIEW
        );
    }

    @TransactionalEventListener
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        Comment comment = event.getComment();
        Long authorId = comment.getAuthor().getId();

        // 대댓글 알림
        if (comment.getParent() != null) {
            notificationService.createNotification(
                    comment.getParent().getAuthor().getId(),
                    NotificationType.REPLY_COMMENT,
                    authorId,
                    comment.getId(),
                    EntityType.COMMENT
            );
            return; // 대댓글은 원댓글 작성자에게만 알림
        }

        // 공구 댓글 알림
        if (comment.getGroupBuy() != null) {
            notificationService.createNotification(
                    comment.getGroupBuy().getHost().getId(),
                    NotificationType.COMMENT_GROUP_BUY,
                    authorId,
                    comment.getId(),
                    EntityType.COMMENT
            );
        }

        // 게시글 댓글 알림
        if (comment.getPost() != null) {
            notificationService.createNotification(
                    comment.getPost().getAuthor().getId(),
                    NotificationType.COMMENT_POST,
                    authorId,
                    comment.getId(),
                    EntityType.COMMENT
            );
        }
    }
}
