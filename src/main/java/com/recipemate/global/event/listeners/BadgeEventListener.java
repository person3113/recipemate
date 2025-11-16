package com.recipemate.global.event.listeners;

import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.domain.badge.service.BadgeService;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.review.entity.Review;
import com.recipemate.domain.review.repository.ReviewRepository;
import com.recipemate.global.common.BadgeType;
import com.recipemate.global.event.GroupBuyCreatedEvent;
import com.recipemate.global.event.ParticipationCreatedEvent;
import com.recipemate.global.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BadgeEventListener {

    private final BadgeService badgeService;
    private final GroupBuyRepository groupBuyRepository;
    private final ParticipationRepository participationRepository;
    private final ReviewRepository reviewRepository;

    @TransactionalEventListener
    public void handleGroupBuyCreatedEvent(GroupBuyCreatedEvent event) {
        long groupBuyCount = groupBuyRepository.countByHostIdAndStatus(event.getUserId(), GroupBuyStatus.RECRUITING);
        if (groupBuyCount == 1) {
            badgeService.checkAndAwardBadge(event.getUserId(), BadgeType.FIRST_GROUP_BUY);
        }
    }

    @TransactionalEventListener
    public void handleParticipationCreatedEvent(ParticipationCreatedEvent event) {
        long count = participationRepository.countByUserId(event.getUserId());
        if (count >= 10) {
            badgeService.checkAndAwardBadge(event.getUserId(), BadgeType.TEN_PARTICIPATIONS);
        }
    }

    @TransactionalEventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        // 후기 작성자에게 REVIEWER 배지 확인 및 수여
        long reviewCount = reviewRepository.countByReviewerId(event.getReviewerId());
        if (reviewCount >= 5) {
            badgeService.checkAndAwardBadge(event.getReviewerId(), BadgeType.REVIEWER);
        }

        // 공구 주최자에게 POPULAR_HOST 배지 확인 및 수여
        List<Review> hostReviews = reviewRepository.findByGroupBuyHostId(event.getHostId());
        if (hostReviews.size() >= 10) {
            double avgRating = hostReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            if (avgRating >= 4.5) {
                badgeService.checkAndAwardBadge(event.getHostId(), BadgeType.POPULAR_HOST);
            }
        }
    }
}
