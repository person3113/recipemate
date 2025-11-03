package com.recipemate.domain.review.repository;

import com.recipemate.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 공구의 후기 목록을 최신순으로 조회
     * idx_review_group_buy_id 인덱스 활용
     */
    List<Review> findByGroupBuyIdOrderByCreatedAtDesc(Long groupBuyId);

    /**
     * 특정 사용자가 특정 공구에 후기를 작성했는지 확인
     * uk_review_reviewer_groupbuy UNIQUE 제약 조건 활용
     */
    boolean existsByReviewerIdAndGroupBuyId(Long reviewerId, Long groupBuyId);

    /**
     * 특정 사용자가 작성한 후기 목록을 최신순으로 조회
     * idx_review_reviewer_id 인덱스 활용
     */
    List<Review> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId);

    /**
     * 특정 공구의 평균 별점 계산
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.groupBuy.id = :groupBuyId")
    Double findAverageRatingByGroupBuyId(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 공구의 후기 개수 조회
     */
    long countByGroupBuyId(Long groupBuyId);

    /**
     * 특정 사용자가 작성한 후기 개수 조회
     */
    long countByReviewerId(Long reviewerId);

    /**
     * 특정 공구 주최자가 받은 모든 후기 조회
     */
    @Query("SELECT r FROM Review r WHERE r.groupBuy.host.id = :hostId ORDER BY r.createdAt DESC")
    List<Review> findByGroupBuyHostId(@Param("hostId") Long hostId);
}
