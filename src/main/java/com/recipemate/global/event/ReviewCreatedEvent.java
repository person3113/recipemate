package com.recipemate.global.event;

import com.recipemate.domain.review.entity.Review;
import lombok.Getter;

@Getter
public class ReviewCreatedEvent {
    private final Long reviewerId;
    private final Long hostId;
    private final Review review;

    public ReviewCreatedEvent(Review review) {
        this.review = review;
        this.reviewerId = review.getReviewer().getId();
        this.hostId = review.getGroupBuy().getHost().getId();
    }
}
