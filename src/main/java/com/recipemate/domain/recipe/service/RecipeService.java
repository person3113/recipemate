package com.recipemate.domain.recipe.service;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.recipe.client.FoodSafetyClient;
import com.recipemate.domain.recipe.client.TheMealDBClient;
import com.recipemate.domain.recipe.dto.*;
import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeIngredient;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipe.entity.RecipeStep;
import com.recipemate.domain.recipe.repository.RecipeIngredientRepository;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.config.CacheConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 레시피 서비스
 * TheMealDB와 식품안전나라 API를 통합하여 레시피 정보를 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final TheMealDBClient theMealDBClient;
    private final FoodSafetyClient foodSafetyClient;
    private final GroupBuyRepository groupBuyRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final JPAQueryFactory queryFactory;
    private final com.recipemate.domain.review.repository.ReviewRepository reviewRepository;
    private final com.recipemate.global.util.ImageUploadUtil imageUploadUtil;

    private static final String MEAL_PREFIX = "meal-";
    private static final String FOOD_PREFIX = "food-";
    private static final int MAX_RANDOM_COUNT = 100;
    private static final int FOOD_SAFETY_SEARCH_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 레시피 검색
     * 두 API에서 검색 후 결과를 통합하여 반환
     * @deprecated DB 기반 검색 메서드로 대체됨 (findRecipes)
     * @param keyword 검색어
     * @return 통합된 레시피 목록
     */
    @Deprecated
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'search:' + #keyword")
    public RecipeListResponse searchRecipes(String keyword) {
        validateKeyword(keyword);

        // 두 API에서 동시에 검색
        List<MealResponse> mealResults = theMealDBClient.searchRecipes(keyword);
        List<CookRecipeResponse> foodResults = foodSafetyClient.searchRecipesByName(keyword, 1, FOOD_SAFETY_SEARCH_SIZE);

        // 결과 통합
        List<RecipeListResponse.RecipeSimpleInfo> recipes = new ArrayList<>();
        
        // TheMealDB 결과 변환
        recipes.addAll(mealResults.stream()
                .map(this::convertMealToSimpleInfo)
                .collect(Collectors.toList()));
        
        // 식품안전나라 결과 변환
        recipes.addAll(foodResults.stream()
                .map(this::convertCookRecipeToSimpleInfo)
                .collect(Collectors.toList()));

        // 출처 결정
        String source = determineSource(mealResults.isEmpty(), foodResults.isEmpty());

        return RecipeListResponse.builder()
                .recipes(recipes)
                .totalCount(recipes.size())
                .source(source)
                .build();
    }

    /**
     * 레시피 상세 조회
     * API ID 형식에 따라 적절한 API를 호출하여 상세 정보 반환
     * @param apiId API ID (meal-{id} 또는 food-{id} 형식)
     * @return 레시피 상세 정보
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'detail:' + #apiId")
    public RecipeDetailResponse getRecipeDetail(String apiId) {
        validateApiId(apiId);

        if (apiId.startsWith(MEAL_PREFIX)) {
            // TheMealDB 레시피 조회
            String mealId = apiId.substring(MEAL_PREFIX.length());
            MealResponse meal = theMealDBClient.getRecipeById(mealId);
            
            if (meal == null) {
                throw new CustomException(ErrorCode.RECIPE_NOT_FOUND);
            }
            
            return convertMealToDetailResponse(meal);
            
        } else if (apiId.startsWith(FOOD_PREFIX)) {
            // 식품안전나라 레시피 조회
            String rcpSeq = apiId.substring(FOOD_PREFIX.length());
            
            // RCP_SEQ로 레시피 조회 (FoodSafetyClient의 getRecipeBySeq 메서드 사용)
            CookRecipeResponse recipe = foodSafetyClient.getRecipeBySeq(rcpSeq);
            
            if (recipe == null) {
                throw new CustomException(ErrorCode.RECIPE_NOT_FOUND);
            }
            
            return convertCookRecipeToDetailResponse(recipe);
            
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 랜덤 레시피 조회 (DB 기반)
     * DB에 저장된 레시피 중에서 랜덤으로 조회
     * 매번 새로운 랜덤 레시피를 제공하기 위해 캐싱하지 않음
     * @param count 조회할 레시피 개수
     * @return 랜덤 레시피 목록
     */
    public RecipeListResponse getRandomRecipes(int count) {
        validateRandomCount(count);
        
        log.info("DB 기반 랜덤 레시피 조회 요청: count={}", count);
        
        // 전체 레시피 개수 조회
        long totalCount = recipeRepository.count();
        
        if (totalCount == 0) {
            log.warn("DB에 레시피가 없습니다");
            return RecipeListResponse.builder()
                    .recipes(List.of())
                    .totalCount(0)
                    .source("database")
                    .build();
        }
        
        // count가 전체 개수보다 크면 전체 개수로 제한
        int actualCount = (int) Math.min(count, totalCount);
        
        // QueryDSL로 랜덤 레시피 조회
        // H2 데이터베이스의 RANDOM() 함수 사용
        com.recipemate.domain.recipe.entity.QRecipe recipe = 
            com.recipemate.domain.recipe.entity.QRecipe.recipe;
        
        List<Recipe> randomRecipes = queryFactory
                .selectFrom(recipe)
                .orderBy(com.querydsl.core.types.dsl.Expressions.numberTemplate(Double.class, "RANDOM()").asc())
                .limit(actualCount)
                .fetch();
        
        // RecipeSimpleInfo로 변환
        List<RecipeListResponse.RecipeSimpleInfo> recipes = randomRecipes.stream()
                .map(this::convertRecipeEntityToSimpleInfo)
                .collect(Collectors.toList());
        
        return RecipeListResponse.builder()
                .recipes(recipes)
                .totalCount(recipes.size())
                .source("database")
                .build();
    }

    /**
     * 카테고리 목록 조회 (DB 기반)
     * DB에 저장된 레시피의 카테고리 목록 반환
     * @return 카테고리 목록
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'categories'")
    public List<CategoryResponse> getCategories() {
        log.info("DB 기반 카테고리 목록 조회");
        
        // QueryDSL로 카테고리별 그룹화 및 집계
        com.recipemate.domain.recipe.entity.QRecipe recipe = 
            com.recipemate.domain.recipe.entity.QRecipe.recipe;
        
        // 카테고리가 null이 아닌 레시피만 조회하고 distinct 카테고리 추출
        List<String> categories = queryFactory
                .select(recipe.category)
                .from(recipe)
                .where(recipe.category.isNotNull())
                .distinct()
                .orderBy(recipe.category.asc())
                .fetch();
        
        // CategoryResponse로 변환 (thumbnail과 description은 null)
        return categories.stream()
                .map(cat -> new CategoryResponse(null, cat, null, null))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 레시피 조회
     * 특정 카테고리에 속한 레시피 목록 반환
     * @param category 카테고리 이름
     * @return 해당 카테고리의 레시피 목록
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'category:' + #category")
    public RecipeListResponse getRecipesByCategory(String category) {
        validateCategory(category);

        List<MealResponse> meals = theMealDBClient.getRecipesByCategory(category);
        
        List<RecipeListResponse.RecipeSimpleInfo> recipes = meals.stream()
                .map(this::convertMealToSimpleInfo)
                .collect(Collectors.toList());

        return RecipeListResponse.builder()
                .recipes(recipes)
                .totalCount(recipes.size())
                .source("themealdb")
                .build();
    }

    /**
     * 레시피 관련 공동구매 조회
     * 특정 레시피 ID와 연결된 모든 공동구매 목록 반환 (평점 정보 포함)
     * @param recipeApiId 레시피 API ID (meal-{id} 또는 food-{id} 형식)
     * @return 모든 상태의 공동구매 목록 (삭제되지 않은 것만, 평점 정보 포함)
     */
    public List<GroupBuyResponse> getRelatedGroupBuys(String recipeApiId) {
        validateRecipeApiId(recipeApiId);

        // 레시피 ID로 삭제되지 않은 공동구매 조회
        List<GroupBuy> groupBuys = groupBuyRepository.findByRecipeApiIdAndNotDeleted(recipeApiId);

        // 모든 상태의 공동구매를 변환 (마감된 공구의 평점도 표시)
        return groupBuys.stream()
                .map(gb -> {
                    // 이미지 URL 목록 수집
                    List<String> imageUrls = gb.getImages().stream()
                            .map(img -> img.getImageUrl())
                            .collect(Collectors.toList());
                    
                    // 후기 정보 조회
                    Double averageRating = reviewRepository.findAverageRatingByGroupBuyId(gb.getId());
                    long reviewCount = reviewRepository.countByGroupBuyId(gb.getId());
                    
                    return GroupBuyResponse.builder()
                            .id(gb.getId())
                            .title(gb.getTitle())
                            .content(gb.getContent())
                            .ingredients(gb.getIngredients())
                            .category(gb.getCategory())
                            .totalPrice(gb.getTotalPrice())
                            .targetHeadcount(gb.getTargetHeadcount())
                            .currentHeadcount(gb.getCurrentHeadcount())
                            .deadline(gb.getDeadline())
                            .deliveryMethod(gb.getDeliveryMethod())
                            .meetupLocation(gb.getMeetupLocation())
                            .parcelFee(gb.getParcelFee())
                            .isParticipantListPublic(gb.getParticipantListPublic())
                            .status(gb.getStatus())
                            .hostId(gb.getHost().getId())
                            .hostNickname(gb.getHost().getNickname())
                            .hostMannerTemperature(gb.getHost().getMannerTemperature())
                            .recipeApiId(gb.getRecipeApiId())
                            .recipeName(gb.getRecipeName())
                            .recipeImageUrl(gb.getRecipeImageUrl())
                            .imageUrls(imageUrls)
                            .averageRating(averageRating)
                            .reviewCount((int) reviewCount)
                            .createdAt(gb.getCreatedAt())
                            .updatedAt(gb.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== DB 기반 레시피 조회 메서드 (신규) ==========

    /**
     * 통합 레시피 검색 (DB 기반)
     * 여러 필터 조건을 조합하여 레시피 검색
     * 
     * @param keyword 검색어 (제목 검색)
     * @param ingredients 재료명 리스트 (여러 재료 OR 조건)
     * @param category 카테고리
     * @param sort 정렬 기준 (latest, name, popularity)
     * @param direction 정렬 방향 (asc, desc)
     * @param pageable 페이징 정보
     * @return 검색된 레시피 목록
     */
    public RecipeListResponse findRecipes(
            String keyword,
            List<String> ingredients,
            String category,
            String sort,
            String direction,
            Pageable pageable) {
        
        log.info("DB 기반 레시피 통합 검색: keyword={}, ingredients={}, category={}, sort={}", 
                 keyword, ingredients, category, sort);

        // QueryDSL을 사용한 동적 쿼리 생성
        com.recipemate.domain.recipe.entity.QRecipe recipe = 
            com.recipemate.domain.recipe.entity.QRecipe.recipe;
        com.recipemate.domain.recipe.entity.QRecipeIngredient recipeIngredient = 
            com.recipemate.domain.recipe.entity.QRecipeIngredient.recipeIngredient;
        com.recipemate.domain.groupbuy.entity.QGroupBuy groupBuy = 
            com.recipemate.domain.groupbuy.entity.QGroupBuy.groupBuy;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 키워드 검색 (제목에 포함)
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(recipe.title.toLowerCase().contains(keyword.toLowerCase()));
        }
        
        // 재료 검색 (OR 조건)
        boolean hasIngredients = ingredients != null && !ingredients.isEmpty();
        if (hasIngredients) {
            BooleanBuilder ingredientBuilder = new BooleanBuilder();
            for (String ingredient : ingredients) {
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    ingredientBuilder.or(recipeIngredient.name.toLowerCase().contains(ingredient.trim().toLowerCase()));
                }
            }
            if (ingredientBuilder.hasValue()) {
                builder.and(ingredientBuilder);
            }
        }
        
        // 카테고리 필터
        if (category != null && !category.trim().isEmpty()) {
            builder.and(recipe.category.eq(category));
        }
        
        // 정렬 기준 결정
        JPAQuery<Recipe> query;
        
        // direction 파라미터를 Sort.Direction으로 변환
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        // popularity 정렬인 경우 GroupBuy 테이블과 조인
        if ("popularity".equals(sort)) {
            // 인기순은 항상 count 기준, direction에 따라 오름차순/내림차순 결정
            com.querydsl.core.types.OrderSpecifier<?> popularityOrder = 
                sortDirection == Sort.Direction.ASC 
                    ? groupBuy.id.count().asc() 
                    : groupBuy.id.count().desc();
            
            query = queryFactory
                    .select(recipe)
                    .from(recipe)
                    .leftJoin(groupBuy).on(groupBuy.recipeApiId.eq(
                        com.querydsl.core.types.dsl.Expressions.stringTemplate(
                            "CASE WHEN {0} = 'MEAL_DB' THEN CONCAT('meal-', {1}) ELSE CONCAT('food-', {1}) END",
                            recipe.sourceApi, recipe.sourceApiId
                        )
                    ).and(groupBuy.deletedAt.isNull()))
                    .where(builder)
                    .groupBy(recipe.id)
                    .orderBy(popularityOrder, recipe.lastSyncedAt.desc());
            
            // 재료 검색이 있으면 조인 추가 (distinct 제거 - GROUP BY로 중복 제거됨)
            if (hasIngredients) {
                query = queryFactory
                        .select(recipe)
                        .from(recipe)
                        .join(recipe.ingredients, recipeIngredient)
                        .leftJoin(groupBuy).on(groupBuy.recipeApiId.eq(
                            com.querydsl.core.types.dsl.Expressions.stringTemplate(
                                "CASE WHEN {0} = 'MEAL_DB' THEN CONCAT('meal-', {1}) ELSE CONCAT('food-', {1}) END",
                                recipe.sourceApi, recipe.sourceApiId
                            )
                        ).and(groupBuy.deletedAt.isNull()))
                        .where(builder)
                        .groupBy(recipe.id)
                        .orderBy(popularityOrder, recipe.lastSyncedAt.desc());
            }
        } else if ("name".equals(sort)) {
            // 이름순: direction에 따라 오름차순/내림차순
            com.querydsl.core.types.OrderSpecifier<?> nameOrder = 
                sortDirection == Sort.Direction.ASC 
                    ? recipe.title.asc() 
                    : recipe.title.desc();
            
            query = queryFactory
                    .selectFrom(recipe)
                    .where(builder)
                    .orderBy(nameOrder);
            
            // 재료 검색이 있으면 조인 추가 및 distinct 적용
            if (hasIngredients) {
                query = queryFactory
                        .selectFrom(recipe)
                        .distinct()
                        .join(recipe.ingredients, recipeIngredient)
                        .where(builder)
                        .orderBy(nameOrder);
            }
        } else {
            // 기본값: latest (최신순): direction에 따라 오름차순/내림차순
            com.querydsl.core.types.OrderSpecifier<?> dateOrder = 
                sortDirection == Sort.Direction.ASC 
                    ? recipe.lastSyncedAt.asc() 
                    : recipe.lastSyncedAt.desc();
            
            query = queryFactory
                    .selectFrom(recipe)
                    .where(builder)
                    .orderBy(dateOrder);
            
            // 재료 검색이 있으면 조인 추가 및 distinct 적용
            if (hasIngredients) {
                query = queryFactory
                        .selectFrom(recipe)
                        .distinct()
                        .join(recipe.ingredients, recipeIngredient)
                        .where(builder)
                        .orderBy(dateOrder);
            }
        }
        
        // 페이징 적용
        List<Recipe> recipes = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 전체 개수 조회
        Long totalCount;
        if (hasIngredients) {
            totalCount = queryFactory
                    .select(recipe.countDistinct())
                    .from(recipe)
                    .join(recipe.ingredients, recipeIngredient)
                    .where(builder)
                    .fetchOne();
        } else {
            totalCount = queryFactory
                    .select(recipe.count())
                    .from(recipe)
                    .where(builder)
                    .fetchOne();
        }
        
        // DTO 변환
        List<RecipeListResponse.RecipeSimpleInfo> recipeInfos = recipes.stream()
                .map(this::convertRecipeEntityToSimpleInfo)
                .collect(Collectors.toList());
        
        return RecipeListResponse.builder()
                .recipes(recipeInfos)
                .totalCount(totalCount != null ? totalCount.intValue() : 0)
                .source("all")
                .build();
    }

    /**
     * 레시피 개수만 조회 (COUNT 쿼리만 실행)
     * 통합 검색에서 배지 표시용으로 사용
     * 
     * @param keyword 검색어 (제목 검색)
     * @return 검색된 레시피 개수
     */
    public long countRecipes(String keyword) {
        log.info("레시피 개수 조회: keyword={}", keyword);

        // QueryDSL을 사용한 동적 쿼리 생성
        com.recipemate.domain.recipe.entity.QRecipe recipe = 
            com.recipemate.domain.recipe.entity.QRecipe.recipe;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 키워드 검색 (제목에 포함)
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(recipe.title.toLowerCase().contains(keyword.toLowerCase()));
        }
        
        // COUNT 쿼리만 실행
        Long count = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(builder)
                .fetchOne();
        
        return (count != null) ? count : 0L;
    }

    /**
     * 영양 정보 기반 레시피 검색 (DB 기반)
     * 
     * @param maxCalories 최대 열량
     * @param maxCarbohydrate 최대 탄수화물
     * @param maxProtein 최대 단백질
     * @param maxFat 최대 지방
     * @param maxSodium 최대 나트륨
     * @param pageable 페이징 정보
     * @return 검색된 레시피 목록
     */
    public RecipeListResponse findRecipesByNutrition(
            Integer maxCalories,
            Integer maxCarbohydrate,
            Integer maxProtein,
            Integer maxFat,
            Integer maxSodium,
            Pageable pageable) {
        
        log.info("영양정보 기반 레시피 검색: calories<={}, carbs<={}, protein<={}, fat<={}, sodium<={}",
                 maxCalories, maxCarbohydrate, maxProtein, maxFat, maxSodium);
        
        // QueryDSL을 사용한 동적 쿼리 생성
        com.recipemate.domain.recipe.entity.QRecipe recipe = 
            com.recipemate.domain.recipe.entity.QRecipe.recipe;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // 영양 정보가 null이 아닌 레시피만 검색
        // (TheMealDB는 영양 정보가 없으므로 자동으로 제외됨)
        
        if (maxCalories != null && maxCalories > 0) {
            builder.and(recipe.calories.isNotNull())
                   .and(recipe.calories.loe(maxCalories));
        }
        
        if (maxCarbohydrate != null && maxCarbohydrate > 0) {
            builder.and(recipe.carbohydrate.isNotNull())
                   .and(recipe.carbohydrate.loe(maxCarbohydrate));
        }
        
        if (maxProtein != null && maxProtein > 0) {
            builder.and(recipe.protein.isNotNull())
                   .and(recipe.protein.loe(maxProtein));
        }
        
        if (maxFat != null && maxFat > 0) {
            builder.and(recipe.fat.isNotNull())
                   .and(recipe.fat.loe(maxFat));
        }
        
        if (maxSodium != null && maxSodium > 0) {
            builder.and(recipe.sodium.isNotNull())
                   .and(recipe.sodium.loe(maxSodium));
        }
        
        // 필터가 없으면 영양 정보가 있는 모든 레시피 반환
        if (!builder.hasValue()) {
            builder.and(recipe.calories.isNotNull());
        }
        
        // 페이징 쿼리 (칼로리 낮은 순)
        List<Recipe> recipes = queryFactory
                .selectFrom(recipe)
                .where(builder)
                .orderBy(recipe.calories.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        // 전체 개수 조회
        Long totalCount = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(builder)
                .fetchOne();
        
        // DTO 변환
        List<RecipeListResponse.RecipeSimpleInfo> recipeInfos = recipes.stream()
                .map(this::convertRecipeEntityToSimpleInfo)
                .collect(Collectors.toList());
        
        return RecipeListResponse.builder()
                .recipes(recipeInfos)
                .totalCount(totalCount != null ? totalCount.intValue() : 0)
                .source("foodsafety") // 영양 정보는 식품안전나라만 제공
                .build();
    }

    /**
     * DB ID로 레시피 상세 조회
     * DB에 있으면 DB에서 조회, 없으면 외부 API 호출 후 저장
     * 
     * @param recipeId 레시피 DB ID
     * @return 레시피 상세 정보
     */
    public RecipeDetailResponse getRecipeDetailById(Long recipeId) {
        // MultipleBagFetchException 방지: ingredients와 steps를 두 단계로 조회
        Recipe recipe = recipeRepository.findByIdWithIngredientsAndSteps(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
        
        // steps를 명시적으로 초기화 (lazy load)
        // 이미 영속성 컨텍스트에 있으므로 steps 접근 시 자동 로드됨
        recipe.getSteps().size(); // lazy initialization trigger
        
        return convertRecipeEntityToDetailResponse(recipe);
    }

    /**
     * API ID로 레시피 상세 조회 (DB 우선)
     * DB에 있으면 DB에서 조회, 없으면 외부 API 호출
     * 
     * @param apiId API ID (meal-{id} 또는 food-{id} 형식)
     * @return 레시피 상세 정보
     */
    public RecipeDetailResponse getRecipeDetailByApiId(String apiId) {
        validateApiId(apiId);
        
        // DB에서 먼저 조회 시도
        RecipeSource source;
        String sourceApiId;
        
        if (apiId.startsWith(MEAL_PREFIX)) {
            source = RecipeSource.MEAL_DB;
            sourceApiId = apiId.substring(MEAL_PREFIX.length());
        } else if (apiId.startsWith(FOOD_PREFIX)) {
            source = RecipeSource.FOOD_SAFETY;
            sourceApiId = apiId.substring(FOOD_PREFIX.length());
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // DB에서 조회 시도
        return recipeRepository.findBySourceApiAndSourceApiIdWithIngredients(source, sourceApiId)
                .map(this::convertRecipeEntityToDetailResponse)
                .orElseGet(() -> {
                    // DB에 없으면 기존 API 호출 방식 사용 (fallback)
                    log.warn("레시피가 DB에 없어 API를 직접 호출합니다: {}", apiId);
                    return getRecipeDetail(apiId);
                });
    }

    // ========== 변환 메서드 ==========

    /**
     * Recipe 엔티티를 RecipeSimpleInfo로 변환
     */
    private RecipeListResponse.RecipeSimpleInfo convertRecipeEntityToSimpleInfo(Recipe recipe) {
        // API ID 결정 (사용자 레시피는 DB ID 사용)
        String apiId;
        if (recipe.getSourceApi() == RecipeSource.USER) {
            apiId = String.valueOf(recipe.getId());  // 사용자 레시피는 DB ID
        } else if (recipe.getSourceApi() == RecipeSource.MEAL_DB) {
            apiId = MEAL_PREFIX + recipe.getSourceApiId();
        } else {
            apiId = FOOD_PREFIX + recipe.getSourceApiId();
        }

        String imageUrl = recipe.getThumbnailImageUrl() != null 
                ? recipe.getThumbnailImageUrl() 
                : recipe.getFullImageUrl();
        
        // 출처 결정: 사용자 레시피는 작성자 닉네임, API 레시피는 소스명
        String source;
        if (recipe.getSourceApi() == RecipeSource.USER && recipe.getAuthor() != null) {
            source = recipe.getAuthor().getNickname();
        } else {
            source = recipe.getSourceApi().name().toLowerCase();
        }

        return RecipeListResponse.RecipeSimpleInfo.builder()
                .id(apiId)
                .name(recipe.getTitle())
                .imageUrl(imageUrl)
                .category(recipe.getCategory())
                .source(source)
                .build();
    }
    /**
     * Recipe 엔티티를 RecipeDetailResponse로 변환
     */
    private RecipeDetailResponse convertRecipeEntityToDetailResponse(Recipe recipe) {
        // API ID 결정 (사용자 레시피는 DB ID 사용)
        String apiId;
        if (recipe.getSourceApi() == RecipeSource.USER) {
            apiId = String.valueOf(recipe.getId());
        } else if (recipe.getSourceApi() == RecipeSource.MEAL_DB) {
            apiId = MEAL_PREFIX + recipe.getSourceApiId();
        } else {
            apiId = FOOD_PREFIX + recipe.getSourceApiId();
        }

        // 재료 정보 변환
        List<RecipeDetailResponse.IngredientInfo> ingredients = recipe.getIngredients().stream()
                .map(ing -> RecipeDetailResponse.IngredientInfo.builder()
                        .name(ing.getName())
                        .measure(ing.getMeasure())
                        .build())
                .collect(Collectors.toList());
        
        // 조리 단계 변환
        List<RecipeDetailResponse.ManualStep> manualSteps = recipe.getSteps().stream()
                .sorted((a, b) -> a.getStepNumber().compareTo(b.getStepNumber()))
                .map(step -> RecipeDetailResponse.ManualStep.builder()
                        .stepNumber(step.getStepNumber())
                        .description(step.getDescription())
                        .imageUrl(step.getImageUrl())
                        .build())
                .collect(Collectors.toList());
        
        // 영양 정보 변환 (있는 경우만)
        RecipeDetailResponse.NutritionInfo nutritionInfo = null;
        if (recipe.getCalories() != null) {
            nutritionInfo = RecipeDetailResponse.NutritionInfo.builder()
                    .weight(recipe.getServingSize())
                    .energy(recipe.getCalories() != null ? recipe.getCalories().toString() : null)
                    .carbohydrate(recipe.getCarbohydrate() != null ? recipe.getCarbohydrate().toString() : null)
                    .protein(recipe.getProtein() != null ? recipe.getProtein().toString() : null)
                    .fat(recipe.getFat() != null ? recipe.getFat().toString() : null)
                    .sodium(recipe.getSodium() != null ? recipe.getSodium().toString() : null)
                    .build();
        }
        
        // 관련 공동구매 목록 조회 (평점 정보 포함)
        List<GroupBuyResponse> relatedGroupBuys = getRelatedGroupBuys(apiId);
        
        // 출처 결정: 사용자 레시피는 작성자 닉네임, API 레시피는 소스명
        String source;
        if (recipe.getSourceApi() == RecipeSource.USER && recipe.getAuthor() != null) {
            source = recipe.getAuthor().getNickname() + " (사용자)";
        } else {
            source = recipe.getSourceApi().name().toLowerCase();
        }

        return RecipeDetailResponse.builder()
                .id(apiId)
                .name(recipe.getTitle())
                .imageUrl(recipe.getFullImageUrl() != null ? recipe.getFullImageUrl() : recipe.getThumbnailImageUrl())
                .category(recipe.getCategory())
                .area(recipe.getArea())
                .instructions(recipe.getInstructions()) // 사용자 레시피는 instructions 필드 사용
                .youtubeUrl(recipe.getYoutubeUrl())
                .sourceUrl(recipe.getSourceUrl())
                .ingredients(ingredients)
                .manualSteps(manualSteps.isEmpty() ? null : manualSteps)
                .nutritionInfo(nutritionInfo)
                .source(source)
                .relatedGroupBuys(relatedGroupBuys)
                .build();
    }

    /**
     * MealResponse를 RecipeSimpleInfo로 변환
     */
    private RecipeListResponse.RecipeSimpleInfo convertMealToSimpleInfo(MealResponse meal) {
        return RecipeListResponse.RecipeSimpleInfo.builder()
                .id(MEAL_PREFIX + meal.getId())
                .name(meal.getName())
                .imageUrl(meal.getThumbnail())
                .category(meal.getCategory())
                .source("themealdb")
                .build();
    }

    /**
     * CookRecipeResponse를 RecipeSimpleInfo로 변환
     */
    private RecipeListResponse.RecipeSimpleInfo convertCookRecipeToSimpleInfo(CookRecipeResponse recipe) {
        return RecipeListResponse.RecipeSimpleInfo.builder()
                .id(FOOD_PREFIX + recipe.getRcpSeq())
                .name(recipe.getRcpNm())
                .imageUrl(recipe.getAttFileNoMain())
                .category(recipe.getRcpPat2())
                .source("foodsafety")
                .build();
    }

    /**
     * MealResponse를 RecipeDetailResponse로 변환
     */
    private RecipeDetailResponse convertMealToDetailResponse(MealResponse meal) {
        // 재료 정보 변환
        List<RecipeDetailResponse.IngredientInfo> ingredients = meal.getIngredients().stream()
                .map(ing -> RecipeDetailResponse.IngredientInfo.builder()
                        .name(ing.getName())
                        .measure(ing.getMeasure())
                        .build())
                .collect(Collectors.toList());

        return RecipeDetailResponse.builder()
                .id(MEAL_PREFIX + meal.getId())
                .name(meal.getName())
                .imageUrl(meal.getThumbnail())
                .category(meal.getCategory())
                .area(meal.getArea())
                .instructions(meal.getInstructions())
                .youtubeUrl(meal.getYoutubeUrl())
                .sourceUrl(meal.getSourceUrl())
                .ingredients(ingredients)
                .manualSteps(null) // TheMealDB는 단계별 이미지가 없음
                .nutritionInfo(null) // TheMealDB는 영양 정보가 없음
                .source("themealdb")
                .build();
    }

    /**
     * CookRecipeResponse를 RecipeDetailResponse로 변환
     */
    private RecipeDetailResponse convertCookRecipeToDetailResponse(CookRecipeResponse recipe) {
        // 재료 정보 변환
        List<RecipeDetailResponse.IngredientInfo> ingredients = recipe.getIngredients().stream()
                .map(ing -> RecipeDetailResponse.IngredientInfo.builder()
                        .name(ing)
                        .measure("") // 식품안전나라는 재료 분량 정보가 별도로 없음
                        .build())
                .collect(Collectors.toList());

        // 조리 단계 변환
        List<RecipeDetailResponse.ManualStep> manualSteps = recipe.getManualSteps().stream()
                .map(step -> RecipeDetailResponse.ManualStep.builder()
                        .stepNumber(step.getStepNumber())
                        .description(step.getDescription())
                        .imageUrl(step.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        // 영양 정보 변환
        RecipeDetailResponse.NutritionInfo nutritionInfo = RecipeDetailResponse.NutritionInfo.builder()
                .weight(recipe.getInfoWgt())
                .energy(recipe.getInfoEng())
                .carbohydrate(recipe.getInfoCar())
                .protein(recipe.getInfoPro())
                .fat(recipe.getInfoFat())
                .sodium(recipe.getInfoNa())
                .build();

        return RecipeDetailResponse.builder()
                .id(FOOD_PREFIX + recipe.getRcpSeq())
                .name(recipe.getRcpNm())
                .imageUrl(recipe.getAttFileNoMain())
                .category(recipe.getRcpPat2())
                .area(null) // 식품안전나라는 지역 정보가 없음
                .instructions(null) // 단계별로 나뉘어 있음
                .youtubeUrl(null) // 식품안전나라는 유튜브 링크가 없음
                .sourceUrl(null) // 식품안전나라는 소스 URL이 없음
                .ingredients(ingredients)
                .manualSteps(manualSteps)
                .nutritionInfo(nutritionInfo)
                .source("foodsafety")
                .build();
    }

    // ========== 유효성 검증 메서드 ==========

    /**
     * 검색어 유효성 검증
     */
    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * API ID 유효성 검증
     */
    private void validateApiId(String apiId) {
        if (apiId == null || apiId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        if (!apiId.startsWith(MEAL_PREFIX) && !apiId.startsWith(FOOD_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 랜덤 레시피 개수 유효성 검증
     */
    private void validateRandomCount(int count) {
        if (count < 0 || count > MAX_RANDOM_COUNT) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 레시피 API ID 유효성 검증
     */
    private void validateRecipeApiId(String recipeApiId) {
        if (recipeApiId == null || recipeApiId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 카테고리 유효성 검증
     */
    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }


    /**
     * 검색 결과 출처 결정
     */
    private String determineSource(boolean mealEmpty, boolean foodEmpty) {
        if (mealEmpty && foodEmpty) {
            return "both";
        } else if (mealEmpty) {
            return "foodsafety";
        } else if (foodEmpty) {
            return "themealdb";
        } else {
            return "both";
        }
    }

    // ========== 사용자 레시피 CRUD ==========

    /**
     * 사용자 레시피 생성
     */
    @Transactional
    public RecipeDetailResponse createUserRecipe(RecipeCreateRequest request, com.recipemate.domain.user.entity.User currentUser) {
        // 1. 대표 이미지 업로드 (Cloudinary)
        String mainImageUrl = null;
        if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
            List<String> uploadedUrls = imageUploadUtil.uploadImages(List.of(request.getMainImage()));
            if (!uploadedUrls.isEmpty()) {
                mainImageUrl = uploadedUrls.get(0);
            }
        }

        // 2. Recipe 엔티티 생성 (간소화 버전)
        Recipe recipe = Recipe.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .area(request.getArea())
                .fullImageUrl(mainImageUrl)
                .thumbnailImageUrl(mainImageUrl)  // 같은 이미지 사용
                .sourceApi(RecipeSource.USER)
                .author(currentUser)  // 중요!
                .instructions(null)  // 사용자 레시피는 instructions 사용 안 함
                .tips(request.getTips())
                .youtubeUrl(request.getYoutubeUrl())
                .sourceUrl(request.getSourceUrl())
                .lastSyncedAt(java.time.LocalDateTime.now())
                .build();

        // 3. 재료 추가
        for (RecipeCreateRequest.IngredientDto ingredientDto : request.getIngredients()) {
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .name(ingredientDto.getName())
                    .measure(ingredientDto.getMeasure())
                    .build();
            recipe.addIngredient(ingredient);
        }

        // 4. 조리 단계 추가 (RecipeStep 사용)
        int stepNumber = 1;
        for (RecipeCreateRequest.StepDto stepDto : request.getSteps()) {
            RecipeStep step = RecipeStep.builder()
                    .stepNumber(stepNumber++)
                    .description(stepDto.getDescription())
                    .imageUrl(null)  // 사용자 레시피는 단계별 이미지 없음
                    .build();
            recipe.addStep(step);
        }

        // 5. 저장
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 6. 응답 DTO 변환 후 반환
        return convertRecipeEntityToDetailResponse(savedRecipe);
    }

    /**
     * 사용자 레시피 수정
     */
    @Transactional
    public RecipeDetailResponse updateUserRecipe(Long recipeId, RecipeUpdateRequest request, com.recipemate.domain.user.entity.User currentUser) {
        // 1. 레시피 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        // 2. 권한 검사
        if (!recipe.canModify(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 대표 이미지 업데이트 (새 이미지가 있으면)
        String oldImageUrl = recipe.getFullImageUrl();
        if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
            List<String> uploadedUrls = imageUploadUtil.uploadImages(List.of(request.getMainImage()));
            if (!uploadedUrls.isEmpty()) {
                recipe.updateMainImage(uploadedUrls.get(0));

                // 기존 이미지 삭제 (새 이미지 업로드 성공 시)
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    try {
                        imageUploadUtil.deleteImages(List.of(oldImageUrl));
                    } catch (Exception e) {
                        log.warn("Failed to delete old image: {}", oldImageUrl, e);
                        // 삭제 실패해도 계속 진행
                    }
                }
            }
        }

        // 4. 기본 정보 업데이트
        recipe.updateBasicInfo(
            request.getTitle(),
            request.getCategory(),
            request.getArea(),
            request.getTips(),
            request.getYoutubeUrl(),
            request.getSourceUrl()
        );

        // 5. 재료 업데이트 (기존 재료 삭제 후 새로 추가)
        recipe.getIngredients().clear();
        for (RecipeUpdateRequest.IngredientDto ingredientDto : request.getIngredients()) {
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .name(ingredientDto.getName())
                    .measure(ingredientDto.getMeasure())
                    .build();
            recipe.addIngredient(ingredient);
        }

        // 6. 조리 단계 업데이트 (기존 단계 삭제 후 새로 추가)
        recipe.getSteps().clear();
        int stepNumber = 1;
        for (RecipeUpdateRequest.StepDto stepDto : request.getSteps()) {
            RecipeStep step = RecipeStep.builder()
                    .stepNumber(stepNumber++)
                    .description(stepDto.getDescription())
                    .imageUrl(null)  // 사용자 레시피는 단계별 이미지 없음
                    .build();
            recipe.addStep(step);
        }

        // 7. 저장 및 반환
        Recipe updatedRecipe = recipeRepository.save(recipe);
        return convertRecipeEntityToDetailResponse(updatedRecipe);
    }

    /**
     * 사용자 레시피 삭제
     */
    @Transactional
    public void deleteUserRecipe(Long recipeId, com.recipemate.domain.user.entity.User currentUser) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        if (!recipe.canModify(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        recipeRepository.delete(recipe);
        // orphanRemoval = true 설정 덕분에 재료와 조리단계도 자동 삭제됨
    }

    /**
     * 사용자가 작성한 레시피 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<RecipeListResponse.RecipeSimpleInfo> getUserRecipes(com.recipemate.domain.user.entity.User user, Pageable pageable) {
        Page<Recipe> recipes = recipeRepository.findByAuthor(user, pageable);
        return recipes.map(this::convertRecipeEntityToSimpleInfo);
    }
}
