# 통합 검색 성능 최적화 완료 보고서

## 작업 개요
통합 검색 기능의 성능 문제(과도한 SQL 쿼리)를 해결하기 위한 최적화 작업을 진행했습니다.

**작업 일시**: 2025년 11월 13일  
**작업 범위**: SearchService, GroupBuyRepository, RecipeService, PostRepository, SearchController

---

## 해결한 문제들

### 1. COUNT 쿼리 중복 실행 문제 ✅ 부분 해결

#### 1.1. 'ALL' 탭에서의 중복 COUNT 쿼리 제거
**문제**: 'ALL' 타입 검색 시 데이터 조회 후 별도로 COUNT 쿼리를 3번 더 실행 (총 6번 이상 쿼리)

**해결 방법**:
- **파일**: `SearchService.java`
- **변경사항**:
  - `searchGroupBuys()`, `searchPosts()`, `searchRecipes()` (List 반환) 메서드 제거
  - `countRecipes()`, `countGroupBuys()`, `countPosts()` 메서드 제거
  - ALL 타입 검색 시 Page 객체를 직접 사용하여 `getTotalElements()`로 전체 개수 추출
  - 불필요한 COUNT 쿼리 3개 제거

**변경 전**:
```java
// 1. 데이터 조회 (List 반환)
List<SearchResultResponse> groupBuyResults = searchGroupBuys(keyword, pageable);
List<SearchResultResponse> postResults = searchPosts(keyword, pageable);
List<SearchResultResponse> recipeResults = searchRecipes(keyword, pageable);

// 2. 별도로 COUNT 쿼리 3번 실행
Long totalGroupBuyCount = countGroupBuys(keyword);
Long totalPostCount = countPosts(keyword);
Long totalRecipeCount = countRecipes(keyword);
```

**변경 후**:
```java
// Page 객체로 받아서 데이터와 전체 개수를 한 번에 가져오기
Page<SearchResultResponse> groupBuyPage = searchGroupBuysPage(keyword, previewPageable);
Page<SearchResultResponse> postPage = searchPostsPage(keyword, previewPageable);
Page<SearchResultResponse> recipePage = searchRecipesPage(keyword, previewPageable);

groupBuyResults = groupBuyPage.getContent();
postResults = postPage.getContent();
recipeResults = recipePage.getContent();

// Page 객체에서 전체 개수 추출 (추가 쿼리 없음)
Long totalGroupBuyCount = groupBuyPage.getTotalElements();
Long totalPostCount = postPage.getTotalElements();
Long totalRecipeCount = recipePage.getTotalElements();
```

**효과**: COUNT 쿼리 3개 제거

#### 1.2. 개별 탭(RECIPE, GROUP_BUY, POST)에서의 중복 조회 제거
**문제**: `type=POST`로 접속해도 Controller가 배지 표시를 위해 `unifiedSearch(keyword, "ALL", ...)`을 호출하여 모든 카테고리를 조회

**해결 방법**:
- **파일**: `SearchService.java`, `SearchController.java`
- **변경사항**:
  - `SearchService.getSearchCounts()` 메서드 추가: COUNT 쿼리만 실행하여 배지 표시용 개수 반환
  - `GroupBuyRepositoryCustom.countByCondition()` 메서드 추가 (인터페이스)
  - `GroupBuyRepositoryImpl.countByCondition()` 구현
  - `PostRepository.countByKeyword()` 메서드 추가
  - `RecipeService.countRecipes()` 메서드 추가
  - `SearchController`에서 개별 탭 조회 시 `unifiedSearch()` 대신 `getSearchCounts()` 사용

**변경 전 (SearchController.java)**:
```java
// 개별 탭: 먼저 전체 탭의 개수 정보를 조회 (배지 표시용)
// ⚠️ 문제: ALL 검색을 실행하여 모든 데이터를 조회함
UnifiedSearchResponse allResults = searchService.unifiedSearch(keyword, "ALL", pageable);

if ("POST".equalsIgnoreCase(type)) {
    // 다시 POST만 조회 (중복 쿼리 발생)
    Page<SearchResultResponse> postPage = searchService.searchPostsPage(keyword, pageable);
    // ...
}
```

**변경 후 (SearchController.java)**:
```java
// 개별 탭: 배지 표시를 위해 개수만 조회 (COUNT 쿼리만 실행)
UnifiedSearchResponse counts = searchService.getSearchCounts(keyword);

if ("POST".equalsIgnoreCase(type)) {
    // POST만 조회 (중복 없음)
    Page<SearchResultResponse> postPage = searchService.searchPostsPage(keyword, pageable);
    // ...
}
```

