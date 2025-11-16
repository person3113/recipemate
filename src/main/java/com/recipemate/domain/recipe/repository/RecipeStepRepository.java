package com.recipemate.domain.recipe.repository;

import com.recipemate.domain.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 조리 단계 Repository
 */
public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    /**
     * 레시피 ID로 조리 단계 목록 조회 (단계 번호 순)
     */
    List<RecipeStep> findByRecipeIdOrderByStepNumberAsc(Long recipeId);

    /**
     * 레시피 ID로 조리 단계 목록 조회 (레시피 함께 조회)
     */
    @Query("SELECT rs FROM RecipeStep rs JOIN FETCH rs.recipe WHERE rs.recipe.id = :recipeId ORDER BY rs.stepNumber ASC")
    List<RecipeStep> findByRecipeIdWithRecipeOrderByStepNumber(@Param("recipeId") Long recipeId);

    /**
     * 레시피의 모든 조리 단계 삭제 (N+1 방지용 - 사용 금지, bulkSoftDelete 사용)
     * @deprecated Use bulkSoftDeleteByRecipeId instead
     */
    @Deprecated
    void deleteByRecipeId(Long recipeId);

    /**
     * 레시피의 모든 조리 단계를 Soft Delete (Bulk UPDATE - 성능 최적화)
     * 단일 쿼리로 처리하여 N+1 문제 방지
     */
    @Modifying
    @Query("UPDATE RecipeStep rs SET rs.deletedAt = CURRENT_TIMESTAMP WHERE rs.recipe.id = :recipeId AND rs.deletedAt IS NULL")
    int bulkSoftDeleteByRecipeId(@Param("recipeId") Long recipeId);

    /**
     * 특정 레시피의 조리 단계 수 조회
     */
    Long countByRecipeId(Long recipeId);

    /**
     * 이미지가 있는 조리 단계 조회
     */
    @Query("SELECT rs FROM RecipeStep rs WHERE rs.recipe.id = :recipeId AND rs.imageUrl IS NOT NULL ORDER BY rs.stepNumber ASC")
    List<RecipeStep> findByRecipeIdWithImageOrderByStepNumber(@Param("recipeId") Long recipeId);
}
