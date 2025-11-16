package com.recipemate.domain.groupbuy.dto;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.global.common.GroupBuyStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GroupPurchaseResponseDto {

    private Long id;
    private String title;
    private GroupBuyStatus status;
    private LocalDate deadline;
    private boolean deletable;

    public GroupPurchaseResponseDto(GroupBuy entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.status = entity.getStatus();
        this.deadline = entity.getDeadline().toLocalDate();
        this.deletable = entity.isDeletable();
    }
}