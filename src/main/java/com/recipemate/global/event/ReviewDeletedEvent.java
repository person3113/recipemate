package com.recipemate.global.event;

import com.recipemate.domain.review.entity.Review;
import lombok.Getter;

@Getter
public class ReviewDeletedEvent {
    private final Long reviewId;
    private final Long hostId;
    private final Integer rating;

    public ReviewDeletedEvent(Review review) {
        this.reviewId = review.getId();
        this.hostId = review.getGroupBuy().getHost().getId();
        this.rating = review.getRating();
    }
}
