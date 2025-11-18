package com.recipemate.domain.wishlist.repository;

import com.recipemate.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserIdAndGroupBuyId(Long userId, Long groupBuyId);

    List<Wishlist> findByUserIdOrderByWishedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndGroupBuyId(Long userId, Long groupBuyId);

    List<Wishlist> findByGroupBuyId(Long groupBuyId);

    /**
     * 삭제되지 않은 공구만 포함하여 찜목록 조회
     * 취소된 공구(CANCELLED)는 포함, 삭제된 공구(deletedAt != null)는 제외
     */
    @Query("SELECT w FROM Wishlist w " +
           "JOIN FETCH w.groupBuy gb " +
           "JOIN FETCH gb.host " +
           "WHERE w.user.id = :userId " +
           "AND gb.deletedAt IS NULL " +
           "ORDER BY w.wishedAt DESC")
    List<Wishlist> findByUserIdWithNonDeletedGroupBuys(@Param("userId") Long userId, Pageable pageable);

    /**
     * 삭제되지 않은 공구의 찜 개수 조회
     */
    @Query("SELECT COUNT(w) FROM Wishlist w " +
           "WHERE w.user.id = :userId " +
           "AND w.groupBuy.deletedAt IS NULL")
    long countByUserIdWithNonDeletedGroupBuys(@Param("userId") Long userId);
}
