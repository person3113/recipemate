package com.recipemate.domain.recipe.dto;

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
    private String id;
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
