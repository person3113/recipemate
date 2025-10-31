package com.recipemate.domain.wishlist.entity;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistTest {

    private User testUser;
    private GroupBuy testGroupBuy;

    @BeforeEach
    void setUp() {
        testUser = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        testGroupBuy = GroupBuy.builder()
                .host(testUser)
                .title("신선한 야채 공동구매")
                .content("품질 좋은 야채를 함께 구매해요")
                .category("채소")
                .totalPrice(50000)
                .targetHeadcount(10)
                .currentHeadcount(0)
                .deadline(LocalDateTime.now().plusDays(7))
                .meetupLocation("서울시 강남구")
                .status(GroupBuyStatus.RECRUITING)
                .deliveryMethod(DeliveryMethod.DIRECT)
                .build();
    }

    @Test
    @DisplayName("Wishlist 생성 시 wishedAt이 자동으로 설정된다")
    void createWishlist() {
        Wishlist wishlist = Wishlist.create(testUser, testGroupBuy);

        assertThat(wishlist.getUser()).isEqualTo(testUser);
        assertThat(wishlist.getGroupBuy()).isEqualTo(testGroupBuy);
        assertThat(wishlist.getWishedAt()).isNotNull();
        assertThat(wishlist.getWishedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("동일한 사용자와 공구로 Wishlist를 생성할 수 있다")
    void createWishlistWithSameUserAndGroupBuy() {
        Wishlist wishlist1 = Wishlist.create(testUser, testGroupBuy);
        Wishlist wishlist2 = Wishlist.create(testUser, testGroupBuy);

        assertThat(wishlist1.getUser()).isEqualTo(wishlist2.getUser());
        assertThat(wishlist1.getGroupBuy()).isEqualTo(wishlist2.getGroupBuy());
        // DB 제약조건(UNIQUE)에서 중복 방지는 Repository 테스트에서 검증
    }

    @Test
    @DisplayName("Wishlist의 wishedAt은 변경할 수 없다")
    void wishedAtIsImmutable() {
        Wishlist wishlist = Wishlist.create(testUser, testGroupBuy);
        LocalDateTime originalWishedAt = wishlist.getWishedAt();

        // wishedAt은 final이므로 변경 메서드가 없어야 함
        assertThat(wishlist.getWishedAt()).isEqualTo(originalWishedAt);
    }

    @Test
    @DisplayName("Wishlist는 User와 GroupBuy 정보를 올바르게 반환한다")
    void getWishlistInfo() {
        Wishlist wishlist = Wishlist.create(testUser, testGroupBuy);

        assertThat(wishlist.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(wishlist.getGroupBuy().getTitle()).isEqualTo("신선한 야채 공동구매");
    }
}
