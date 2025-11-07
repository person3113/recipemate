package com.recipemate.domain.groupbuy.dto;

import com.recipemate.global.common.GroupBuyCategory;
import com.recipemate.global.common.GroupBuyStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupBuySearchCondition {

    /**
     * 카테고리 필터
     */
    private GroupBuyCategory category;

    /**
     * 공구 상태 필터 (RECRUITING, IMMINENT, CLOSED)
     */
    private GroupBuyStatus status;

    /**
     * 레시피 기반 공구만 조회 여부
     */
    @Builder.Default
    private Boolean recipeOnly = false;

    /**
     * 검색 키워드 (제목 또는 내용에서 검색)
     */
    private String keyword;
}
