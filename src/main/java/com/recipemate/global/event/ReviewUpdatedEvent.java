package com.recipemate.global.event;

import com.recipemate.domain.review.entity.Review;
import lombok.Getter;

@Getter
public class ReviewUpdatedEvent {
    private final Long reviewerId;
    private final Long hostId;
    private final Review review;
    private final Integer oldRating;
    private final Integer newRating;

    public ReviewUpdatedEvent(Review review, Integer oldRating) {
        this.review = review;
        this.reviewerId = review.getReviewer().getId();
        this.hostId = review.getGroupBuy().getHost().getId();
        this.oldRating = oldRating;
        this.newRating = review.getRating();
    }
}
