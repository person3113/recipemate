package com.recipemate.domain.wishlist.service;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.wishlist.entity.Wishlist;
import com.recipemate.domain.wishlist.repository.WishlistRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService 테스트")
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("찜 추가 성공")
    void addWishlist_Success() {
        // given
        Long userId = 1L;
        Long purchaseId = 1L;

        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();

        GroupBuy groupBuy = GroupBuy.builder()
                .host(user)
                .title("테스트 공구")
                .content("테스트 내용")
                .category("식재료")
                .totalPrice(10000)
                .targetHeadcount(5)
                .currentHeadcount(0)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .meetupLocation("서울역")
                .parcelFee(3000)
                .isParticipantListPublic(true)
                .status(GroupBuyStatus.RECRUITING)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupBuyRepository.findById(purchaseId)).willReturn(Optional.of(groupBuy));
        given(wishlistRepository.existsByUserIdAndGroupBuyId(userId, purchaseId)).willReturn(false);
        given(wishlistRepository.save(any(Wishlist.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        wishlistService.addWishlist(userId, purchaseId);

        // then
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("찜 추가 실패 - 사용자를 찾을 수 없음")
    void addWishlist_Fail_UserNotFound() {
        // given
        Long userId = 999L;
        Long purchaseId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.addWishlist(userId, purchaseId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("찜 추가 실패 - 공구를 찾을 수 없음")
    void addWishlist_Fail_GroupBuyNotFound() {
        // given
        Long userId = 1L;
        Long purchaseId = 999L;

        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupBuyRepository.findById(purchaseId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.addWishlist(userId, purchaseId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);

        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("찜 추가 실패 - 중복 찜")
    void addWishlist_Fail_AlreadyWished() {
        // given
        Long userId = 1L;
        Long purchaseId = 1L;

        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();

        GroupBuy groupBuy = GroupBuy.builder()
                .host(user)
                .title("테스트 공구")
                .content("테스트 내용")
                .category("식재료")
                .totalPrice(10000)
                .targetHeadcount(5)
                .currentHeadcount(0)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .meetupLocation("서울역")
                .parcelFee(3000)
                .isParticipantListPublic(true)
                .status(GroupBuyStatus.RECRUITING)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupBuyRepository.findById(purchaseId)).willReturn(Optional.of(groupBuy));
        given(wishlistRepository.existsByUserIdAndGroupBuyId(userId, purchaseId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> wishlistService.addWishlist(userId, purchaseId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_ALREADY_EXISTS);

        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("찜 취소 성공")
    void removeWishlist_Success() {
        // given
        Long userId = 1L;
        Long purchaseId = 1L;

        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();

        GroupBuy groupBuy = GroupBuy.builder()
                .host(user)
                .title("테스트 공구")
                .content("테스트 내용")
                .category("식재료")
                .totalPrice(10000)
                .targetHeadcount(5)
                .currentHeadcount(0)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .meetupLocation("서울역")
                .parcelFee(3000)
                .isParticipantListPublic(true)
                .status(GroupBuyStatus.RECRUITING)
                .build();

        Wishlist wishlist = Wishlist.create(user, groupBuy);

        given(wishlistRepository.findByUserIdAndGroupBuyId(userId, purchaseId))
                .willReturn(Optional.of(wishlist));

        // when
        wishlistService.removeWishlist(userId, purchaseId);

        // then
        verify(wishlistRepository, times(1)).delete(wishlist);
    }

    @Test
    @DisplayName("찜 취소 실패 - 찜 내역이 없음")
    void removeWishlist_Fail_WishlistNotFound() {
        // given
        Long userId = 1L;
        Long purchaseId = 1L;

        given(wishlistRepository.findByUserIdAndGroupBuyId(userId, purchaseId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.removeWishlist(userId, purchaseId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_NOT_FOUND);

        verify(wishlistRepository, never()).delete(any(Wishlist.class));
    }
}
