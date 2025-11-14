package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {
    TIPS("팁 공유"),
    FREE("자유");

    private final String displayName;
}
