package com.recipemate.domain.recipewishlist.repository;

import com.recipemate.domain.recipewishlist.entity.RecipeWishlist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeWishlistRepository extends JpaRepository<RecipeWishlist, Long> {

    Optional<RecipeWishlist> findByUserIdAndRecipeId(Long userId, Long recipeId);

    List<RecipeWishlist> findByUserIdOrderByWishedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    List<RecipeWishlist> findByRecipeId(Long recipeId);
}
