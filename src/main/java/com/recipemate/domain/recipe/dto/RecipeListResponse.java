package com.recipemate.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 레시피 목록 조회 응답 DTO
 * 두 API(TheMealDB, 식품안전나라)의 결과를 통합하여 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeListResponse {
    private List<RecipeSimpleInfo> recipes;
    private int totalCount;
    private String source; // "themealdb" 또는 "foodsafety" 또는 "both"

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeSimpleInfo {
        private String id;
        private String name;
        private String imageUrl;
        private String category;
        private String source; // API 출처
    }
}
