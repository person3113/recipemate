package com.recipemate.domain.badge.dto;

import com.recipemate.global.common.BadgeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BadgeChallengeResponse {

    private BadgeType badgeType;
    private String displayName;
    private String description;
    private String iconUrl;
    private boolean isAcquired;
    private String progress;
    private long currentCount;
    private long goalCount;
}
