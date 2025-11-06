package com.recipemate.global.event.listeners;

import com.recipemate.global.event.GroupBuyCreatedEvent;
import com.recipemate.global.event.ParticipationCreatedEvent;
import com.recipemate.global.event.ReviewCreatedEvent;
import com.recipemate.domain.user.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    @TransactionalEventListener
    public void handleGroupBuyCreatedEvent(GroupBuyCreatedEvent event) {
        pointService.earnPoints(event.getUserId(), 50, "공동구매 생성");
    }

    @TransactionalEventListener
    public void handleParticipationCreatedEvent(ParticipationCreatedEvent event) {
        pointService.earnPoints(event.getUserId(), 10, "공동구매 참여");
    }

    @TransactionalEventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        pointService.earnPoints(event.getReviewerId(), 20, "후기 작성");
    }
}