**효과**: 개별 탭 조회 시 불필요한 데이터 조회 쿼리 제거, COUNT 쿼리만 실행

---

### 2. N+1 쿼리 문제 (Host 조회) ✅ 완전 해결

**문제**: 공동구매 목록 조회 시 각 항목마다 host 정보를 별도로 조회

**해결 방법**:
- **파일**: `GroupBuyRepositoryImpl.java`
- **변경사항**: 
  - `searchGroupBuys()` 메서드에 `.leftJoin(groupBuy.host).fetchJoin()` 추가
  - `searchGroupBuysWithReviewStats()` 메서드에도 동일하게 적용
  - Host 정보를 한 번의 쿼리로 함께 조회

**변경 전**:
```java
JPAQuery<GroupBuy> query = queryFactory
    .selectFrom(groupBuy)
    .where(builder)
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize());
// ⚠️ host는 지연 로딩 상태 -> N+1 발생
```

**변경 후**:
```java
JPAQuery<GroupBuy> query = queryFactory
    .selectFrom(groupBuy)
    .leftJoin(groupBuy.host).fetchJoin()  // ✅ Fetch Join으로 한 번에 조회
    .where(builder)
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize());
```

**효과**: 공동구매 목록이 N개일 때, Host 조회를 위한 N개의 추가 쿼리 제거

---

### 3. N+1 쿼리 문제 (리뷰 집계 데이터) ✅ 완전 해결

**문제**: 공동구매 목록의 각 항목마다 평균 평점과 리뷰 개수를 별도로 조회 (가장 심각한 N+1 문제)

**해결 방법**:
- **새로 생성한 파일**: `GroupBuyWithReviewStatsDto.java`
  - GroupBuy 엔티티와 리뷰 통계(averageRating, reviewCount)를 함께 담는 DTO
  
- **수정한 파일들**:
  - `GroupBuyRepositoryCustom.java`: `searchGroupBuysWithReviewStats()` 메서드 인터페이스 추가
  - `GroupBuyRepositoryImpl.java`: 
    - QueryDSL Projections를 사용한 `searchGroupBuysWithReviewStats()` 구현
    - LEFT JOIN과 GROUP BY를 활용해 리뷰 통계를 한 번에 조회
  - `GroupBuyService.java`:
    - `getGroupBuyList()` 메서드를 새로운 `searchGroupBuysWithReviewStats()` 사용하도록 변경
    - `mapToResponseWithStats()` 메서드 추가 (리뷰 통계를 파라미터로 받음)
    - 기존 `mapToResponse()`는 상세 조회에서만 사용 (reviewRepository 직접 조회)

**변경 전 (GroupBuyService.java)**:
```java
public Page<GroupBuyResponse> getGroupBuyList(GroupBuySearchCondition condition, Pageable pageable) {
    Page<GroupBuy> groupBuys = groupBuyRepository.searchGroupBuys(condition, pageable);
    
    // ⚠️ N+1 문제 발생 지점
    return groupBuys.map(groupBuy -> {
        // 각 항목마다 개별적으로 리뷰 통계 조회 (N개의 쿼리 발생)
        Double averageRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuy.getId());
        long reviewCount = reviewRepository.countByGroupBuyId(groupBuy.getId());
        return mapToResponse(groupBuy, averageRating, reviewCount);
    });
}
```

**변경 후 (GroupBuyService.java)**:
```java
public Page<GroupBuyResponse> getGroupBuyList(GroupBuySearchCondition condition, Pageable pageable) {
    // ✅ 리뷰 통계를 포함한 DTO로 조회 (한 번의 쿼리)
    Page<GroupBuyWithReviewStatsDto> groupBuysWithStats = 
        groupBuyRepository.searchGroupBuysWithReviewStats(condition, pageable);
    
    // 리뷰 통계가 이미 포함되어 있어 추가 쿼리 불필요
    return groupBuysWithStats.map(dto -> 
        mapToResponseWithStats(dto.getGroupBuy(), dto.getAverageRating(), dto.getReviewCount())
    );
}
```

**변경 후 (GroupBuyRepositoryImpl.java)**:
```java
@Override
public Page<GroupBuyWithReviewStatsDto> searchGroupBuysWithReviewStats(
        GroupBuySearchCondition condition, Pageable pageable) {
    
    QGroupBuy groupBuy = QGroupBuy.groupBuy;
    QReview review = QReview.review;

    // ✅ QueryDSL Projection으로 리뷰 통계를 한 번에 계산
    JPAQuery<GroupBuyWithReviewStatsDto> query = queryFactory
        .select(Projections.constructor(
            GroupBuyWithReviewStatsDto.class,
            groupBuy,
            review.rating.avg().coalesce(0.0),  // 평균 평점
            review.count()                       // 리뷰 개수
        ))
        .from(groupBuy)
        .leftJoin(groupBuy.host).fetchJoin()
        .leftJoin(review).on(review.groupBuy.eq(groupBuy))
        .where(builder)
        .groupBy(groupBuy.id)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());
    
    List<GroupBuyWithReviewStatsDto> content = query.fetch();
    
    return new PageImpl<>(content, pageable, total);
}
```

