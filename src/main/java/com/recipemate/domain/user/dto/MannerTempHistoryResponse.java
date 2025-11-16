package com.recipemate.domain.user.dto;

import com.recipemate.domain.user.entity.MannerTempHistory;

import java.time.LocalDateTime;

public record MannerTempHistoryResponse(
        Long id,
        Double changeValue,
        Double previousTemperature,
        Double newTemperature,
        String reason,
        Long relatedReviewId,
        Long relatedGroupBuyId,
        LocalDateTime createdAt
) {
    public static MannerTempHistoryResponse from(MannerTempHistory history) {
        return new MannerTempHistoryResponse(
                history.getId(),
                history.getChangeValue(),
                history.getPreviousTemperature(),
                history.getNewTemperature(),
                history.getReason(),
                history.getRelatedReviewId(),
                null, // relatedGroupBuyId will be set by custom query
                history.getCreatedAt()
        );
    }
    
    public static MannerTempHistoryResponse from(MannerTempHistory history, Long groupBuyId) {
        return new MannerTempHistoryResponse(
                history.getId(),
                history.getChangeValue(),
                history.getPreviousTemperature(),
                history.getNewTemperature(),
                history.getReason(),
                history.getRelatedReviewId(),
                groupBuyId,
                history.getCreatedAt()
        );
    }
}
