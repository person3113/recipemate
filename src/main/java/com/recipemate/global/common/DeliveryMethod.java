package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryMethod {
    DIRECT("직거래"),
    PARCEL("택배"),
    BOTH("택배/직거래");

    private final String displayName;
}
