package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    EARN("적립", "포인트 적립"),
    USE("사용", "포인트 사용");

    private final String displayName;
    private final String description;
}
