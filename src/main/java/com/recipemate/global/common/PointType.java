package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    EARN("적립", "포인트 적립"),
    USE("사용", "포인트 사용"),
    CHARGE("충전", "포인트 충전"),
    REFUND("환불", "포인트 환불"),
    RECIPE_CORRECTION("보상", "레시피 개선 제안 승인 보상");

    private final String displayName;
    private final String description;
}

