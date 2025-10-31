package com.recipemate.domain.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * TheMealDB API의 단일 레시피 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MealResponse {

    @JsonProperty("idMeal")
    private String id;

    @JsonProperty("strMeal")
    private String name;

    @JsonProperty("strCategory")
    private String category;

    @JsonProperty("strArea")
    private String area;

    @JsonProperty("strInstructions")
    private String instructions;

    @JsonProperty("strMealThumb")
    private String thumbnail;

    @JsonProperty("strYoutube")
    private String youtubeUrl;

    @JsonProperty("strSource")
    private String sourceUrl;

    // Ingredients (strIngredient1 ~ strIngredient20)
    @JsonProperty("strIngredient1")
    private String ingredient1;
    @JsonProperty("strIngredient2")
    private String ingredient2;
    @JsonProperty("strIngredient3")
    private String ingredient3;
    @JsonProperty("strIngredient4")
    private String ingredient4;
    @JsonProperty("strIngredient5")
    private String ingredient5;
    @JsonProperty("strIngredient6")
    private String ingredient6;
    @JsonProperty("strIngredient7")
    private String ingredient7;
    @JsonProperty("strIngredient8")
    private String ingredient8;
    @JsonProperty("strIngredient9")
    private String ingredient9;
    @JsonProperty("strIngredient10")
    private String ingredient10;
    @JsonProperty("strIngredient11")
    private String ingredient11;
    @JsonProperty("strIngredient12")
    private String ingredient12;
    @JsonProperty("strIngredient13")
    private String ingredient13;
    @JsonProperty("strIngredient14")
    private String ingredient14;
    @JsonProperty("strIngredient15")
    private String ingredient15;
    @JsonProperty("strIngredient16")
    private String ingredient16;
    @JsonProperty("strIngredient17")
    private String ingredient17;
    @JsonProperty("strIngredient18")
    private String ingredient18;
    @JsonProperty("strIngredient19")
    private String ingredient19;
    @JsonProperty("strIngredient20")
    private String ingredient20;

    // Measures (strMeasure1 ~ strMeasure20)
    @JsonProperty("strMeasure1")
    private String measure1;
    @JsonProperty("strMeasure2")
    private String measure2;
    @JsonProperty("strMeasure3")
    private String measure3;
    @JsonProperty("strMeasure4")
    private String measure4;
    @JsonProperty("strMeasure5")
    private String measure5;
    @JsonProperty("strMeasure6")
    private String measure6;
    @JsonProperty("strMeasure7")
    private String measure7;
    @JsonProperty("strMeasure8")
    private String measure8;
    @JsonProperty("strMeasure9")
    private String measure9;
    @JsonProperty("strMeasure10")
    private String measure10;
    @JsonProperty("strMeasure11")
    private String measure11;
    @JsonProperty("strMeasure12")
    private String measure12;
    @JsonProperty("strMeasure13")
    private String measure13;
    @JsonProperty("strMeasure14")
    private String measure14;
    @JsonProperty("strMeasure15")
    private String measure15;
    @JsonProperty("strMeasure16")
    private String measure16;
    @JsonProperty("strMeasure17")
    private String measure17;
    @JsonProperty("strMeasure18")
    private String measure18;
    @JsonProperty("strMeasure19")
    private String measure19;
    @JsonProperty("strMeasure20")
    private String measure20;

    /**
     * 재료와 분량을 파싱하여 리스트로 반환
     * 빈 값이나 공백은 제외
     */
    public List<IngredientInfo> getIngredients() {
        List<IngredientInfo> ingredients = new ArrayList<>();
        String[] ingredientNames = {
            ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
            ingredient6, ingredient7, ingredient8, ingredient9, ingredient10,
            ingredient11, ingredient12, ingredient13, ingredient14, ingredient15,
            ingredient16, ingredient17, ingredient18, ingredient19, ingredient20
        };
        String[] measures = {
            measure1, measure2, measure3, measure4, measure5,
            measure6, measure7, measure8, measure9, measure10,
            measure11, measure12, measure13, measure14, measure15,
            measure16, measure17, measure18, measure19, measure20
        };

        for (int i = 0; i < 20; i++) {
            String ingredientName = ingredientNames[i];
            String measure = measures[i];
            
            // 재료명이 null이거나 빈 문자열이면 건너뛰기
            if (ingredientName == null || ingredientName.trim().isEmpty()) {
                continue;
            }
            
            ingredients.add(new IngredientInfo(
                ingredientName.trim(),
                measure != null ? measure.trim() : ""
            ));
        }

        return ingredients;
    }

    /**
     * 재료 정보를 담는 내부 클래스
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IngredientInfo {
        private String name;
        private String measure;
    }
}
