package com.recipemate.domain.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 식품안전나라 API의 레시피 목록 응답 DTO
 * COOKRCP01 객체를 매핑
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CookRecipeListResponse {

    /**
     * 전체 데이터 개수
     */
    @JsonProperty("total_count")
    private String totalCount;

    /**
     * 레시피 목록
     */
    @JsonProperty("row")
    private List<CookRecipeResponse> row;

    /**
     * API 응답 결과 정보
     */
    @JsonProperty("RESULT")
    private FoodSafetyApiResult result;
}
