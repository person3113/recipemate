package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.global.common.GroupBuyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * GroupBuy 커스텀 Repository 인터페이스
 * 동적 쿼리를 통한 고급 검색 기능 제공
 */
public interface GroupBuyRepositoryCustom {
    
    /**
     * 검색 조건에 따라 공구 목록을 동적으로 검색
     * 
     * @param condition 검색 조건 (keyword, category, status, recipeOnly)
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<GroupBuy> searchGroupBuys(GroupBuySearchCondition condition, Pageable pageable);
    
    /**
     * 인기 공구 목록 조회 (참여자 수 기준 내림차순)
     * RECRUITING, IMMINENT 상태의 공구만 조회
     * 
     * @param statuses 조회할 상태 목록
     * @param pageable 페이징 정보
     * @return 인기 공구 목록
     */
    List<GroupBuy> findPopularGroupBuys(List<GroupBuyStatus> statuses, Pageable pageable);
}
