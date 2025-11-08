package com.recipemate.global.event.listeners;

import com.recipemate.global.event.CommentCreatedEvent;
import com.recipemate.global.event.GroupBuyCreatedEvent;
import com.recipemate.global.event.ParticipationCreatedEvent;
import com.recipemate.global.event.ReviewCreatedEvent;
import com.recipemate.domain.user.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGroupBuyCreatedEvent(GroupBuyCreatedEvent event) {
        pointService.earnPoints(event.getUserId(), 100, "공동구매 생성");
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleParticipationCreatedEvent(ParticipationCreatedEvent event) {
        pointService.earnPoints(event.getUserId(), 50, "공동구매 참여");
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        pointService.earnPoints(event.getReviewerId(), 30, "후기 작성");
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        pointService.earnPoints(event.getComment().getAuthor().getId(), 10, "댓글 작성");
    }
}
