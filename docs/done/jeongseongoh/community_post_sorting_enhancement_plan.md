# 커뮤니티 게시글 정렬 기능 확장 계획 - likeCount & commentCount 정렬 추가

## 1. 개요

### 1.1. 현재 상태
- ✅ **구현 완료**: `latest` (최신순), `views` (조회수순) 정렬
- ❌ **미구현**: `likes` (좋아요순), `comments` (댓글순) 정렬

### 1.2. 문제점 분석

#### 현재 Repository 구조
```java
@Query("SELECT new com.recipemate.domain.post.dto.PostWithCountsDto(" +
        "p, " +
        "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p), " +
        "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p)) " +
        "FROM Post p " +
        "LEFT JOIN FETCH p.author " +
        "WHERE p.deletedAt IS NULL")
Page<PostWithCountsDto> findAllWithCounts(Pageable pageable);
```

**문제점**:
- `likeCount`와 `commentCount`는 서브쿼리로 계산되어 DTO 생성자의 인자로 전달됨
- JPQL에서 이들을 **정렬 가능한 alias로 정의하지 않음**
- Spring Data JPA의 `Pageable`은 엔티티 필드나 명시적 alias만 정렬할 수 있음
- 서브쿼리 결과는 `ORDER BY`에서 직접 참조 불가능

### 1.3. 기술 스택 확인
- ✅ **QueryDSL 7.0 이미 설치됨** (`build.gradle` line 62-65)
- ✅ **JPAQueryFactory 빈 등록됨** (`QueryDslConfig.java`)
- ✅ **Q클래스 생성됨** (`QPost`, `QPostLike`, `QComment` 등)
- ✅ **프로젝트 내 QueryDSL 사용 사례 있음** (`GroupBuyRepositoryImpl.java`, `RecipeService.java`)

---

## 2. 해결 방안 비교 분석

### 2.1. 방안 1: Native SQL 사용 ⛔ (비추천)
```java
@Query(value = "SELECT p.*, " +
        "(SELECT COUNT(*) FROM post_like WHERE post_id = p.id) as like_count, " +
        "(SELECT COUNT(*) FROM comment WHERE post_id = p.id) as comment_count " +
        "FROM post p WHERE p.deleted_at IS NULL " +
        "ORDER BY like_count DESC",
        nativeQuery = true)
```

**장점**:
- 간단하고 직관적

**단점**:
- ❌ 데이터베이스 종속적 (H2, PostgreSQL 호환성 문제)
- ❌ 타입 안정성 부족
- ❌ 동적 쿼리 구성 어려움 (카테고리, 검색어, 다양한 정렬 조건 조합)
- ❌ Entity 대신 Object[] 반환으로 매핑 복잡
- ❌ 프로젝트의 JPA/QueryDSL 패턴과 불일치

### 2.2. 방안 2: JPQL with Subquery Alias 시도 ⚠️ (불가능)
```java
@Query("SELECT p, " +
        "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p) as likeCount, " +
        "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p) as commentCount " +
        "FROM Post p " +
        "ORDER BY likeCount DESC")
```

**단점**:
- ❌ JPQL은 SELECT 절의 alias를 ORDER BY에서 참조하는 것을 **공식적으로 지원하지 않음**
- ❌ Hibernate가 일부 경우 허용하지만 표준이 아니며 불안정함

### 2.3. 방안 3: QueryDSL 사용 ✅ (최종 선택)

**장점**:
- ✅ 이미 프로젝트에 설치되어 사용 중
- ✅ 타입 안정성 (컴파일 타임 체크)
- ✅ 동적 쿼리 구성 용이
- ✅ 서브쿼리를 변수로 선언하고 정렬에 활용 가능
- ✅ 프로젝트의 기존 패턴과 일치 (`GroupBuyRepositoryImpl` 참고)
- ✅ 데이터베이스 독립적

**단점**:
- ⚠️ 추가 코드 작성 필요 (하지만 재사용성과 유지보수성 향상)

---

## 3. 구현 계획 (QueryDSL 방식)

### 3.1. 아키텍처 설계

```
PostRepository (Interface)
    └─ extends JpaRepository
    └─ extends PostRepositoryCustom (새로 생성)

PostRepositoryCustom (Interface)
    └─ findAllWithCountsDynamic(...) 정의

PostRepositoryImpl (Class)
    └─ implements PostRepositoryCustom
    └─ JPAQueryFactory 주입
    └─ QueryDSL로 동적 쿼리 구현
```

