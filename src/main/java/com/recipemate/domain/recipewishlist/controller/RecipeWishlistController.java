package com.recipemate.domain.recipewishlist.controller;

import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.domain.recipewishlist.dto.RecipeWishlistResponse;
import com.recipemate.domain.recipewishlist.service.RecipeWishlistService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.ApiResponse;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/recipes/{recipeId}/bookmarks")
@RequiredArgsConstructor
public class RecipeWishlistController {

    private final RecipeWishlistService recipeWishlistService;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    /**
     * 레시피 찜 추가
     * POST /recipes/{recipeId}/bookmarks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @PathVariable String recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()));
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // recipeId를 DB ID로 변환
        Long dbRecipeId = resolveRecipeId(recipeId);
        
        log.info("레시피 찜 추가 요청: userId={}, recipeId={}, dbId={}", user.getId(), recipeId, dbRecipeId);
        
        recipeWishlistService.addWishlist(user.getId(), dbRecipeId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 레시피 찜 취소
     * POST /recipes/{recipeId}/bookmarks/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @PathVariable String recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()));
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // recipeId를 DB ID로 변환
        Long dbRecipeId = resolveRecipeId(recipeId);
        
        log.info("레시피 찜 취소 요청: userId={}, recipeId={}, dbId={}", user.getId(), recipeId, dbRecipeId);
        
        recipeWishlistService.removeWishlist(user.getId(), dbRecipeId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 레시피 찜 상태 확인
     * GET /recipes/{recipeId}/bookmarks/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkBookmarkStatus(
            @PathVariable String recipeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // recipeId를 DB ID로 변환
        Long dbRecipeId = resolveRecipeId(recipeId);
        
        boolean isBookmarked = recipeWishlistService.isWishlisted(user.getId(), dbRecipeId);
        
        return ResponseEntity.ok(ApiResponse.success(isBookmarked));
    }

    /**
     * 내 레시피 찜 목록 조회
     * GET /recipes/bookmarks/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<RecipeWishlistResponse>>> getMyBookmarks(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        log.info("내 레시피 찜 목록 조회 요청: userId={}", user.getId());
        
        Page<RecipeWishlistResponse> wishlists = recipeWishlistService.getMyWishlist(user.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success(wishlists));
    }
    
    /**
     * recipeId를 DB ID로 변환하는 헬퍼 메서드
     * @param recipeId 숫자 또는 API ID (예: "123" 또는 "meal-52935")
     * @return DB ID (Long)
     */
    private Long resolveRecipeId(String recipeId) {
        // recipeId가 순수 숫자인지 확인 (DB ID)
        if (recipeId.matches("\\d+")) {
            // DB ID로 직접 반환 (삭제된 레시피도 찜 취소 가능하도록)
            return Long.parseLong(recipeId);
        } else {
            // API ID로 조회하여 DB ID 반환
            // API ID 형식: "meal-52935" 또는 "food-1234"
            String sourceApiId;
            RecipeSource sourceApi;
            
            if (recipeId.startsWith("meal-")) {
                sourceApiId = recipeId.substring(5); // "meal-" 제거
                sourceApi = RecipeSource.MEAL_DB;
            } else if (recipeId.startsWith("food-")) {
                sourceApiId = recipeId.substring(5); // "food-" 제거
                sourceApi = RecipeSource.FOOD_SAFETY;
            } else {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            
            // DB에서 sourceApi와 sourceApiId로 레시피 조회
            Recipe recipe = recipeRepository.findBySourceApiAndSourceApiId(sourceApi, sourceApiId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
            
            return recipe.getId();
        }
    }
}
