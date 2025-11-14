package com.recipemate.domain.recipewishlist.service;

import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.domain.recipewishlist.dto.RecipeWishlistResponse;
import com.recipemate.domain.recipewishlist.entity.RecipeWishlist;
import com.recipemate.domain.recipewishlist.repository.RecipeWishlistRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
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
public class RecipeWishlistService {

    private final RecipeWishlistRepository recipeWishlistRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public void addWishlist(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        if (recipeWishlistRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
            throw new CustomException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        RecipeWishlist wishlist = RecipeWishlist.create(user, recipe);
        recipeWishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeWishlist(Long userId, Long recipeId) {
        // 삭제된 레시피도 찜 취소가 가능하도록 네이티브 쿼리로 직접 조회
        RecipeWishlist wishlist = recipeWishlistRepository.findByUserIdAndRecipeIdIncludingDeleted(userId, recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.WISHLIST_NOT_FOUND));

        recipeWishlistRepository.delete(wishlist);
    }

    public Page<RecipeWishlistResponse> getMyWishlist(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<RecipeWishlist> wishlists = recipeWishlistRepository.findByUserIdOrderByWishedAtDesc(userId, pageable);
        
        // 삭제된 레시피는 자동으로 필터링 (recipe가 null인 경우 제외)
        List<RecipeWishlistResponse> responses = wishlists.stream()
                .filter(wishlist -> wishlist.getRecipe() != null) // 삭제된 레시피 제외
                .map(RecipeWishlistResponse::from)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, responses.size());
    }

    public boolean isWishlisted(Long userId, Long recipeId) {
        return recipeWishlistRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }
}
