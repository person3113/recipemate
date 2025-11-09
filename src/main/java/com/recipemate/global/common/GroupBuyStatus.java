package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupBuyStatus {
    RECRUITING("모집중"),
    IMMINENT("마감 임박"),
    CLOSED("마감");
    
    private final String displayName;
}
