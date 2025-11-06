package com.recipemate.domain.review.service;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.review.dto.CreateReviewRequest;
import com.recipemate.domain.review.dto.ReviewResponse;
import com.recipemate.domain.review.dto.UpdateReviewRequest;
import com.recipemate.domain.review.entity.Review;
import com.recipemate.domain.review.repository.ReviewRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.event.ReviewCreatedEvent;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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

        // 7. 후기 생성 관련 이벤트 발행 (매너온도, 알림, 포인트, 뱃지 등)
        eventPublisher.publishEvent(new ReviewCreatedEvent(savedReview));

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
}
