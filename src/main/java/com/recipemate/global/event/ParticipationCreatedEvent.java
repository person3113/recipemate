package com.recipemate.global.event;

import lombok.Getter;

@Getter
public class ParticipationCreatedEvent {
    private final Long userId;
    private final Long groupBuyId;

    public ParticipationCreatedEvent(Long userId, Long groupBuyId) {
        this.userId = userId;
        this.groupBuyId = groupBuyId;
    }
}
