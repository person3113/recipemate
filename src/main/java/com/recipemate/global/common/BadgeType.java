package com.recipemate.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeType {
    FIRST_GROUP_BUY("첫 공구 주최자", "첫 번째 공동구매를 생성했습니다"),
    TEN_PARTICIPATIONS("열혈 참여자", "공동구매에 10회 참여했습니다"),
    REVIEWER("후기 마스터", "후기를 5개 작성했습니다"),
    POPULAR_HOST("인기 호스트", "매너온도 40도 이상을 달성했습니다");

    private final String displayName;
    private final String description;
}
