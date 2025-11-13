package com.recipemate.domain.search.entity;

import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 검색 키워드 엔티티
 * 사용자의 검색 키워드를 저장하고 검색 횟수를 추적
 */
@Entity
@Table(name = "search_keywords", indexes = {
        @Index(name = "idx_search_keyword_count", columnList = "search_count DESC"),
        @Index(name = "idx_search_keyword_keyword", columnList = "keyword", unique = true)
})
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String keyword;

    @Column(nullable = false)
    private Long searchCount;

    /**
     * 검색 횟수 증가
     */
    public void incrementSearchCount() {
        this.searchCount++;
    }

    /**
     * 새로운 검색 키워드 생성
     */
    public static SearchKeyword create(String keyword) {
        return SearchKeyword.builder()
                .keyword(keyword.trim().toLowerCase())
                .searchCount(1L)
                .build();
    }
}
