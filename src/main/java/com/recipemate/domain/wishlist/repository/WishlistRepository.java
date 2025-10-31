package com.recipemate.domain.wishlist.repository;

import com.recipemate.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserIdAndGroupBuyId(Long userId, Long groupBuyId);

    List<Wishlist> findByUserIdOrderByWishedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndGroupBuyId(Long userId, Long groupBuyId);
}
