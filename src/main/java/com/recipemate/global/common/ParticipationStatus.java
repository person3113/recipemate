package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipationStatus {
    PAYMENT_COMPLETED("결제완료", "결제가 완료된 상태"),
    CANCELLED("취소됨", "참여가 취소된 상태");

    private final String displayName;
    private final String description;
}
