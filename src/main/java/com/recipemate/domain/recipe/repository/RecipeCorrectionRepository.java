package com.recipemate.domain.recipe.repository;

import com.recipemate.domain.recipe.entity.CorrectionStatus;
import com.recipemate.domain.recipe.entity.RecipeCorrection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeCorrectionRepository extends JpaRepository<RecipeCorrection, Long> {

    /**
     * 상태별 제안 조회 (최신순, 페이징)
     */
    Page<RecipeCorrection> findByStatusOrderByCreatedAtDesc(CorrectionStatus status, Pageable pageable);

    /**
     * PENDING 상태의 모든 제안 조회 (관리자용)
     */
    @Query("SELECT rc FROM RecipeCorrection rc " +
            "WHERE rc.status = 'PENDING' " +
            "ORDER BY rc.createdAt DESC")
    List<RecipeCorrection> findAllPending();

    /**
     * 특정 레시피에 대한 제안 조회
     */
    @Query("SELECT rc FROM RecipeCorrection rc " +
            "WHERE rc.recipe.id = :recipeId " +
            "ORDER BY rc.createdAt DESC")
    List<RecipeCorrection> findByRecipeId(@Param("recipeId") Long recipeId);

    /**
     * 특정 사용자가 제출한 제안 조회
     */
    @Query("SELECT rc FROM RecipeCorrection rc " +
            "WHERE rc.proposer.id = :proposerId " +
            "ORDER BY rc.createdAt DESC")
    Page<RecipeCorrection> findByProposerId(@Param("proposerId") Long proposerId, Pageable pageable);

    /**
     * 특정 레시피에 대한 사용자의 PENDING 제안 개수
     */
    @Query("SELECT COUNT(rc) FROM RecipeCorrection rc " +
            "WHERE rc.recipe.id = :recipeId " +
            "AND rc.proposer.id = :proposerId " +
            "AND rc.status = 'PENDING'")
    long countPendingByRecipeAndProposer(@Param("recipeId") Long recipeId, @Param("proposerId") Long proposerId);
}
