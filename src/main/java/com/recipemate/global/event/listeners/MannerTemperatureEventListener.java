package com.recipemate.global.event.listeners;

import com.recipemate.domain.review.entity.Review;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MannerTemperatureEventListener {

    private final UserService userService;

    @TransactionalEventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        Review review = event.getReview();
        double delta = review.calculateMannerTemperatureDelta();
        userService.updateMannerTemperature(event.getHostId(), delta);
    }
}
