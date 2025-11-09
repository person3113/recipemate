package com.recipemate.domain.wishlist.service;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.wishlist.dto.WishlistResponse;
import com.recipemate.domain.wishlist.entity.Wishlist;
import com.recipemate.domain.wishlist.repository.WishlistRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;

    @Transactional
    public void addWishlist(Long userId, Long purchaseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GroupBuy groupBuy = groupBuyRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        if (wishlistRepository.existsByUserIdAndGroupBuyId(userId, purchaseId)) {
            throw new CustomException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        Wishlist wishlist = Wishlist.create(user, groupBuy);
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeWishlist(Long userId, Long purchaseId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndGroupBuyId(userId, purchaseId)
                .orElseThrow(() -> new CustomException(ErrorCode.WISHLIST_NOT_FOUND));

        wishlistRepository.delete(wishlist);
    }

    public boolean isWishlisted(Long userId, Long purchaseId) {
        return wishlistRepository.existsByUserIdAndGroupBuyId(userId, purchaseId);
    }

    public Page<WishlistResponse> getMyWishlist(Long userId, Pageable pageable) {
        // 사용자 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 찜 목록 조회
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByWishedAtDesc(userId, pageable);
        
        // DTO 변환
        List<WishlistResponse> responses = wishlists.stream()
                .map(WishlistResponse::from)
                .collect(Collectors.toList());

        // Page 객체 생성 (전체 개수는 별도 카운트 쿼리 필요하지만, 간단하게 구현)
        return new PageImpl<>(responses, pageable, responses.size());
    }
}
