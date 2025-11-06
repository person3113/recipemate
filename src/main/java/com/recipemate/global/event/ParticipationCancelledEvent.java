package com.recipemate.global.event;

import lombok.Getter;

@Getter
public class ParticipationCancelledEvent {
    private final Long userId;
    private final Long groupBuyId;

    public ParticipationCancelledEvent(Long userId, Long groupBuyId) {
        this.userId = userId;
        this.groupBuyId = groupBuyId;
    }
}
