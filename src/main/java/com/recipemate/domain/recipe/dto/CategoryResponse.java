package com.recipemate.domain.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TheMealDB API의 카테고리 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryResponse {

    @JsonProperty("idCategory")
    private String id;

    @JsonProperty("strCategory")
    private String name;

    @JsonProperty("strCategoryThumb")
    private String thumbnail;

    @JsonProperty("strCategoryDescription")
    private String description;
}
