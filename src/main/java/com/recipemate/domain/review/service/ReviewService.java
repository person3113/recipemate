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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
     * - COMPLETED 상태의 공구만 작성 가능
     * - 중복 작성 방지
     * - 삭제된 후기가 있으면 재작성 불가
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

        // 5. 삭제된 후기 존재 확인 (한 번 삭제하면 다시 작성 불가)
        if (reviewRepository.existsDeletedReviewByReviewerIdAndGroupBuyId(userId, groupBuyId)) {
            throw new CustomException(ErrorCode.REVIEW_DELETED_CANNOT_REWRITE);
        }

        // 6. 후기 생성 (Review.create() 내부에서 COMPLETED 상태 검증)
        Review review = Review.create(
                user,
                groupBuy,
                request.getRating(),
                request.getContent()
        );

        // 7. 후기 저장
        Review savedReview = reviewRepository.save(review);

        // 8. 후기 생성 관련 이벤트 발행 (매너온도, 알림, 포인트, 뱃지 등)
        ReviewCreatedEvent event = new ReviewCreatedEvent(savedReview);
        log.info("ReviewCreatedEvent 발행 - ReviewId: {}, HostId: {}, Rating: {}", 
            savedReview.getId(), event.getHostId(), savedReview.getRating());
        eventPublisher.publishEvent(event);

        return ReviewResponse.from(savedReview);
    }

    /**
     * 후기 수정
     * - 작성자만 수정 가능
     * - 별점 변경 시 매너온도 재계산
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

        // 4. 후기 수정 (기존 별점 반환)
        Integer oldRating = review.update(request.getRating(), request.getContent());

        // 5. 별점이 변경된 경우 매너온도 재계산 이벤트 발행
        if (request.getRating() != null && !oldRating.equals(request.getRating())) {
            eventPublisher.publishEvent(new com.recipemate.global.event.ReviewUpdatedEvent(review, oldRating));
        }

        return ReviewResponse.from(review);
    }

    /**
     * 후기 삭제 (소프트 삭제)
     * - 작성자만 삭제 가능
     * - 매너온도 원상복귀
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

        // 4. 후기 삭제 이벤트 발행 (매너온도 원상복귀)
        eventPublisher.publishEvent(new com.recipemate.global.event.ReviewDeletedEvent(review));
        log.info("ReviewDeletedEvent 발행 - ReviewId: {}, HostId: {}, Rating: {}", 
            review.getId(), review.getGroupBuy().getHost().getId(), review.getRating());

        // 5. 후기 소프트 삭제
        review.softDelete();
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
     * 특정 공구의 후기 목록을 페이징하여 조회 (평점 분포 포함)
     */
    public Page<ReviewResponse> getReviewsByGroupBuyPaged(Long groupBuyId, Pageable pageable) {
        // 1. 공구 존재 여부 확인
        if (!groupBuyRepository.existsById(groupBuyId)) {
            throw new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND);
        }

        // 2. 후기 목록 조회 (페이징)
        Page<Review> reviews = reviewRepository.findByGroupBuyIdWithDetails(groupBuyId, pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 전체 후기 목록을 페이징하여 조회
     */
    public Page<ReviewResponse> getAllReviewsPaged(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllWithDetails(pageable);
        return reviews.map(ReviewResponse::from);
    }

    /**
     * 특정 공구의 평점 분포 계산
     * @return Map<평점(1-5), 해당 평점의 후기 개수>
     */
    public Map<Integer, Long> getRatingDistribution(Long groupBuyId) {
        // 1. 공구 존재 여부 확인
        if (!groupBuyRepository.existsById(groupBuyId)) {
            throw new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND);
        }

        // 2. 평점별 후기 개수 조회
        List<Object[]> results = reviewRepository.countByRatingForGroupBuy(groupBuyId);

        // 3. Map으로 변환 (1~5점 모두 초기화)
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        // 4. 실제 데이터 채우기
        for (Object[] result : results) {
            Integer rating = (Integer) result[0];
            Long count = (Long) result[1];
            distribution.put(rating, count);
        }

        return distribution;
    }

    /**
     * 특정 공구의 평균 평점 조회
     */
    public Double getAverageRating(Long groupBuyId) {
        // 1. 공구 존재 여부 확인
        if (!groupBuyRepository.existsById(groupBuyId)) {
            throw new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND);
        }

        // 2. 평균 평점 조회 (null이면 0.0 반환)
        Double averageRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuyId);
        return averageRating != null ? averageRating : 0.0;
    }
}
