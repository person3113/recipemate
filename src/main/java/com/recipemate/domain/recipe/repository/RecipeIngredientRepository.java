package com.recipemate.domain.recipe.repository;

import com.recipemate.domain.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 레시피 재료 Repository
 */
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    /**
     * 레시피 ID로 재료 목록 조회
     */
    List<RecipeIngredient> findByRecipeId(Long recipeId);

    /**
     * 레시피 ID로 재료 목록 조회 (레시피 함께 조회)
     */
    @Query("SELECT ri FROM RecipeIngredient ri JOIN FETCH ri.recipe WHERE ri.recipe.id = :recipeId")
    List<RecipeIngredient> findByRecipeIdWithRecipe(@Param("recipeId") Long recipeId);

    /**
     * 재료명으로 검색
     */
    @Query("SELECT ri FROM RecipeIngredient ri WHERE LOWER(ri.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RecipeIngredient> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * 특정 재료를 포함하는 레시피 수 조회
     */
    @Query("SELECT COUNT(DISTINCT ri.recipe.id) FROM RecipeIngredient ri WHERE LOWER(ri.name) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    Long countRecipesByIngredientName(@Param("ingredientName") String ingredientName);

    /**
     * 레시피의 모든 재료 삭제
     */
    void deleteByRecipeId(Long recipeId);

    /**
     * 모든 고유 재료명 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT ri.name FROM RecipeIngredient ri ORDER BY ri.name")
    List<String> findDistinctIngredientNames();
}
