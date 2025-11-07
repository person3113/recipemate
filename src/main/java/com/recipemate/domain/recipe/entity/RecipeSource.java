package com.recipemate.domain.recipe.entity;

/**
 * 레시피 데이터 출처 구분
 */
public enum RecipeSource {
    /**
     * TheMealDB API
     */
    MEAL_DB,

    /**
     * 식품안전나라 API
     */
    FOOD_SAFETY,

    /**
     * 사용자 직접 등록
     */
    USER
}
