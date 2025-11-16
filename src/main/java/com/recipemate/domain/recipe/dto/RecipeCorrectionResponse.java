package com.recipemate.domain.recipe.dto;

import com.recipemate.domain.recipe.entity.CorrectionStatus;
import com.recipemate.domain.recipe.entity.CorrectionType;
import com.recipemate.domain.recipe.entity.RecipeCorrection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 레시피 개선 제안 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class RecipeCorrectionResponse {

    private Long id;
    private Long recipeId;
    private String recipeTitle;
    private Long proposerId;
    private String proposerNickname;
    private CorrectionType correctionType;
    private String correctionTypeDescription;
    private String proposedChange;
    private CorrectionStatus status;
    private String statusDescription;
    private String adminReason;
    private Long resolverId;
    private String resolverNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecipeCorrectionResponse from(RecipeCorrection correction) {
        return RecipeCorrectionResponse.builder()
                .id(correction.getId())
                .recipeId(correction.getRecipe().getId())
                .recipeTitle(correction.getRecipe().getTitle())
                .proposerId(correction.getProposer().getId())
                .proposerNickname(correction.getProposer().getNickname())
                .correctionType(correction.getCorrectionType())
                .correctionTypeDescription(correction.getCorrectionType().getDescription())
                .proposedChange(correction.getProposedChange())
                .status(correction.getStatus())
                .statusDescription(correction.getStatus().getDescription())
                .adminReason(correction.getAdminReason())
                .resolverId(correction.getResolver() != null ? correction.getResolver().getId() : null)
                .resolverNickname(correction.getResolver() != null ? correction.getResolver().getNickname() : null)
                .createdAt(correction.getCreatedAt())
                .updatedAt(correction.getUpdatedAt())
                .build();
    }
}
