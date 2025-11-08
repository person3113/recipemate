//package com.recipemate.domain.review.repository;
//
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
//import com.recipemate.domain.review.entity.Review;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import com.recipemate.global.common.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(com.recipemate.global.config.QueryDslConfig.class)
//@ActiveProfiles("test")
//@DisplayName("ReviewRepository 통합 테스트")
//class ReviewRepositoryTest {
//
//    @Autowired
//    private ReviewRepository reviewRepository;
//
//    @Autowired
//    private GroupBuyRepository groupBuyRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User host;
//    private User reviewer1;
//    private User reviewer2;
//    private GroupBuy groupBuy;
//
//    @BeforeEach
//    void setUp() {
//        reviewRepository.deleteAll();
//        groupBuyRepository.deleteAll();
//        userRepository.deleteAll();
//
//        host = User.builder()
//                .email("host@example.com")
//                .password("password")
//                .nickname("host")
//                .phoneNumber("010-1111-1111")
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(host);
//
//        reviewer1 = User.builder()
//                .email("reviewer1@example.com")
//                .password("password")
//                .nickname("reviewer1")
//                .phoneNumber("010-2222-2222")
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(reviewer1);
//
//        reviewer2 = User.builder()
//                .email("reviewer2@example.com")
//                .password("password")
//                .nickname("reviewer2")
//                .phoneNumber("010-3333-3333")
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(reviewer2);
//
//        groupBuy = GroupBuy.builder()
//                .host(host)
//                .title("Test Group Buy")
//                .content("Test content")
//                .category("FOOD")
//                .totalPrice(10000)
//                .targetHeadcount(5)
//                .deadline(LocalDateTime.now().plusDays(3))
//                .deliveryMethod(DeliveryMethod.DIRECT)
//                .status(GroupBuyStatus.CLOSED)
//                .build();
//        groupBuyRepository.save(groupBuy);
//    }
//
//    @Test
//    @DisplayName("후기를 저장하고 ID로 조회할 수 있다")
//    void saveAndFindById() {
//        // given
//        Review review = Review.create(reviewer1, groupBuy, 5, "Great!");
//
//        // when
//        Review savedReview = reviewRepository.save(review);
//        Review foundReview = reviewRepository.findById(savedReview.getId()).orElse(null);
//
//        // then
//        assertThat(foundReview).isNotNull();
//        assertThat(foundReview.getId()).isEqualTo(savedReview.getId());
//        assertThat(foundReview.getReviewer().getNickname()).isEqualTo("reviewer1");
//        assertThat(foundReview.getGroupBuy().getTitle()).isEqualTo("Test Group Buy");
//        assertThat(foundReview.getRating()).isEqualTo(5);
//        assertThat(foundReview.getContent()).isEqualTo("Great!");
//    }
//
//    @Test
//    @DisplayName("특정 공구의 후기 목록을 최신순으로 조회한다 (idx_review_group_buy_id 인덱스 활용)")
//    void findByGroupBuyIdOrderByCreatedAtDesc() throws InterruptedException {
//        // given
//        Review review1 = Review.create(reviewer1, groupBuy, 5, "First review");
//        Review review2 = Review.create(reviewer2, groupBuy, 4, "Second review");
//        reviewRepository.save(review1);
//        Thread.sleep(10); // 시간 차이를 두기 위해
//        reviewRepository.save(review2);
//
//        // when
//        List<Review> reviews = reviewRepository.findByGroupBuyIdOrderByCreatedAtDesc(groupBuy.getId());
//
//        // then
//        assertThat(reviews).hasSize(2);
//        // 최신순이므로 review2가 먼저 나와야 함
//        assertThat(reviews.get(0).getContent()).isEqualTo("Second review");
//        assertThat(reviews.get(1).getContent()).isEqualTo("First review");
//    }
//
//    @Test
//    @DisplayName("특정 사용자가 특정 공구에 후기를 작성했는지 확인한다 (UNIQUE 제약 조건 활용)")
//    void existsByReviewerIdAndGroupBuyId() {
//        // given
//        Review review = Review.create(reviewer1, groupBuy, 5, "Great!");
//        reviewRepository.save(review);
//
//        // when
//        boolean exists = reviewRepository.existsByReviewerIdAndGroupBuyId(reviewer1.getId(), groupBuy.getId());
//        boolean notExists = reviewRepository.existsByReviewerIdAndGroupBuyId(reviewer2.getId(), groupBuy.getId());
//
//        // then
//        assertThat(exists).isTrue();
//        assertThat(notExists).isFalse();
//    }
//
//    @Test
//    @DisplayName("특정 사용자가 작성한 후기 목록을 최신순으로 조회한다 (idx_review_reviewer_id 인덱스 활용)")
//    void findByReviewerIdOrderByCreatedAtDesc() throws InterruptedException {
//        // given
//        GroupBuy anotherGroupBuy = GroupBuy.builder()
//                .host(host)
//                .title("Another Group Buy")
//                .content("Another content")
//                .category("FOOD")
//                .totalPrice(20000)
//                .targetHeadcount(3)
//                .deadline(LocalDateTime.now().plusDays(5))
//                .deliveryMethod(DeliveryMethod.PARCEL)
//                .status(GroupBuyStatus.CLOSED)
//                .build();
//        groupBuyRepository.save(anotherGroupBuy);
//
//        Review review1 = Review.create(reviewer1, groupBuy, 5, "Review 1");
//        Review review2 = Review.create(reviewer1, anotherGroupBuy, 4, "Review 2");
//        Review review3 = Review.create(reviewer2, groupBuy, 3, "Review by another user");
//        reviewRepository.save(review1);
//        Thread.sleep(10);
//        reviewRepository.save(review2);
//        reviewRepository.save(review3);
//
//        // when
//        List<Review> reviews = reviewRepository.findByReviewerIdOrderByCreatedAtDesc(reviewer1.getId());
//
//        // then
//        assertThat(reviews).hasSize(2);
//        assertThat(reviews).extracting(Review::getContent).containsExactly("Review 2", "Review 1");
//        assertThat(reviews).allMatch(r -> r.getReviewer().getId().equals(reviewer1.getId()));
//    }
//
//    @Test
//    @DisplayName("특정 공구의 평균 별점을 계산한다")
//    void findAverageRatingByGroupBuyId() {
//        // given
//        Review review1 = Review.create(reviewer1, groupBuy, 5, "Excellent!");
//        Review review2 = Review.create(reviewer2, groupBuy, 3, "Good");
//        reviewRepository.saveAll(List.of(review1, review2));
//
//        // when
//        Double avgRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuy.getId());
//
//        // then
//        assertThat(avgRating).isEqualTo(4.0); // (5 + 3) / 2 = 4.0
//    }
//
//    @Test
//    @DisplayName("후기가 없는 공구의 평균 별점은 null이다")
//    void findAverageRatingByGroupBuyIdWithNoReviews() {
//        // given
//        GroupBuy groupBuyWithoutReviews = GroupBuy.builder()
//                .host(host)
//                .title("No Reviews Group Buy")
//                .content("Content")
//                .category("FOOD")
//                .totalPrice(15000)
//                .targetHeadcount(4)
//                .deadline(LocalDateTime.now().plusDays(4))
//                .deliveryMethod(DeliveryMethod.BOTH)
//                .status(GroupBuyStatus.CLOSED)
//                .build();
//        groupBuyRepository.save(groupBuyWithoutReviews);
//
//        // when
//        Double avgRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuyWithoutReviews.getId());
//
//        // then
//        assertThat(avgRating).isNull();
//    }
//
//    @Test
//    @DisplayName("특정 공구의 후기 개수를 조회한다")
//    void countByGroupBuyId() {
//        // given
//        Review review1 = Review.create(reviewer1, groupBuy, 5, "Review 1");
//        Review review2 = Review.create(reviewer2, groupBuy, 4, "Review 2");
//        reviewRepository.saveAll(List.of(review1, review2));
//
//        // when
//        long count = reviewRepository.countByGroupBuyId(groupBuy.getId());
//
//        // then
//        assertThat(count).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("특정 사용자가 작성한 후기 개수를 조회한다")
//    void countByReviewerId() {
//        // given
//        GroupBuy anotherGroupBuy = GroupBuy.builder()
//                .host(host)
//                .title("Another Group Buy")
//                .content("Another content")
//                .category("FOOD")
//                .totalPrice(20000)
//                .targetHeadcount(3)
//                .deadline(LocalDateTime.now().plusDays(5))
//                .deliveryMethod(DeliveryMethod.PARCEL)
//                .status(GroupBuyStatus.CLOSED)
//                .build();
//        groupBuyRepository.save(anotherGroupBuy);
//
//        Review review1 = Review.create(reviewer1, groupBuy, 5, "Review 1");
//        Review review2 = Review.create(reviewer1, anotherGroupBuy, 4, "Review 2");
//        Review review3 = Review.create(reviewer2, groupBuy, 3, "Review by another user");
//        reviewRepository.saveAll(List.of(review1, review2, review3));
//
//        // when
//        long count = reviewRepository.countByReviewerId(reviewer1.getId());
//
//        // then
//        assertThat(count).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("특정 공구 주최자가 받은 모든 후기를 조회한다")
//    void findByGroupBuyHostId() {
//        // given
//        User anotherHost = User.builder()
//                .email("anotherhost@example.com")
//                .password("password")
//                .nickname("anotherhost")
//                .phoneNumber("010-4444-4444")
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(anotherHost);
//
//        GroupBuy anotherGroupBuy = GroupBuy.builder()
//                .host(anotherHost)
//                .title("Another Host's Group Buy")
//                .content("Another content")
//                .category("FOOD")
//                .totalPrice(20000)
//                .targetHeadcount(3)
//                .deadline(LocalDateTime.now().plusDays(5))
//                .deliveryMethod(DeliveryMethod.PARCEL)
//                .status(GroupBuyStatus.CLOSED)
//                .build();
//        groupBuyRepository.save(anotherGroupBuy);
//
//        Review review1 = Review.create(reviewer1, groupBuy, 5, "Review for host");
//        Review review2 = Review.create(reviewer2, groupBuy, 4, "Another review for host");
//        Review review3 = Review.create(reviewer1, anotherGroupBuy, 3, "Review for another host");
//        reviewRepository.saveAll(List.of(review1, review2, review3));
//
//        // when
//        List<Review> hostReviews = reviewRepository.findByGroupBuyHostId(host.getId());
//
//        // then
//        assertThat(hostReviews).hasSize(2);
//        assertThat(hostReviews).allMatch(r -> r.getGroupBuy().getHost().getId().equals(host.getId()));
//        assertThat(hostReviews).extracting(Review::getContent)
//                .containsExactlyInAnyOrder("Review for host", "Another review for host");
//    }
//}
