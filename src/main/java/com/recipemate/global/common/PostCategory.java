package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {
    TIPS("팁 공유"),
    FREE("자유"),
    REVIEW("후기");

    private final String displayName;
}
