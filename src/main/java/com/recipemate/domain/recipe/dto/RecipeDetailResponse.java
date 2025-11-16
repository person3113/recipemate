package com.recipemate.domain.recipe.dto;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 레시피 상세 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDetailResponse {
    private String id;  // API ID for display (meal-123, food-456, or DB ID for USER recipes)
    private Long dbId;  // DB ID for operations (edit, delete)
    private String name;
    private String imageUrl;
    private String category;
    private String area;
    private String instructions;
    private String youtubeUrl;
    private String sourceUrl;
    private List<IngredientInfo> ingredients;
    private List<ManualStep> manualSteps;
    private NutritionInfo nutritionInfo;
    private String source; // API 출처
    private List<GroupBuyResponse> relatedGroupBuys; // 관련 공동구매 목록

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientInfo {
        private String name;
        private String measure;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManualStep {
        private int stepNumber;
        private String description;
        private String imageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionInfo {
        private String weight;
        private String energy;
        private String carbohydrate;
        private String protein;
        private String fat;
        private String sodium;
    }
}
