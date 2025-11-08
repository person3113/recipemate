package com.recipemate.global.event;

import lombok.Getter;

/**
 * 공구 마감 이벤트
 * 공구가 마감될 때 참여자 전원에게 알림을 보내기 위한 이벤트
 */
@Getter
public class GroupBuyDeadlineEvent {
    private final Long groupBuyId;

    public GroupBuyDeadlineEvent(Long groupBuyId) {
        this.groupBuyId = groupBuyId;
    }
}