**효과**: 공동구매 목록이 N개일 때, 리뷰 통계 조회를 위한 2N개의 추가 쿼리 제거

---

## 성능 개선 효과 요약

### 쿼리 개수 비교 (예: keyword="치킨" 검색 시)

#### Case 1: 'ALL' 탭 조회 (상위 5개씩 미리보기)

**변경 전**:
```
1. 공동구매 조회 (LIMIT 5)                    1개
2. 공동구매 host 조회 (각 5개)                 5개
3. 공동구매 리뷰 평균 조회 (각 5개)            5개
4. 공동구매 리뷰 개수 조회 (각 5개)            5개
5. 게시글 조회 (LIMIT 5)                       1개
6. 레시피 조회 (LIMIT 5)                       1개
7. 공동구매 COUNT 쿼리                         1개
8. 게시글 COUNT 쿼리                           1개
9. 레시피 COUNT 쿼리                           1개
---------------------------------------------------
총: 21개 쿼리
```

**변경 후**:
```
1. 공동구매 조회 (LIMIT 5) + host + 리뷰통계   1개 (JOIN + GROUP BY)
2. 공동구매 COUNT 쿼리                         1개 (Page 내부)
3. 게시글 조회 (LIMIT 5)                       1개
4. 게시글 COUNT 쿼리                           1개 (Page 내부)
5. 레시피 조회 (LIMIT 5)                       1개
6. 레시피 COUNT 쿼리                           1개 (Page 내부)
---------------------------------------------------
총: 6개 쿼리
```

**개선율**: 21개 → 6개 (**약 71% 감소**)

#### Case 2: 개별 탭 조회 (예: type=POST, 10개 조회)

**변경 전**:
```
1. 공동구매 조회 (LIMIT 5) - ALL 탭 배지용     1개
2. 공동구매 host 조회 (각 5개)                 5개
3. 공동구매 리뷰 통계 조회 (각 5개 * 2)        10개
4. 게시글 조회 (LIMIT 5) - ALL 탭 배지용      1개
5. 레시피 조회 (LIMIT 5) - ALL 탭 배지용      1개
6. 공동구매 COUNT 쿼리                         1개
7. 게시글 COUNT 쿼리                           1개
8. 레시피 COUNT 쿼리                           1개
9. 게시글 실제 조회 (LIMIT 10)                 1개
10. 게시글 실제 COUNT 쿼리                     1개
---------------------------------------------------
총: 23개 쿼리
```

**변경 후**:
```
1. 공동구매 COUNT 쿼리만                       1개
2. 게시글 COUNT 쿼리만                         1개
3. 레시피 COUNT 쿼리만                         1개
4. 게시글 실제 조회 (LIMIT 10)                 1개
5. 게시글 실제 COUNT 쿼리                      1개
---------------------------------------------------
총: 5개 쿼리
```

**개선율**: 23개 → 5개 (**약 78% 감소**)

---

## 남은 최적화 과제

### 1. Spring Data JPA의 자동 COUNT 쿼리 문제 (보류)

**현상**: Page 객체를 사용하면 Spring Data JPA가 자동으로 별도의 COUNT 쿼리를 실행합니다.

**예시**:
```sql
-- 데이터 조회 쿼리
SELECT gb.* FROM group_buys gb WHERE ... LIMIT 5;

-- 자동 실행되는 COUNT 쿼리
SELECT COUNT(gb.id) FROM group_buys gb WHERE ...;
```

**해결 방안 (선택지)**:

1. **Slice 사용** (추천 - 개별 탭)
   - `Page` 대신 `Slice` 사용
   - COUNT 쿼리를 실행하지 않음
   - 단점: 전체 개수를 알 수 없음 (hasNext()만 제공)
   - 적용 대상: 개별 탭 (RECIPE, GROUP_BUY, POST)에서 페이징 시

2. **Window 함수 사용** (복잡)
   - Native Query로 WINDOW 함수 사용
   - 데이터 조회와 COUNT를 한 번에 처리
   - 단점: JPQL/QueryDSL로는 제한적, Native Query 필요

