package com.recipemate.domain.recipe.repository;

import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 레시피 Repository
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    /**
     * 외부 API 소스와 ID로 레시피 조회 (중복 방지용)
     */
    Optional<Recipe> findBySourceApiAndSourceApiId(RecipeSource sourceApi, String sourceApiId);

    /**
     * 소스별 레시피 목록 조회
     */
    Page<Recipe> findBySourceApi(RecipeSource sourceApi, Pageable pageable);

    /**
     * 제목으로 검색
     */
    Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 카테고리로 검색
     */
    Page<Recipe> findByCategory(String category, Pageable pageable);

    /**
     * 지역으로 검색
     */
    Page<Recipe> findByArea(String area, Pageable pageable);

    /**
     * 소스와 카테고리로 검색
     */
    Page<Recipe> findBySourceApiAndCategory(RecipeSource sourceApi, String category, Pageable pageable);

    /**
     * 칼로리 범위로 검색
     */
    @Query("SELECT r FROM Recipe r WHERE r.calories IS NOT NULL AND r.calories <= :maxCalories ORDER BY r.calories ASC")
    Page<Recipe> findByCaloriesLessThanEqual(@Param("maxCalories") Integer maxCalories, Pageable pageable);

    /**
     * 재료명으로 레시피 검색
     */
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    Page<Recipe> findByIngredientNameContaining(@Param("ingredientName") String ingredientName, Pageable pageable);

    /**
     * 여러 재료로 레시피 검색
     */
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE LOWER(i.name) IN :ingredientNames")
    Page<Recipe> findByIngredientsIn(@Param("ingredientNames") List<String> ingredientNames, Pageable pageable);

    /**
     * ID로 재료와 함께 조회
     */
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);

    /**
     * ID로 재료와 조리단계와 함께 조회
     * MultipleBagFetchException 방지를 위해 ingredients만 fetch
     * steps는 별도 쿼리로 lazy load
     */
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredientsAndSteps(@Param("id") Long id);
    
    /**
     * ID로 조리단계와 함께 조회 (ingredients 이후 호출용)
     */
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.steps WHERE r.id = :id")
    Optional<Recipe> findByIdWithSteps(@Param("id") Long id);

    /**
     * 소스 API ID로 재료와 함께 조회
     */
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients WHERE r.sourceApi = :sourceApi AND r.sourceApiId = :sourceApiId")
    Optional<Recipe> findBySourceApiAndSourceApiIdWithIngredients(@Param("sourceApi") RecipeSource sourceApi, @Param("sourceApiId") String sourceApiId);

    /**
     * 인기 레시피 조회 (조회수 기준 - 추후 구현 예정)
     */
    Page<Recipe> findByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 소스의 카테고리 목록 조회
     */
    @Query("SELECT DISTINCT r.category FROM Recipe r WHERE r.sourceApi = :sourceApi AND r.category IS NOT NULL ORDER BY r.category")
    List<String> findDistinctCategoriesBySourceApi(@Param("sourceApi") RecipeSource sourceApi);

    /**
     * 특정 소스의 지역 목록 조회
     */
    @Query("SELECT DISTINCT r.area FROM Recipe r WHERE r.sourceApi = :sourceApi AND r.area IS NOT NULL ORDER BY r.area")
    List<String> findDistinctAreasBySourceApi(@Param("sourceApi") RecipeSource sourceApi);
}
