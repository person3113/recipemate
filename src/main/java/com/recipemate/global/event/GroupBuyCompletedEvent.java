package com.recipemate.global.event;

import lombok.Getter;

/**
 * 공구 목표 달성 이벤트
 * 공구의 참여 인원이 목표 인원에 도달했을 때 참여자 전원에게 알림을 보내기 위한 이벤트
 */
@Getter
public class GroupBuyCompletedEvent {
    private final Long groupBuyId;

    public GroupBuyCompletedEvent(Long groupBuyId) {
        this.groupBuyId = groupBuyId;
    }
}
