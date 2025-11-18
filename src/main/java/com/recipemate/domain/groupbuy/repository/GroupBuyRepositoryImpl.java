package com.recipemate.domain.groupbuy.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.dto.GroupBuyWithReviewStatsDto;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.QGroupBuy;
import com.recipemate.domain.review.entity.QReview;
import com.recipemate.global.common.GroupBuyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * GroupBuy 커스텀 Repository 구현체
 * QueryDSL을 사용한 동적 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class GroupBuyRepositoryImpl implements GroupBuyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GroupBuy> searchGroupBuys(GroupBuySearchCondition condition, Pageable pageable) {
        QGroupBuy groupBuy = QGroupBuy.groupBuy;

        // 동적 쿼리 조건 빌드
        BooleanBuilder builder = new BooleanBuilder();

        // 삭제되지 않은 공구만 조회
        builder.and(groupBuy.deletedAt.isNull());

        // 키워드 검색 (제목 또는 내용에 포함)
        if (StringUtils.hasText(condition.getKeyword())) {
            builder.and(
                groupBuy.title.containsIgnoreCase(condition.getKeyword())
                    .or(groupBuy.content.containsIgnoreCase(condition.getKeyword()))
            );
        }

        // 재료명 검색
        if (StringUtils.hasText(condition.getIngredients())) {
            builder.and(groupBuy.ingredients.containsIgnoreCase(condition.getIngredients()));
        }

        // 카테고리 필터
        if (condition.getCategory() != null) {
            builder.and(groupBuy.category.eq(condition.getCategory()));
        }

        // 상태 필터
        if (condition.getStatus() != null) {
            builder.and(groupBuy.status.eq(condition.getStatus()));
        }

        // 레시피 기반 공구 필터
        if (condition.getRecipeOnly() != null && condition.getRecipeOnly()) {
            builder.and(groupBuy.recipeApiId.isNotNull());
        }

        // 전체 개수 조회 (별도 count 쿼리 실행)
        Long totalCount = queryFactory
                .select(groupBuy.count())
                .from(groupBuy)
                .where(builder)
                .fetchOne();

        // null 방지
        long total = (totalCount != null) ? totalCount : 0L;

        // 페이징 및 정렬 적용하여 조회 (host를 fetchJoin으로 함께 조회)
        JPAQuery<GroupBuy> query = queryFactory
                .selectFrom(groupBuy)
                .leftJoin(groupBuy.host).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 커스텀 정렬 로직 (condition의 sortBy, direction 우선 사용)
        String sortBy = condition.getSortBy() != null ? condition.getSortBy() : "latest";
        String direction = condition.getDirection() != null ? condition.getDirection() : "desc";
        Order sortOrder = "asc".equals(direction) ? Order.ASC : Order.DESC;

        OrderSpecifier<?> orderSpecifier = switch (sortBy) {
            case "latest" -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
            case "deadline" -> new OrderSpecifier<>(sortOrder, groupBuy.deadline);
            case "participants" -> new OrderSpecifier<>(sortOrder, groupBuy.currentHeadcount);
            case "price" -> {
                // 1인당 가격 = targetAmount / targetHeadcount
                NumberExpression<Double> pricePerPerson = 
                    groupBuy.targetAmount.doubleValue().divide(groupBuy.targetHeadcount.doubleValue());
                yield new OrderSpecifier<>(sortOrder, pricePerPerson);
            }
            default -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
        };

        query.orderBy(orderSpecifier);

        List<GroupBuy> content = query.fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<GroupBuyWithReviewStatsDto> searchGroupBuysWithReviewStats(GroupBuySearchCondition condition, Pageable pageable) {
        QGroupBuy groupBuy = QGroupBuy.groupBuy;
        QReview review = QReview.review;

        // 동적 쿼리 조건 빌드
        BooleanBuilder builder = new BooleanBuilder();

        // 삭제되지 않은 공구만 조회
        builder.and(groupBuy.deletedAt.isNull());

        // 키워드 검색 (제목 또는 내용에 포함)
        if (StringUtils.hasText(condition.getKeyword())) {
            builder.and(
                groupBuy.title.containsIgnoreCase(condition.getKeyword())
                    .or(groupBuy.content.containsIgnoreCase(condition.getKeyword()))
            );
        }

        // 재료명 검색
        if (StringUtils.hasText(condition.getIngredients())) {
            builder.and(groupBuy.ingredients.containsIgnoreCase(condition.getIngredients()));
        }

        // 카테고리 필터
        if (condition.getCategory() != null) {
            builder.and(groupBuy.category.eq(condition.getCategory()));
        }

        // 상태 필터
        if (condition.getStatus() != null) {
            builder.and(groupBuy.status.eq(condition.getStatus()));
        }

        // 레시피 기반 공구 필터
        if (condition.getRecipeOnly() != null && condition.getRecipeOnly()) {
            builder.and(groupBuy.recipeApiId.isNotNull());
        }

        // 전체 개수 조회
        Long totalCount = queryFactory
                .select(groupBuy.count())
                .from(groupBuy)
                .where(builder)
                .fetchOne();

        long total = (totalCount != null) ? totalCount : 0L;

        // 페이징 및 정렬 적용하여 조회 (host와 리뷰 통계를 함께 조회)
        JPAQuery<GroupBuyWithReviewStatsDto> query = queryFactory
                .select(Projections.constructor(
                    GroupBuyWithReviewStatsDto.class,
                    groupBuy,
                    review.rating.avg().coalesce(0.0),
                    review.count()
                ))
                .from(groupBuy)
                .leftJoin(groupBuy.host).fetchJoin()
                .leftJoin(review).on(review.groupBuy.eq(groupBuy))
                .where(builder)
                .groupBy(groupBuy.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 커스텀 정렬 로직
        String sortBy = condition.getSortBy() != null ? condition.getSortBy() : "latest";
        String direction = condition.getDirection() != null ? condition.getDirection() : "desc";
        Order sortOrder = "asc".equals(direction) ? Order.ASC : Order.DESC;

        OrderSpecifier<?> orderSpecifier = switch (sortBy) {
            case "latest" -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
            case "deadline" -> new OrderSpecifier<>(sortOrder, groupBuy.deadline);
            case "participants" -> new OrderSpecifier<>(sortOrder, groupBuy.currentHeadcount);
            case "price" -> {
                NumberExpression<Double> pricePerPerson = 
                    groupBuy.targetAmount.doubleValue().divide(groupBuy.targetHeadcount.doubleValue());
                yield new OrderSpecifier<>(sortOrder, pricePerPerson);
            }
            default -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
        };

        query.orderBy(orderSpecifier);

        List<GroupBuyWithReviewStatsDto> content = query.fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<GroupBuy> findPopularGroupBuys(List<GroupBuyStatus> statuses, Pageable pageable) {
        QGroupBuy groupBuy = QGroupBuy.groupBuy;
        
        // 현재 시간 및 7일 전 시간 계산
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        // 인기 점수 계산 공식: (참여 인원수 - 1) / (경과 시간 + 2)^1.8
        // 경과 시간 = (현재 시간 - 생성 시간) / 1시간
        // Note: QueryDSL에서 직접 제곱 계산이 어려우므로, 여기서는 단순화된 버전 사용
        // 실제로는 currentHeadcount를 우선순위로 하되, 최신 공구에 가중치를 줌
        
        return queryFactory
                .selectFrom(groupBuy)
                .leftJoin(groupBuy.host).fetchJoin()
                .where(
                    groupBuy.status.in(statuses)
                    .and(groupBuy.deletedAt.isNull())
                    .and(groupBuy.createdAt.goe(sevenDaysAgo)) // 최근 7일 내 생성된 공구만
                )
                .orderBy(
                    groupBuy.currentHeadcount.desc(),  // 참여자 수 우선
                    groupBuy.createdAt.desc()           // 최신 공구 우선
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
    
    @Override
    public long countByCondition(GroupBuySearchCondition condition) {
        QGroupBuy groupBuy = QGroupBuy.groupBuy;

        // 동적 쿼리 조건 빌드
        BooleanBuilder builder = new BooleanBuilder();

        // 삭제되지 않은 공구만 조회
        builder.and(groupBuy.deletedAt.isNull());

        // 키워드 검색 (제목 또는 내용에 포함)
        if (StringUtils.hasText(condition.getKeyword())) {
            builder.and(
                groupBuy.title.containsIgnoreCase(condition.getKeyword())
                    .or(groupBuy.content.containsIgnoreCase(condition.getKeyword()))
            );
        }

        // 재료명 검색
        if (StringUtils.hasText(condition.getIngredients())) {
            builder.and(groupBuy.ingredients.containsIgnoreCase(condition.getIngredients()));
        }

        // 카테고리 필터
        if (condition.getCategory() != null) {
            builder.and(groupBuy.category.eq(condition.getCategory()));
        }

        // 상태 필터
        if (condition.getStatus() != null) {
            builder.and(groupBuy.status.eq(condition.getStatus()));
        }

        // 레시피 기반 공구 필터
        if (condition.getRecipeOnly() != null && condition.getRecipeOnly()) {
            builder.and(groupBuy.recipeApiId.isNotNull());
        }

        // COUNT 쿼리만 실행
        Long count = queryFactory
                .select(groupBuy.count())
                .from(groupBuy)
                .where(builder)
                .fetchOne();

        return (count != null) ? count : 0L;
    }
}
