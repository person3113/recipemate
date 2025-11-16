package com.recipemate.global.event.listeners;

import com.recipemate.domain.review.entity.Review;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.event.ReviewCreatedEvent;
import com.recipemate.global.event.ReviewDeletedEvent;
import com.recipemate.global.event.ReviewUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MannerTemperatureEventListener {

    private final UserService userService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        Review review = event.getReview();
        double delta = review.calculateMannerTemperatureDelta();
        String reason = String.format("후기 작성 (%d점)", review.getRating());
        
        log.info("ReviewCreatedEvent 수신 - ReviewId: {}, HostId: {}, Delta: {}, Reason: {}", 
            review.getId(), event.getHostId(), delta, reason);
        
        userService.updateMannerTemperature(event.getHostId(), delta, reason, review.getId());
        
        log.info("매너온도 업데이트 완료 - ReviewId: {}", review.getId());
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewUpdatedEvent(ReviewUpdatedEvent event) {
        Review review = event.getReview();
        
        // 기존 별점의 온도 변화량 계산 (되돌리기)
        double oldDelta = calculateDeltaByRating(event.getOldRating());
        
        // 새 별점의 온도 변화량 계산
        double newDelta = review.calculateMannerTemperatureDelta();
        
        // 차이만큼 반영 (기존 온도 되돌리고 새 온도 적용)
        double totalDelta = newDelta - oldDelta;
        
        String reason = String.format("후기 수정 (%d점 → %d점)", event.getOldRating(), event.getNewRating());
        
        log.info("ReviewUpdatedEvent 수신 - ReviewId: {}, HostId: {}, TotalDelta: {}, Reason: {}", 
            review.getId(), event.getHostId(), totalDelta, reason);
        
        userService.updateMannerTemperature(event.getHostId(), totalDelta, reason, review.getId());
        
        log.info("매너온도 업데이트 완료 - ReviewId: {}", review.getId());
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewDeletedEvent(ReviewDeletedEvent event) {
        // 기존 별점의 반대 효과를 적용하여 원상복귀
        double delta = -calculateDeltaByRating(event.getRating());
        String reason = String.format("후기 삭제 (%d점 반영 취소)", event.getRating());
        
        log.info("ReviewDeletedEvent 수신 - ReviewId: {}, HostId: {}, Delta: {}, Reason: {}", 
            event.getReviewId(), event.getHostId(), delta, reason);
        
        userService.updateMannerTemperature(event.getHostId(), delta, reason, event.getReviewId());
        
        log.info("매너온도 원상복귀 완료 - ReviewId: {}", event.getReviewId());
    }

    private double calculateDeltaByRating(Integer rating) {
        return switch (rating) {
            case 5 -> 0.5;
            case 4 -> 0.2;
            case 3 -> 0.0;
            case 2 -> -0.2;
            case 1 -> -0.5;
            default -> 0.0;
        };
    }
}