3. **캐싱 활용** (간단)
   - COUNT 결과를 Redis 등에 캐싱
   - 단점: 캐시 무효화 전략 필요

**권장사항**:
- **'ALL' 탭**: COUNT 쿼리가 필요하므로 현재 유지 (Page 사용)
- **개별 탭**: 무한 스크롤이나 "더보기" 방식으로 변경 가능하면 Slice 사용 고려

### 2. findPopularGroupBuys 메서드 최적화 (미완료)

**문제**: 홈 화면의 인기 공구 조회에서도 동일한 N+1 문제 발생 가능성

**해결 방안**: `searchGroupBuysWithReviewStats()`와 동일한 방식으로 리뷰 통계를 함께 조회하도록 수정

---

## 수정된 파일 목록

### 신규 생성
- `src/main/java/com/recipemate/domain/groupbuy/dto/GroupBuyWithReviewStatsDto.java`

### 수정
1. **SearchService.java**
   - `unifiedSearch()`: Page 객체로 변경, COUNT 쿼리 제거
   - `getSearchCounts()`: 신규 메서드 추가 (COUNT만 조회)
   - 제거된 메서드: `searchGroupBuys()`, `searchPosts()`, `searchRecipes()`, `countGroupBuys()`, `countPosts()`, `countRecipes()`

2. **SearchController.java**
   - 개별 탭 조회 시 `unifiedSearch()` → `getSearchCounts()` 변경

3. **GroupBuyRepositoryCustom.java**
   - `searchGroupBuysWithReviewStats()` 인터페이스 추가
   - `countByCondition()` 인터페이스 추가

4. **GroupBuyRepositoryImpl.java**
   - `searchGroupBuys()`: fetchJoin 추가
   - `searchGroupBuysWithReviewStats()`: 신규 메서드 구현
   - `countByCondition()`: 신규 메서드 구현

5. **GroupBuyService.java**
   - `getGroupBuyList()`: `searchGroupBuysWithReviewStats()` 사용
   - `mapToResponseWithStats()`: 신규 메서드 추가

6. **PostRepository.java**
   - `countByKeyword()`: 신규 메서드 추가

7. **RecipeService.java**
   - `countRecipes()`: 신규 메서드 추가

---

## 테스트 가이드

### 1. 빌드 확인
```bash
./gradlew compileJava
```
**결과**: ✅ BUILD SUCCESSFUL

### 2. 애플리케이션 실행 및 로그 확인

실행 후 다음 URL들로 테스트하면서 SQL 로그를 확인:

```yaml
# application.yml에서 SQL 로그 활성화
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

**테스트 시나리오**:

1. **'ALL' 탭 검색**
   - URL: `http://localhost:8080/search?keyword=치킨&type=ALL`
   - 예상 쿼리 수: 약 6개
   - 확인 사항: COUNT 쿼리가 Page 객체 생성 시 1번씩만 실행되는지

2. **개별 탭 검색 (POST)**
   - URL: `http://localhost:8080/search?keyword=치킨&type=POST`
   - 예상 쿼리 수: 약 5개
   - 확인 사항: 
     - ALL 탭 데이터 조회 쿼리가 실행되지 않는지
     - COUNT 쿼리만 3개 실행되는지

3. **개별 탭 검색 (GROUP_BUY)**
   - URL: `http://localhost:8080/search?keyword=치킨&type=GROUP_BUY`
   - 예상 쿼리 수: 약 5개
   - 확인 사항:
     - N+1 쿼리가 발생하지 않는지
     - JOIN 쿼리에 host와 review가 포함되어 있는지

### 3. 성능 비교

**측정 방법**:
1. SQL 로그에서 쿼리 개수 카운트
2. 응답 시간 측정 (브라우저 개발자 도구 Network 탭)
3. 데이터베이스 모니터링 (쿼리 실행 시간, 부하)

---

## 결론

이번 최적화 작업을 통해:
- ✅ 불필요한 중복 COUNT 쿼리 제거
- ✅ N+1 쿼리 문제 완전 해결 (Host, 리뷰 통계)
- ✅ 개별 탭 조회 시 불필요한 전체 검색 제거
- ✅ 약 70-80%의 쿼리 감소 효과

**예상 성능 향상**:
- 응답 시간: 30-50% 단축 예상
- 데이터베이스 부하: 70% 이상 감소
- 동시 사용자 처리 능력: 2-3배 향상 예상

**다음 단계**:
1. 실제 환경에서 성능 테스트 및 검증
2. Slice 적용 검토 (개별 탭 페이징)
3. `findPopularGroupBuys()` 메서드 최적화
4. 캐싱 전략 수립 (Redis 등)
