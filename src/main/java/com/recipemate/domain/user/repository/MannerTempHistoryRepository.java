package com.recipemate.domain.user.repository;

import com.recipemate.domain.user.entity.MannerTempHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MannerTempHistoryRepository extends JpaRepository<MannerTempHistory, Long> {
    
    Page<MannerTempHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT mth, r.groupBuy.id FROM MannerTempHistory mth " +
           "LEFT JOIN Review r ON r.id = mth.relatedReviewId " +
           "WHERE mth.user.id = :userId " +
           "ORDER BY mth.createdAt DESC")
    Page<Object[]> findByUserIdWithGroupBuyId(@Param("userId") Long userId, Pageable pageable);
}
