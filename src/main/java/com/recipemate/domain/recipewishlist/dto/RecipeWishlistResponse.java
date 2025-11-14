package com.recipemate.domain.recipewishlist.dto;

import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipewishlist.entity.RecipeWishlist;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecipeWishlistResponse {

    // 찜 정보
    private Long wishlistId;
    private LocalDateTime wishedAt;
    
    // 레시피 정보
    private Long recipeId;
    private String title;
    private String thumbnailImageUrl;
    private String fullImageUrl;
    private String category;
    private String area;
    private RecipeSource sourceApi;
    private Integer calories;
    private Integer carbohydrate;
    private Integer protein;
    private Integer fat;
    private Integer sodium;
    
    // 삭제된 레시피 여부
    private boolean isDeleted;
    
    public static RecipeWishlistResponse from(RecipeWishlist wishlist) {
        Recipe recipe = wishlist.getRecipe();
        
        // 소프트 삭제된 레시피인 경우
        if (recipe == null) {
            return RecipeWishlistResponse.builder()
                    .wishlistId(wishlist.getId())
                    .wishedAt(wishlist.getWishedAt())
                    .isDeleted(true)
                    .title("삭제된 레시피")
                    .build();
        }
        
        return RecipeWishlistResponse.builder()
                .wishlistId(wishlist.getId())
                .wishedAt(wishlist.getWishedAt())
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .thumbnailImageUrl(recipe.getThumbnailImageUrl())
                .fullImageUrl(recipe.getFullImageUrl())
                .category(recipe.getCategory())
                .area(recipe.getArea())
                .sourceApi(recipe.getSourceApi())
                .calories(recipe.getCalories())
                .carbohydrate(recipe.getCarbohydrate())
                .protein(recipe.getProtein())
                .fat(recipe.getFat())
                .sodium(recipe.getSodium())
                .isDeleted(false)
                .build();
    }
}
