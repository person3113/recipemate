package com.recipemate.domain.groupbuy.dto;

import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.GroupBuyCategory;
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

    /**
     * 재료명 검색
     */
    private String ingredients;

    /**
     * 정렬 기준: latest(최신순), deadline(마감임박순), price(가격순), participants(참여자순)
     */
    private String sortBy;

    /**
     * 정렬 방향: asc(오름차순), desc(내림차순)
     */
    private String direction;
}