### 3.2. 단계별 구현

#### 3.2.1. PostRepositoryCustom 인터페이스 생성
**파일**: `src/main/java/com/recipemate/domain/post/repository/PostRepositoryCustom.java`

```java
package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.dto.PostWithCountsDto;
import com.recipemate.global.common.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL을 활용한 동적 쿼리 메서드 정의
 */
public interface PostRepositoryCustom {
    
    /**
     * 필터링, 검색, 동적 정렬을 지원하는 게시글 목록 조회
     * 
     * @param category 카테고리 필터 (null 가능)
     * @param keyword 검색 키워드 (null 가능)
     * @param pageable 페이징 및 정렬 정보
     * @return 게시글 목록 (좋아요 수, 댓글 수 포함)
     */
    Page<PostWithCountsDto> findAllWithCountsDynamic(
        PostCategory category, 
        String keyword, 
        Pageable pageable
    );
}
```

#### 3.2.2. PostRepositoryImpl 구현 클래스 생성
**파일**: `src/main/java/com/recipemate/domain/post/repository/PostRepositoryImpl.java`

```java
package com.recipemate.domain.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
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

        // 서브쿼리로 좋아요 수, 댓글 수 계산
        NumberExpression<Long> likeCountExpr = JPAExpressions
                .select(postLike.count())
                .from(postLike)
                .where(postLike.post.eq(post));

        NumberExpression<Long> commentCountExpr = JPAExpressions
                .select(comment.count())
                .from(comment)
                .where(comment.post.eq(post));

        // 데이터 조회 쿼리
        JPAQuery<PostWithCountsDto> query = queryFactory
                .select(Projections.constructor(
                        PostWithCountsDto.class,
                        post,
                        likeCountExpr,
                        commentCountExpr
                ))
                .from(post)
                .leftJoin(post.author).fetchJoin()
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(category),
                        keywordContains(keyword)
                );

        // 동적 정렬 적용
        for (OrderSpecifier<?> order : getOrderSpecifiers(pageable, post, likeCountExpr, commentCountExpr)) {
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
            NumberExpression<Long> likeCountExpr,
            NumberExpression<Long> commentCountExpr
    ) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            com.querydsl.core.types.Order direction = sortOrder.isAscending()
                    ? com.querydsl.core.types.Order.ASC
                    : com.querydsl.core.types.Order.DESC;

            switch (sortOrder.getProperty()) {
                case "likeCount":
                    orders.add(new OrderSpecifier<>(direction, likeCountExpr));
                    break;
                case "commentCount":
                    orders.add(new OrderSpecifier<>(direction, commentCountExpr));
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
```

