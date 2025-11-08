//package com.recipemate.domain.review;
//
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.review.entity.Review;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import com.recipemate.global.exception.CustomException;
//import com.recipemate.global.exception.ErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.*;
//
//class ReviewTest {
//
//    private User reviewer;
//    private User host;
//    private GroupBuy groupBuy;
//
//    @BeforeEach
//    void setUp() {
//        reviewer = User.create(
//                "reviewer@example.com",
//                "encodedPassword",
//                "리뷰어",
//                "010-1234-5678"
//        );
//
//        host = User.create(
//                "host@example.com",
//                "encodedPassword",
//                "주최자",
//                "010-9999-9999"
//        );
//
//        groupBuy = GroupBuy.createGeneral(
//                host,
//                "까르보나라 재료 공구",
//                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
//                "식재료",
//                60000,
//                5,
//                LocalDateTime.now().plusDays(7),
//                DeliveryMethod.BOTH,
//                "서울대학교 정문",
//                3000,
//                true
//        );
//    }
//
//    @Test
//    @DisplayName("후기 생성 성공")
//    void createReview() {
//        // given
//        groupBuy.close();
//        Integer rating = 5;
//        String content = "정말 좋은 공구였습니다!";
//
//        // when
//        Review review = Review.create(reviewer, groupBuy, rating, content);
//
//        // then
//        assertThat(review).isNotNull();
//        assertThat(review.getReviewer()).isEqualTo(reviewer);
//        assertThat(review.getGroupBuy()).isEqualTo(groupBuy);
//        assertThat(review.getRating()).isEqualTo(rating);
//        assertThat(review.getContent()).isEqualTo(content);
//    }
//
//    @Test
//    @DisplayName("후기 생성 시 내용은 null일 수 있다")
//    void createReviewWithoutContent() {
//        // given
//        groupBuy.close();
//        Integer rating = 5;
//
//        // when
//        Review review = Review.create(reviewer, groupBuy, rating, null);
//
//        // then
//        assertThat(review).isNotNull();
//        assertThat(review.getContent()).isNull();
//    }
//
//    @Test
//    @DisplayName("별점이 1 미만이면 예외 발생")
//    void createReviewWithRatingBelowOne() {
//        // given
//        groupBuy.close();
//
//        // when & then
//        assertThatThrownBy(() -> Review.create(reviewer, groupBuy, 0, "후기 내용"))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING);
//    }
//
//    @Test
//    @DisplayName("별점이 5 초과이면 예외 발생")
//    void createReviewWithRatingAboveFive() {
//        // given
//        groupBuy.close();
//
//        // when & then
//        assertThatThrownBy(() -> Review.create(reviewer, groupBuy, 6, "후기 내용"))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING);
//    }
//
//    @Test
//    @DisplayName("별점이 null이면 예외 발생")
//    void createReviewWithNullRating() {
//        // given
//        groupBuy.close();
//
//        // when & then
//        assertThatThrownBy(() -> Review.create(reviewer, groupBuy, null, "후기 내용"))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING);
//    }
//
//    @Test
//    @DisplayName("완료되지 않은 공구에 후기 작성 시 예외 발생")
//    void createReviewOnNotClosedGroupBuy() {
//        // given - groupBuy는 RECRUITING 상태
//
//        // when & then
//        assertThatThrownBy(() -> Review.create(reviewer, groupBuy, 5, "후기 내용"))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_CLOSED);
//    }
//
//    @Test
//    @DisplayName("후기 수정 성공")
//    void updateReview() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 5, "좋았습니다");
//
//        // when
//        review.update(4, "생각보다 보통이었습니다");
//
//        // then
//        assertThat(review.getRating()).isEqualTo(4);
//        assertThat(review.getContent()).isEqualTo("생각보다 보통이었습니다");
//    }
//
//    @Test
//    @DisplayName("후기 수정 시 별점만 변경 가능")
//    void updateReviewRatingOnly() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 5, "좋았습니다");
//
//        // when
//        review.update(3, null);
//
//        // then
//        assertThat(review.getRating()).isEqualTo(3);
//        assertThat(review.getContent()).isEqualTo("좋았습니다"); // 기존 내용 유지
//    }
//
//    @Test
//    @DisplayName("후기 수정 시 내용만 변경 가능")
//    void updateReviewContentOnly() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 5, "좋았습니다");
//
//        // when
//        review.update(null, "정말 훌륭했습니다");
//
//        // then
//        assertThat(review.getRating()).isEqualTo(5); // 기존 별점 유지
//        assertThat(review.getContent()).isEqualTo("정말 훌륭했습니다");
//    }
//
//    @Test
//    @DisplayName("후기 수정 시 유효하지 않은 별점이면 예외 발생")
//    void updateReviewWithInvalidRating() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 5, "좋았습니다");
//
//        // when & then
//        assertThatThrownBy(() -> review.update(6, "수정 내용"))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING);
//    }
//
//    @Test
//    @DisplayName("별점이 5점일 때 매너온도 증가량은 +0.5")
//    void getMannerTemperatureDeltaForRating5() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 5, "완벽했습니다");
//
//        // when
//        double delta = review.calculateMannerTemperatureDelta();
//
//        // then
//        assertThat(delta).isEqualTo(0.5);
//    }
//
//    @Test
//    @DisplayName("별점이 4점일 때 매너온도 증가량은 +0.3")
//    void getMannerTemperatureDeltaForRating4() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 4, "좋았습니다");
//
//        // when
//        double delta = review.calculateMannerTemperatureDelta();
//
//        // then
//        assertThat(delta).isEqualTo(0.3);
//    }
//
//    @Test
//    @DisplayName("별점이 3점일 때 매너온도 변화량은 0")
//    void getMannerTemperatureDeltaForRating3() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 3, "보통이었습니다");
//
//        // when
//        double delta = review.calculateMannerTemperatureDelta();
//
//        // then
//        assertThat(delta).isEqualTo(0.0);
//    }
//
//    @Test
//    @DisplayName("별점이 2점일 때 매너온도 감소량은 -1.0")
//    void getMannerTemperatureDeltaForRating2() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 2, "별로였습니다");
//
//        // when
//        double delta = review.calculateMannerTemperatureDelta();
//
//        // then
//        assertThat(delta).isEqualTo(-1.0);
//    }
//
//    @Test
//    @DisplayName("별점이 1점일 때 매너온도 감소량은 -2.0")
//    void getMannerTemperatureDeltaForRating1() {
//        // given
//        groupBuy.close();
//        Review review = Review.create(reviewer, groupBuy, 1, "최악이었습니다");
//
//        // when
//        double delta = review.calculateMannerTemperatureDelta();
//
//        // then
//        assertThat(delta).isEqualTo(-2.0);
//    }
//}
