package com.recipemate.domain.groupbuy.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.QGroupBuy;
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

        // 페이징 및 정렬 적용하여 조회
        JPAQuery<GroupBuy> query = queryFactory
                .selectFrom(groupBuy)
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
                // 1인당 가격 = totalPrice / targetHeadcount
                NumberExpression<Double> pricePerPerson = 
                    groupBuy.totalPrice.doubleValue().divide(groupBuy.targetHeadcount.doubleValue());
                yield new OrderSpecifier<>(sortOrder, pricePerPerson);
            }
            default -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
        };

        query.orderBy(orderSpecifier);

        List<GroupBuy> content = query.fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<GroupBuy> findPopularGroupBuys(List<GroupBuyStatus> statuses, Pageable pageable) {
        QGroupBuy groupBuy = QGroupBuy.groupBuy;

        return queryFactory
                .selectFrom(groupBuy)
                .leftJoin(groupBuy.host).fetchJoin()
                .where(
                    groupBuy.status.in(statuses)
                    .and(groupBuy.deletedAt.isNull())
                )
                .orderBy(groupBuy.currentHeadcount.desc(), groupBuy.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
