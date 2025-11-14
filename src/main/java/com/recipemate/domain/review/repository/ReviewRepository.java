package com.recipemate.domain.review.repository;

import com.recipemate.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 공구의 후기 목록을 최신순으로 조회 (삭제되지 않은 것만)
     * idx_review_group_buy_id 인덱스 활용
     */
    @Query("SELECT r FROM Review r WHERE r.groupBuy.id = :groupBuyId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Review> findByGroupBuyIdOrderByCreatedAtDesc(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 공구의 후기 목록을 페이징하여 조회 (최신순, 삭제되지 않은 것만)
     * idx_review_group_buy_id 인덱스 활용
     */
    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer JOIN FETCH r.groupBuy WHERE r.groupBuy.id = :groupBuyId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Review> findByGroupBuyIdWithDetails(@Param("groupBuyId") Long groupBuyId, Pageable pageable);

    /**
     * 전체 후기 목록을 페이징하여 조회 (최신순, 삭제되지 않은 것만)
     */
    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer JOIN FETCH r.groupBuy WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Review> findAllWithDetails(Pageable pageable);

    /**
     * 특정 사용자가 특정 공구에 후기를 작성했는지 확인 (삭제되지 않은 것만)
     * uk_review_reviewer_groupbuy UNIQUE 제약 조건 활용
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.reviewer.id = :reviewerId AND r.groupBuy.id = :groupBuyId AND r.deletedAt IS NULL")
    boolean existsByReviewerIdAndGroupBuyId(@Param("reviewerId") Long reviewerId, @Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 사용자가 작성한 후기 목록을 최신순으로 조회 (삭제되지 않은 것만)
     * idx_review_reviewer_id 인덱스 활용
     */
    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Review> findByReviewerIdOrderByCreatedAtDesc(@Param("reviewerId") Long reviewerId);

    /**
     * 특정 공구의 평균 별점 계산 (삭제되지 않은 것만)
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.groupBuy.id = :groupBuyId AND r.deletedAt IS NULL")
    Double findAverageRatingByGroupBuyId(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 공구의 후기 개수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.groupBuy.id = :groupBuyId AND r.deletedAt IS NULL")
    long countByGroupBuyId(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 사용자가 작성한 후기 개수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewer.id = :reviewerId AND r.deletedAt IS NULL")
    long countByReviewerId(@Param("reviewerId") Long reviewerId);

    /**
     * 특정 공구 주최자가 받은 모든 후기 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT r FROM Review r WHERE r.groupBuy.host.id = :hostId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Review> findByGroupBuyHostId(@Param("hostId") Long hostId);

    /**
     * 특정 공구의 평점별 후기 개수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.groupBuy.id = :groupBuyId AND r.deletedAt IS NULL GROUP BY r.rating")
    List<Object[]> countByRatingForGroupBuy(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 사용자가 특정 공구에 삭제된 후기가 있는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.reviewer.id = :reviewerId AND r.groupBuy.id = :groupBuyId AND r.deletedAt IS NOT NULL")
    boolean existsDeletedReviewByReviewerIdAndGroupBuyId(@Param("reviewerId") Long reviewerId, @Param("groupBuyId") Long groupBuyId);
}

