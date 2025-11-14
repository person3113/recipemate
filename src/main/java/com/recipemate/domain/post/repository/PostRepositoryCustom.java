package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.dto.PostWithCountsDto;
import com.recipemate.global.common.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL을 활용한 동적 쿼리 메서드 정의
 */
public interface PostRepositoryCustom {
    
    /**
     * 필터링, 검색, 동적 정렬을 지원하는 게시글 목록 조회
     * 
     * @param category 카테고리 필터 (null 가능)
     * @param keyword 검색 키워드 (null 가능)
     * @param pageable 페이징 및 정렬 정보
     * @return 게시글 목록 (좋아요 수, 댓글 수 포함)
     */
    Page<PostWithCountsDto> findAllWithCountsDynamic(
        PostCategory category, 
        String keyword, 
        Pageable pageable
    );
}
