package com.recipemate.domain.review.service;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.review.dto.CreateReviewRequest;
import com.recipemate.domain.review.dto.ReviewResponse;
import com.recipemate.domain.review.dto.UpdateReviewRequest;
import com.recipemate.domain.review.entity.Review;
import com.recipemate.domain.review.repository.ReviewRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final ParticipationRepository participationRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final com.recipemate.domain.badge.service.BadgeService badgeService;
    private final com.recipemate.domain.user.service.PointService pointService;

    /**
     * 후기 작성
     * - 참여자만 작성 가능
     * - CLOSED 상태의 공구만 작성 가능
     * - 중복 작성 방지
     * - 매너온도 반영
     */
    @Transactional
    public ReviewResponse createReview(Long userId, Long groupBuyId, CreateReviewRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 공구 조회 (주최자 정보 포함)
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 3. 참여자 여부 확인
        if (!participationRepository.existsByUserIdAndGroupBuyId(userId, groupBuyId)) {
            throw new CustomException(ErrorCode.NOT_PARTICIPATED);
        }

        // 4. 중복 후기 확인
        if (reviewRepository.existsByReviewerIdAndGroupBuyId(userId, groupBuyId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 5. 후기 생성 (Review.create() 내부에서 CLOSED 상태 검증)
        Review review = Review.create(
                user,
                groupBuy,
                request.getRating(),
                request.getContent()
        );

        // 6. 후기 저장
        Review savedReview = reviewRepository.save(review);

        // 7. 매너온도 반영
        double delta = savedReview.calculateMannerTemperatureDelta();
        userService.updateMannerTemperature(groupBuy.getHost().getId(), delta);

        // 8. 공구 주최자에게 후기 알림 전송
        notificationService.createNotification(
            groupBuy.getHost().getId(),
            NotificationType.REVIEW_GROUP_BUY,
            userId,
            savedReview.getId(),
            EntityType.REVIEW
        );

        // 9. 후기 작성 포인트 적립 (+20)
        pointService.earnPoints(userId, 20, "후기 작성");

        // 10. 후기 작성자에게 REVIEWER 배지 확인 및 수여
        checkAndAwardReviewerBadge(userId);

        // 11. 공구 주최자에게 POPULAR_HOST 배지 확인 및 수여
        checkAndAwardPopularHostBadge(groupBuy.getHost().getId());

        return ReviewResponse.from(savedReview);
    }

    /**
     * 후기 수정
     * - 작성자만 수정 가능
     */
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 3. 작성자 권한 확인
        if (!review.isReviewedBy(user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 4. 후기 수정
        review.update(request.getRating(), request.getContent());

        return ReviewResponse.from(review);
    }

    /**
     * 후기 삭제
     * - 작성자만 삭제 가능
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 3. 작성자 권한 확인
        if (!review.isReviewedBy(user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 4. 후기 삭제
        reviewRepository.delete(review);
    }

    /**
     * 특정 공구의 후기 목록 조회
     */
    public List<ReviewResponse> getReviewsByGroupBuy(Long groupBuyId) {
        // 1. 공구 존재 여부 확인
        if (!groupBuyRepository.existsById(groupBuyId)) {
            throw new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND);
        }

        // 2. 후기 목록 조회
        List<Review> reviews = reviewRepository.findByGroupBuyIdOrderByCreatedAtDesc(groupBuyId);

        return reviews.stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * REVIEWER 배지 확인 및 수여 (5개 이상 후기 작성)
     */
    private void checkAndAwardReviewerBadge(Long userId) {
        long count = reviewRepository.countByReviewerId(userId);
        if (count >= 5) {
            badgeService.checkAndAwardBadge(userId, com.recipemate.global.common.BadgeType.REVIEWER);
        }
    }

    /**
     * POPULAR_HOST 배지 확인 및 수여 (10개 이상 후기 & 평균 평점 4.5 이상)
     */
    private void checkAndAwardPopularHostBadge(Long hostId) {
        List<Review> hostReviews = reviewRepository.findByGroupBuyHostId(hostId);
        
        if (hostReviews.size() >= 10) {
            double avgRating = hostReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            
            if (avgRating >= 4.5) {
                badgeService.checkAndAwardBadge(hostId, com.recipemate.global.common.BadgeType.POPULAR_HOST);
            }
        }
    }
}
