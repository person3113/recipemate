package com.recipemate.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportedEntityType {
    RECIPE("레시피", "recipes"),
    POST("게시글", "community-posts"),
    COMMENT("댓글", "comments"),
    GROUP_PURCHASE("공동구매", "group-purchases"),
    USER("사용자", "users");

    private final String displayName;
    private final String urlPrefix;

    public String getEntityUrl(Long entityId) {
        if (this == COMMENT) {
            return null;
        }
        return "/" + urlPrefix + "/" + entityId;
    }
}
