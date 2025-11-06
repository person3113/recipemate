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
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.recipemate.domain.badge.service.BadgeService badgeService;

    @Mock
    private com.recipemate.domain.user.service.PointService pointService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private User reviewer;
    private User host;
    private GroupBuy closedGroupBuy;
    private Long reviewerId = 1L;
    private Long hostId = 2L;
    private Long groupBuyId = 1L;

    @BeforeEach
    void setUp() {
        reviewer = User.create(
            "reviewer@example.com",
            "encodedPassword",
            "리뷰어",
            "010-1234-5678"
        );
        setUserId(reviewer, reviewerId);

        host = User.create(
            "host@example.com",
            "encodedPassword",
            "주최자",
            "010-9999-9999"
        );
        setUserId(host, hostId);

        closedGroupBuy = GroupBuy.createGeneral(
            host,
            "삼겹살 공동구매",
            "맛있는 삼겹살",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "강남역 2번 출구",
            3000,
            true
        );
        setGroupBuyId(closedGroupBuy, groupBuyId);
        setGroupBuyStatus(closedGroupBuy, GroupBuyStatus.CLOSED);
    }

    // ========== 후기 작성 테스트 ==========

    @Test
    @DisplayName("후기 작성 성공 - 5점 별점 (+0.5 매너온도)")
    void createReview_Success_5Stars() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(5)
            .content("정말 좋은 공구였습니다!")
            .build();

        Review savedReview = Review.create(reviewer, closedGroupBuy, 5, "정말 좋은 공구였습니다!");
        setReviewId(savedReview, 1L);

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // when
        ReviewResponse response = reviewService.createReview(reviewerId, groupBuyId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getContent()).isEqualTo("정말 좋은 공구였습니다!");
        assertThat(response.getReviewerNickname()).isEqualTo("리뷰어");
        
        verify(userRepository).findById(reviewerId);
        verify(groupBuyRepository).findByIdWithHost(groupBuyId);
        verify(participationRepository).existsByUserIdAndGroupBuyId(reviewerId, groupBuyId);
        verify(reviewRepository).existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId);
        verify(reviewRepository).save(any(Review.class));
        verify(eventPublisher).publishEvent(any(com.recipemate.global.event.ReviewCreatedEvent.class));
    }

    @Test
    @DisplayName("후기 작성 성공 - 4점 별점 (+0.3 매너온도)")
    void createReview_Success_4Stars() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(4)
            .content("만족스러운 공구였어요")
            .build();

        Review savedReview = Review.create(reviewer, closedGroupBuy, 4, "만족스러운 공구였어요");
        setReviewId(savedReview, 1L);

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // when
        ReviewResponse response = reviewService.createReview(reviewerId, groupBuyId, request);

        // then
        assertThat(response.getRating()).isEqualTo(4);
        verify(eventPublisher).publishEvent(any(com.recipemate.global.event.ReviewCreatedEvent.class));
    }

    @Test
    @DisplayName("후기 작성 성공 - 3점 별점 (매너온도 변화 없음)")
    void createReview_Success_3Stars() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(3)
            .content("보통이었어요")
            .build();

        Review savedReview = Review.create(reviewer, closedGroupBuy, 3, "보통이었어요");
        setReviewId(savedReview, 1L);

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // when
        ReviewResponse response = reviewService.createReview(reviewerId, groupBuyId, request);

        // then
        assertThat(response.getRating()).isEqualTo(3);
        verify(eventPublisher).publishEvent(any(com.recipemate.global.event.ReviewCreatedEvent.class));
    }

    @Test
    @DisplayName("후기 작성 성공 - 2점 별점 (-1.0 매너온도)")
    void createReview_Success_2Stars() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(2)
            .content("아쉬운 점이 있었어요")
            .build();

        Review savedReview = Review.create(reviewer, closedGroupBuy, 2, "아쉬운 점이 있었어요");
        setReviewId(savedReview, 1L);

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // when
        ReviewResponse response = reviewService.createReview(reviewerId, groupBuyId, request);

        // then
        assertThat(response.getRating()).isEqualTo(2);
        verify(eventPublisher).publishEvent(any(com.recipemate.global.event.ReviewCreatedEvent.class));
    }

    @Test
    @DisplayName("후기 작성 성공 - 1점 별점 (-2.0 매너온도)")
    void createReview_Success_1Star() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(1)
            .content("매우 불만족스러웠어요")
            .build();

        Review savedReview = Review.create(reviewer, closedGroupBuy, 1, "매우 불만족스러웠어요");
        setReviewId(savedReview, 1L);

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);

        // when
        ReviewResponse response = reviewService.createReview(reviewerId, groupBuyId, request);

        // then
        assertThat(response.getRating()).isEqualTo(1);
        verify(eventPublisher).publishEvent(any(com.recipemate.global.event.ReviewCreatedEvent.class));
    }

    @Test
    @DisplayName("후기 작성 실패 - 존재하지 않는 사용자")
    void createReview_Fail_UserNotFound() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(5)
            .content("좋았어요")
            .build();

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(999L, groupBuyId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("후기 작성 실패 - 존재하지 않는 공구")
    void createReview_Fail_GroupBuyNotFound() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(5)
            .content("좋았어요")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, 999L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("후기 작성 실패 - 참여하지 않은 공구")
    void createReview_Fail_NotParticipated() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(5)
            .content("좋았어요")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, groupBuyId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_PARTICIPATED);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("후기 작성 실패 - 이미 후기 작성함 (중복 후기)")
    void createReview_Fail_AlreadyReviewed() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(5)
            .content("좋았어요")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, groupBuyId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_ALREADY_EXISTS);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("후기 작성 실패 - CLOSED 상태가 아닌 공구 (RECRUITING)")
    void createReview_Fail_GroupBuyNotClosed_Recruiting() {
        // given
        GroupBuy recruitingGroupBuy = GroupBuy.createGeneral(
            host,
            "모집 중인 공구",
            "아직 모집 중",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        setGroupBuyId(recruitingGroupBuy, 2L);
        setGroupBuyStatus(recruitingGroupBuy, GroupBuyStatus.RECRUITING);

        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(5)
            .content("좋았어요")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(2L)).willReturn(Optional.of(recruitingGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, 2L)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, 2L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, 2L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_CLOSED);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("후기 작성 실패 - 별점 범위 초과 (6점)")
    void createReview_Fail_InvalidRating_TooHigh() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(6)
            .content("좋았어요")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, groupBuyId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("후기 작성 실패 - 별점 범위 미만 (0점)")
    void createReview_Fail_InvalidRating_TooLow() {
        // given
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rating(0)
            .content("좋았어요")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(groupBuyRepository.findByIdWithHost(groupBuyId)).willReturn(Optional.of(closedGroupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(true);
        given(reviewRepository.existsByReviewerIdAndGroupBuyId(reviewerId, groupBuyId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, groupBuyId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING);
        
        verify(reviewRepository, never()).save(any(Review.class));
        verify(userService, never()).updateMannerTemperature(anyLong(), anyDouble());
    }

    // ========== 후기 수정 테스트 ==========

    @Test
    @DisplayName("후기 수정 성공 - 별점과 내용 모두 수정")
    void updateReview_Success() {
        // given
        Review existingReview = Review.create(reviewer, closedGroupBuy, 5, "좋았어요");
        setReviewId(existingReview, 1L);

        UpdateReviewRequest request = UpdateReviewRequest.builder()
            .rating(4)
            .content("수정된 후기입니다")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(reviewRepository.findById(1L)).willReturn(Optional.of(existingReview));

        // when
        ReviewResponse response = reviewService.updateReview(reviewerId, 1L, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getContent()).isEqualTo("수정된 후기입니다");
        
        verify(userRepository).findById(reviewerId);
        verify(reviewRepository).findById(1L);
    }

    @Test
    @DisplayName("후기 수정 성공 - 내용만 수정")
    void updateReview_Success_ContentOnly() {
        // given
        Review existingReview = Review.create(reviewer, closedGroupBuy, 5, "좋았어요");
        setReviewId(existingReview, 1L);

        UpdateReviewRequest request = UpdateReviewRequest.builder()
            .rating(null)
            .content("내용만 수정합니다")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(reviewRepository.findById(1L)).willReturn(Optional.of(existingReview));

        // when
        ReviewResponse response = reviewService.updateReview(reviewerId, 1L, request);

        // then
        assertThat(response.getRating()).isEqualTo(5); // 별점 유지
        assertThat(response.getContent()).isEqualTo("내용만 수정합니다");
    }

    @Test
    @DisplayName("후기 수정 실패 - 존재하지 않는 후기")
    void updateReview_Fail_ReviewNotFound() {
        // given
        UpdateReviewRequest request = UpdateReviewRequest.builder()
            .rating(4)
            .content("수정")
            .build();

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(reviewerId, 999L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("후기 수정 실패 - 작성자가 아닌 사용자")
    void updateReview_Fail_Unauthorized() {
        // given
        User otherUser = User.create(
            "other@example.com",
            "encodedPassword",
            "다른사용자",
            "010-8888-8888"
        );
        setUserId(otherUser, 3L);

        Review existingReview = Review.create(reviewer, closedGroupBuy, 5, "좋았어요");
        setReviewId(existingReview, 1L);

        UpdateReviewRequest request = UpdateReviewRequest.builder()
            .rating(4)
            .content("수정")
            .build();

        given(userRepository.findById(3L)).willReturn(Optional.of(otherUser));
        given(reviewRepository.findById(1L)).willReturn(Optional.of(existingReview));

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(3L, 1L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
    }

    // ========== 후기 삭제 테스트 ==========

    @Test
    @DisplayName("후기 삭제 성공")
    void deleteReview_Success() {
        // given
        Review existingReview = Review.create(reviewer, closedGroupBuy, 5, "좋았어요");
        setReviewId(existingReview, 1L);

        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(reviewRepository.findById(1L)).willReturn(Optional.of(existingReview));
        willDoNothing().given(reviewRepository).delete(existingReview);

        // when
        reviewService.deleteReview(reviewerId, 1L);

        // then
        verify(userRepository).findById(reviewerId);
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(existingReview);
    }

    @Test
    @DisplayName("후기 삭제 실패 - 존재하지 않는 후기")
    void deleteReview_Fail_ReviewNotFound() {
        // given
        given(userRepository.findById(reviewerId)).willReturn(Optional.of(reviewer));
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(reviewerId, 999L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("후기 삭제 실패 - 작성자가 아닌 사용자")
    void deleteReview_Fail_Unauthorized() {
        // given
        User otherUser = User.create(
            "other@example.com",
            "encodedPassword",
            "다른사용자",
            "010-8888-8888"
        );
        setUserId(otherUser, 3L);

        Review existingReview = Review.create(reviewer, closedGroupBuy, 5, "좋았어요");
        setReviewId(existingReview, 1L);

        given(userRepository.findById(3L)).willReturn(Optional.of(otherUser));
        given(reviewRepository.findById(1L)).willReturn(Optional.of(existingReview));

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(3L, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    // ========== 후기 목록 조회 테스트 ==========

    @Test
    @DisplayName("공구별 후기 목록 조회 성공")
    void getReviewsByGroupBuy_Success() {
        // given
        Review review1 = Review.create(reviewer, closedGroupBuy, 5, "좋았어요");
        setReviewId(review1, 1L);

        User reviewer2 = User.create(
            "reviewer2@example.com",
            "encodedPassword",
            "리뷰어2",
            "010-2222-2222"
        );
        setUserId(reviewer2, 3L);
        Review review2 = Review.create(reviewer2, closedGroupBuy, 4, "괜찮았어요");
        setReviewId(review2, 2L);

        List<Review> reviews = Arrays.asList(review1, review2);

        given(groupBuyRepository.existsById(groupBuyId)).willReturn(true);
        given(reviewRepository.findByGroupBuyIdOrderByCreatedAtDesc(groupBuyId)).willReturn(reviews);

        // when
        List<ReviewResponse> responses = reviewService.getReviewsByGroupBuy(groupBuyId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getRating()).isEqualTo(5);
        assertThat(responses.get(0).getReviewerNickname()).isEqualTo("리뷰어");
        assertThat(responses.get(1).getRating()).isEqualTo(4);
        assertThat(responses.get(1).getReviewerNickname()).isEqualTo("리뷰어2");
        
        verify(groupBuyRepository).existsById(groupBuyId);
        verify(reviewRepository).findByGroupBuyIdOrderByCreatedAtDesc(groupBuyId);
    }

    @Test
    @DisplayName("공구별 후기 목록 조회 실패 - 존재하지 않는 공구")
    void getReviewsByGroupBuy_Fail_GroupBuyNotFound() {
        // given
        given(groupBuyRepository.existsById(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewsByGroupBuy(999L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
        
        verify(reviewRepository, never()).findByGroupBuyIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("공구별 후기 목록 조회 성공 - 후기가 없는 경우")
    void getReviewsByGroupBuy_Success_EmptyList() {
        // given
        given(groupBuyRepository.existsById(groupBuyId)).willReturn(true);
        given(reviewRepository.findByGroupBuyIdOrderByCreatedAtDesc(groupBuyId)).willReturn(Arrays.asList());

        // when
        List<ReviewResponse> responses = reviewService.getReviewsByGroupBuy(groupBuyId);

        // then
        assertThat(responses).isEmpty();
    }

    // ========== 유틸리티 메서드 ==========

    private void setUserId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setGroupBuyId(GroupBuy groupBuy, Long id) {
        try {
            Field idField = GroupBuy.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(groupBuy, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setGroupBuyStatus(GroupBuy groupBuy, GroupBuyStatus status) {
        try {
            Field statusField = GroupBuy.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(groupBuy, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setReviewId(Review review, Long id) {
        try {
            Field idField = Review.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(review, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
