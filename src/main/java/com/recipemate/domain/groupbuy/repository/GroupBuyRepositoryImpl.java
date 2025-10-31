package com.recipemate.domain.groupbuy.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.QGroupBuy;
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

        // 카테고리 필터
        if (StringUtils.hasText(condition.getCategory())) {
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

        // 정렬 적용
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                
                OrderSpecifier<?> orderSpecifier = switch (property) {
                    case "createdAt" -> new OrderSpecifier<>(direction, groupBuy.createdAt);
                    case "deadline" -> new OrderSpecifier<>(direction, groupBuy.deadline);
                    case "title" -> new OrderSpecifier<>(direction, groupBuy.title);
                    case "currentHeadcount" -> new OrderSpecifier<>(direction, groupBuy.currentHeadcount);
                    default -> new OrderSpecifier<>(direction, groupBuy.createdAt);
                };
                
                query.orderBy(orderSpecifier);
            });
        } else {
            // 기본 정렬: 최신순
            query.orderBy(groupBuy.createdAt.desc());
        }

        List<GroupBuy> content = query.fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
