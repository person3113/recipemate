package com.recipemate.domain.recipe.entity;

/**
 * 레시피 개선 제안 처리 상태
 */
public enum CorrectionStatus {
    PENDING("검토 대기"),
    APPROVED("승인됨"),
    REJECTED("기각됨");

    private final String description;

    CorrectionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