#### 3.2.3. PostRepository 인터페이스 확장
**파일**: `src/main/java/com/recipemate/domain/post/repository/PostRepository.java`

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    // 기존 메서드들 유지
    // ...
}
```

#### 3.2.4. PostService 수정
**파일**: `src/main/java/com/recipemate/domain/post/service/PostService.java`

```java
@Cacheable(
        value = CacheConfig.VIEW_COUNTS_CACHE,
        key = "'post_list:' + (#category != null ? #category.name() : 'all') + ':' + (#keyword != null ? #keyword : 'none') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
        unless = "#result.isEmpty()"
)
public Page<PostResponse> getPostList(PostCategory category, String keyword, Pageable pageable) {
    // QueryDSL 커스텀 메서드 호출로 변경
    Page<PostWithCountsDto> postsWithCounts = postRepository.findAllWithCountsDynamic(category, keyword, pageable);

    // DTO 변환 (기존 로직 유지)
    return postsWithCounts.map(dto -> {
        Post post = dto.getPost();
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .authorEmail(post.getAuthor().getEmail())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(dto.getLikeCount())
                .commentCount(dto.getCommentCount())
                .isLiked(false)
                .build();
    });
}
```

#### 3.2.5. PostController 수정
**파일**: `src/main/java/com/recipemate/domain/post/controller/PostController.java`

```java
@GetMapping("/list")
public String listPage(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "latest") String sort,
        @RequestParam(defaultValue = "desc") String dir,
        Model model
) {
    // 정렬 정보 생성
    Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    String sortProperty;
    switch (sort) {
        case "views":    sortProperty = "viewCount";   break;
        case "likes":    sortProperty = "likeCount";   break;    // 새로 추가
        case "comments": sortProperty = "commentCount"; break;   // 새로 추가
        default:         sortProperty = "createdAt";   break;
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
    
    // ... 나머지 로직 동일
}
```

#### 3.2.6. list.html 수정
**파일**: `src/main/resources/templates/community-posts/list.html`

```html
<!-- Sort Options -->
<div class="d-flex justify-content-end align-items-center gap-3 mb-3">
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='latest', dir=(${currentSort == 'latest' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'latest' || currentSort == null} ? 'fw-bold text-primary' : 'text-muted'">
        최신순
        <i th:if="${currentSort == 'latest' || currentSort == null}" class="bi" th:classappend="${currentDir == 'desc' || currentDir == null} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='views', dir=(${currentSort == 'views' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'views'} ? 'fw-bold text-primary' : 'text-muted'">
        조회수순
        <i th:if="${currentSort == 'views'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
    <!-- 새로 추가 -->
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='likes', dir=(${currentSort == 'likes' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'likes'} ? 'fw-bold text-primary' : 'text-muted'">
        좋아요순
        <i th:if="${currentSort == 'likes'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
    <a th:href="@{/community-posts/list(category=${currentCategory}, keyword=${keyword}, sort='comments', dir=(${currentSort == 'comments' && currentDir == 'desc'} ? 'asc' : 'desc'))}"
       class="text-decoration-none" th:classappend="${currentSort == 'comments'} ? 'fw-bold text-primary' : 'text-muted'">
        댓글많은순
        <i th:if="${currentSort == 'comments'}" class="bi" th:classappend="${currentDir == 'desc'} ? 'bi-sort-down' : 'bi-sort-up'"></i>
    </a>
</div>
```

---

## 4. 구현 순서 (에자일 방식)

### Phase 1: 핵심 구조 구축
1. `PostRepositoryCustom` 인터페이스 생성
2. `PostRepositoryImpl` 구현 (기본 쿼리 작동 확인)
3. `PostRepository`에 `PostRepositoryCustom` 확장

### Phase 2: 통합 및 테스트
4. `PostService.getPostList()` 메서드를 새 구현으로 교체
5. 기존 기능 (latest, views) 정상 작동 확인

### Phase 3: 신규 기능 추가
6. `PostController`에 likes, comments 정렬 옵션 추가
7. `list.html`에 정렬 UI 추가

### Phase 4: 성능 최적화 및 리팩터링
8. 쿼리 성능 검증 (EXPLAIN ANALYZE)
9. 필요시 인덱스 추가 검토
10. 기존 JPQL 메서드 제거 여부 결정

---

## 5. 예상 이점

### 5.1. 기능적 이점
- ✅ 모든 정렬 옵션 지원 (latest, views, likes, comments)
- ✅ 필터링 조건 유지하면서 정렬 가능
- ✅ 동적 쿼리로 코드 중복 제거

### 5.2. 기술적 이점
- ✅ 타입 안정성 (컴파일 타임 오류 감지)
- ✅ 리팩터링 안전성 (IDE 지원)
- ✅ 데이터베이스 독립적
- ✅ 프로젝트 표준 패턴과 일치

### 5.3. 유지보수 이점
- ✅ 가독성 향상 (복잡한 JPQL 문자열 제거)
- ✅ 재사용성 (동적 정렬 로직 재활용 가능)
- ✅ 확장성 (새로운 정렬 조건 추가 용이)

---

## 6. 주의사항

### 6.1. 성능 고려사항
- 서브쿼리로 COUNT를 계산하므로 데이터가 많을 경우 성능 저하 가능
- 필요시 `PostLike`, `Comment` 테이블에 인덱스 추가 검토:
  ```sql
  CREATE INDEX idx_post_like_post_id ON post_like(post_id);
  CREATE INDEX idx_comment_post_id ON comment(post_id);
  ```

### 6.2. 캐시 무효화
- `PostService.getPostList()`의 캐시 키에 정렬 조건 포함 필요
- 좋아요/댓글 추가 시 캐시 무효화 전략 확인

### 6.3. 기존 메서드 처리
- 기존 JPQL 메서드들(`findAllWithCounts`, `findByCategoryWithCounts` 등)을 유지할지 제거할지 결정
- **권장**: 일단 유지하고 추후 사용처가 없으면 제거 (에자일 방식)
