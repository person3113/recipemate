package com.recipemate.domain.badge.dto;

import com.recipemate.domain.badge.entity.Badge;
import com.recipemate.global.common.BadgeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BadgeResponse {

    private Long id;
    private BadgeType badgeType;
    private String displayName;
    private String description;
    private LocalDateTime acquiredAt;

    public static BadgeResponse from(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .badgeType(badge.getBadgeType())
                .displayName(badge.getBadgeType().getDisplayName())
                .description(badge.getBadgeType().getDescription())
                .acquiredAt(badge.getAcquiredAt())
                .build();
    }
}
