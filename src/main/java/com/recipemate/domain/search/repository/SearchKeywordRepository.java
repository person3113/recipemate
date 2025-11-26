package com.recipemate.domain.search.repository;

import com.recipemate.domain.search.entity.SearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 검색 키워드 레포지토리
 */
@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    /**
     * 키워드로 검색 키워드 엔티티 조회
     */
    Optional<SearchKeyword> findByKeyword(String keyword);

    /**
     * 검색 횟수가 많은 순으로 상위 N개 조회
     */
    List<SearchKeyword> findAllByOrderBySearchCountDesc(Pageable pageable);
    
    /**
     * 검색 횟수 증가 (동시성 제어)
     */
    @Modifying
    @Transactional
    @Query("UPDATE SearchKeyword sk SET sk.searchCount = sk.searchCount + 1 WHERE sk.keyword = :keyword")
    int incrementSearchCount(@Param("keyword") String keyword);
}
