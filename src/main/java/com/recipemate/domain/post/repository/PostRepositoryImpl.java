package com.recipemate.domain.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.recipemate.domain.comment.entity.QComment;
import com.recipemate.domain.like.entity.QPostLike;
import com.recipemate.domain.post.dto.PostWithCountsDto;
import com.recipemate.domain.post.entity.QPost;
import com.recipemate.global.common.PostCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryDSL을 활용한 PostRepository 커스텀 구현
 * likeCount, commentCount 기준 정렬을 위해 구현
 * LEFT JOIN과 GROUP BY를 사용하여 집계 값 기준 정렬 지원
 */
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostWithCountsDto> findAllWithCountsDynamic(
            PostCategory category,
            String keyword,
            Pageable pageable
    ) {
        QPost post = QPost.post;
        QPostLike postLike = QPostLike.postLike;
        QComment comment = QComment.comment;

        // 데이터 조회 쿼리 (LEFT JOIN + GROUP BY로 집계)
        JPAQuery<PostWithCountsDto> query = queryFactory
                .select(Projections.constructor(
                        PostWithCountsDto.class,
                        post,
                        postLike.count(),
                        comment.count()
                ))
                .from(post)
                .leftJoin(post.author).fetchJoin()
                .leftJoin(postLike).on(postLike.post.eq(post))
                .leftJoin(comment).on(comment.post.eq(post))
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(category),
                        keywordContains(keyword)
                )
                .groupBy(post.id, post.author.id);

        // 동적 정렬 적용
        for (OrderSpecifier<?> order : getOrderSpecifiers(pageable, post, postLike, comment)) {
            query.orderBy(order);
        }

        // 페이징 적용
        List<PostWithCountsDto> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리 (정렬 제외, 성능 최적화)
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(category),
                        keywordContains(keyword)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 카테고리 필터 조건
     */
    private BooleanExpression categoryEq(PostCategory category) {
        return category != null ? QPost.post.category.eq(category) : null;
    }

    /**
     * 검색 키워드 조건 (제목 또는 내용)
     */
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim().toLowerCase();
        return QPost.post.title.lower().contains(trimmedKeyword)
                .or(QPost.post.content.lower().contains(trimmedKeyword));
    }

    /**
     * 동적 정렬 조건 생성
     * Pageable의 Sort 정보를 기반으로 OrderSpecifier 생성
     */
    private List<OrderSpecifier<?>> getOrderSpecifiers(
            Pageable pageable,
            QPost post,
            QPostLike postLike,
            QComment comment
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            com.querydsl.core.types.Order direction = sortOrder.isAscending()
                    ? com.querydsl.core.types.Order.ASC
                    : com.querydsl.core.types.Order.DESC;

            switch (sortOrder.getProperty()) {
                case "likeCount":
                    orders.add(new OrderSpecifier<>(direction, postLike.count()));
                    break;
                case "commentCount":
                    orders.add(new OrderSpecifier<>(direction, comment.count()));
                    break;
                case "viewCount":
                    orders.add(new OrderSpecifier<>(direction, post.viewCount));
                    break;
                case "createdAt":
                default:
                    orders.add(new OrderSpecifier<>(direction, post.createdAt));
                    break;
            }
        }

        // 정렬 조건이 없으면 기본값: 최신순
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, post.createdAt));
        }

        return orders;
    }
}
