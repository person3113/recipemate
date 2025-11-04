package com.recipemate.domain.user.dto;

import com.recipemate.domain.user.entity.PointHistory;
import com.recipemate.global.common.PointType;

import java.time.LocalDateTime;

public record PointHistoryResponse(
        Long id,
        Integer amount,
        String description,
        PointType type,
        LocalDateTime createdAt
) {
    public static PointHistoryResponse from(PointHistory pointHistory) {
        return new PointHistoryResponse(
                pointHistory.getId(),
                pointHistory.getAmount(),
                pointHistory.getDescription(),
                pointHistory.getType(),
                pointHistory.getCreatedAt()
        );
    }
}
