//package com.recipemate.domain.wishlist.repository;
//
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.domain.wishlist.entity.Wishlist;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.PageRequest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@DataJpaTest
//@Import(com.recipemate.global.config.QueryDslConfig.class)
//class WishlistRepositoryTest {
//
//    @Autowired
//    private WishlistRepository wishlistRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private GroupBuyRepository groupBuyRepository;
//
//    private User testUser;
//    private GroupBuy testGroupBuy;
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.create(
//                "test@example.com",
//                "encodedPassword",
//                "테스터",
//                "010-1234-5678"
//        );
//        userRepository.save(testUser);
//
//        testGroupBuy = GroupBuy.builder()
//                .host(testUser)
//                .title("신선한 야채 공동구매")
//                .content("품질 좋은 야채를 함께 구매해요")
//                .category("채소")
//                .totalPrice(50000)
//                .targetHeadcount(10)
//                .currentHeadcount(0)
//                .deadline(LocalDateTime.now().plusDays(7))
//                .meetupLocation("서울시 강남구")
//                .status(GroupBuyStatus.RECRUITING)
//                .deliveryMethod(DeliveryMethod.DIRECT)
//                .build();
//        groupBuyRepository.save(testGroupBuy);
//    }
//
//    @Test
//    @DisplayName("사용자 ID와 공구 ID로 찜을 조회할 수 있다")
//    void findByUserIdAndGroupBuyId() {
//        Wishlist wishlist = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist);
//
//        Optional<Wishlist> found = wishlistRepository.findByUserIdAndGroupBuyId(
//                testUser.getId(),
//                testGroupBuy.getId()
//        );
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
//        assertThat(found.get().getGroupBuy().getId()).isEqualTo(testGroupBuy.getId());
//    }
//
//    @Test
//    @DisplayName("사용자의 찜 목록을 최신순으로 조회할 수 있다")
//    void findByUserIdOrderByWishedAtDesc() throws InterruptedException {
//        // 첫 번째 찜
//        Wishlist wishlist1 = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist1);
//
//        // 두 번째 공구 생성
//        GroupBuy groupBuy2 = GroupBuy.builder()
//                .host(testUser)
//                .title("신선한 과일 공동구매")
//                .content("품질 좋은 과일을 함께 구매해요")
//                .category("과일")
//                .totalPrice(30000)
//                .targetHeadcount(5)
//                .currentHeadcount(0)
//                .deadline(LocalDateTime.now().plusDays(5))
//                .meetupLocation("서울시 서초구")
//                .status(GroupBuyStatus.RECRUITING)
//                .deliveryMethod(DeliveryMethod.DIRECT)
//                .build();
//        groupBuyRepository.save(groupBuy2);
//
//        Thread.sleep(10); // wishedAt 시간 차이를 위해
//
//        // 두 번째 찜
//        Wishlist wishlist2 = Wishlist.create(testUser, groupBuy2);
//        wishlistRepository.save(wishlist2);
//
//        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByWishedAtDesc(
//                testUser.getId(),
//                PageRequest.of(0, 10)
//        );
//
//        assertThat(wishlists).hasSize(2);
//        assertThat(wishlists.get(0).getWishedAt()).isAfter(wishlists.get(1).getWishedAt());
//    }
//
//    @Test
//    @DisplayName("사용자와 공구의 찜 존재 여부를 확인할 수 있다")
//    void existsByUserIdAndGroupBuyId() {
//        Wishlist wishlist = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist);
//
//        boolean exists = wishlistRepository.existsByUserIdAndGroupBuyId(
//                testUser.getId(),
//                testGroupBuy.getId()
//        );
//
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("동일한 사용자와 공구로 중복 찜을 저장할 수 없다 (UNIQUE 제약조건)")
//    void duplicateWishlistShouldThrowException() {
//        Wishlist wishlist1 = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist1);
//
//        Wishlist wishlist2 = Wishlist.create(testUser, testGroupBuy);
//
//        assertThatThrownBy(() -> wishlistRepository.saveAndFlush(wishlist2))
//                .isInstanceOf(Exception.class);
//    }
//
//    @Test
//    @DisplayName("서로 다른 사용자는 같은 공구를 찜할 수 있다")
//    void differentUserCanWishlistSameGroupBuy() {
//        User anotherUser = User.create(
//                "another@example.com",
//                "password",
//                "또다른사용자",
//                "010-9999-9999"
//        );
//        userRepository.save(anotherUser);
//
//        Wishlist wishlist1 = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist1);
//
//        Wishlist wishlist2 = Wishlist.create(anotherUser, testGroupBuy);
//        wishlistRepository.save(wishlist2);
//
//        assertThat(wishlistRepository.findAll()).hasSize(2);
//    }
//
//    @Test
//    @DisplayName("같은 사용자는 서로 다른 공구를 찜할 수 있다")
//    void sameUserCanWishlistDifferentGroupBuys() {
//        GroupBuy groupBuy2 = GroupBuy.builder()
//                .host(testUser)
//                .title("신선한 과일 공동구매")
//                .content("품질 좋은 과일을 함께 구매해요")
//                .category("과일")
//                .totalPrice(30000)
//                .targetHeadcount(5)
//                .currentHeadcount(0)
//                .deadline(LocalDateTime.now().plusDays(5))
//                .meetupLocation("서울시 서초구")
//                .status(GroupBuyStatus.RECRUITING)
//                .deliveryMethod(DeliveryMethod.DIRECT)
//                .build();
//        groupBuyRepository.save(groupBuy2);
//
//        Wishlist wishlist1 = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist1);
//
//        Wishlist wishlist2 = Wishlist.create(testUser, groupBuy2);
//        wishlistRepository.save(wishlist2);
//
//        assertThat(wishlistRepository.findAll()).hasSize(2);
//    }
//
//    @Test
//    @DisplayName("찜을 삭제할 수 있다")
//    void deleteWishlist() {
//        Wishlist wishlist = Wishlist.create(testUser, testGroupBuy);
//        wishlistRepository.save(wishlist);
//
//        wishlistRepository.delete(wishlist);
//
//        boolean exists = wishlistRepository.existsByUserIdAndGroupBuyId(
//                testUser.getId(),
//                testGroupBuy.getId()
//        );
//        assertThat(exists).isFalse();
//    }
//}
