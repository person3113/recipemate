package com.recipemate.global.event;

import lombok.Getter;

@Getter
public class GroupBuyCreatedEvent {
    private final Long userId;

    public GroupBuyCreatedEvent(Long userId) {
        this.userId = userId;
    }
}
