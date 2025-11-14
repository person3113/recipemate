package com.recipemate.domain.recipewishlist.repository;

import com.recipemate.domain.recipewishlist.entity.RecipeWishlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeWishlistRepository extends JpaRepository<RecipeWishlist, Long> {

    Optional<RecipeWishlist> findByUserIdAndRecipeId(Long userId, Long recipeId);

    List<RecipeWishlist> findByUserIdOrderByWishedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    List<RecipeWishlist> findByRecipeId(Long recipeId);

    /**
     * 삭제된 레시피를 포함하여 찜 목록 조회 (찜 취소 시 사용)
     */
    @Query(value = "SELECT * FROM recipe_wishlists WHERE user_id = :userId AND recipe_id = :recipeId", nativeQuery = true)
    Optional<RecipeWishlist> findByUserIdAndRecipeIdIncludingDeleted(@Param("userId") Long userId, @Param("recipeId") Long recipeId);
}
