package com.recipemate.domain.recipe.entity;

/**
 * 레시피 개선 제안 종류
 */
public enum CorrectionType {
    TYPO("오타"),
    INCORRECT_INFO("잘못된 정보"),
    SUGGESTION("개선 제안"),
    OTHER("기타");

    private final String description;

    CorrectionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
