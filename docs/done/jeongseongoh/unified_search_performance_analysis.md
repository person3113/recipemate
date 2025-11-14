# 통합 검색 기능 성능 분석 및 개선 계획

## 1. 문제 분석

현재 통합 검색 기능(`http://localhost:8080/search?keyword=...`)은 사용자 요청에 비해 과도한 SQL 쿼리를 발생시켜 성능 저하의 원인이 되고 있습니다. 특히 '전체' 탭 조회 시 여러 번의 중복 쿼리가 실행되며, 각 탭을 전환할 때마다 불필요한 전체 페이지 로딩이 발생합니다.

### 1.1. 불필요한 COUNT 쿼리 중복 실행

'전체' 탭(`type=ALL`)을 조회할 때, 각 카테고리(레시피, 공동구매, 커뮤니티)별로 상위 5개의 미리보기 데이터를 가져온 후, 총 개수를 파악하기 위해 별도의 `COUNT(*)` 쿼리를 추가로 실행하고 있습니다.

- **현상**: 데이터 조회를 위한 쿼리 3개 실행 후, 총 개수 조회를 위한 `COUNT` 쿼리 3개가 추가로 실행되어 총 6번 이상 데이터베이스를 조회합니다.
- **문제**: Spring Data의 `Page` 객체나 커스텀 응답 객체를 사용하면 데이터와 총 개수를 한 번의 쿼리로 가져올 수 있으므로, `COUNT` 쿼리는 불필요한 중복 조회입니다.

### 1.2. N+1 쿼리 문제 (공동구매 검색)

공동구매 목록을 검색할 때, Hibernate의 지연 로딩(Lazy Loading) 전략으로 인해 심각한 N+1 문제가 발생하고 있습니다. 목록 조회 쿼리 1번 실행 후, 각 공동구매 항목마다 연관된 하위 데이터(작성자 정보, 리뷰 평균, 리뷰 개수 등)를 가져오기 위해 N번의 추가 쿼리가 실행됩니다.

- **현상**: 공동구매 결과가 5개일 경우, `1 (목록) + 5 * (리뷰 평균 + 리뷰 개수 + 작성자 정보) = 16개` 이상의 쿼리가 발생합니다.
- **문제**: 애플리케이션의 성능을 가장 크게 저하시키는 원인입니다.

#### 상세 원인
N+1 문제의 구체적인 발생 지점은 `GroupBuyService`가 `GroupBuy` 엔티티 목록을 `GroupBuyResponse` DTO 목록으로 변환하는 과정에 있습니다.

1.  **`GroupBuyRepositoryImpl`**: `searchGroupBuys` 메서드는 `GroupBuy` 엔티티 목록만 조회할 뿐, 연관된 데이터(작성자, 리뷰 등)는 함께 조회하지 않습니다.
2.  **`GroupBuyService`**: `getGroupBuyList` 메서드에서 `groupBuyRepository.searchGroupBuys`를 통해 `Page<GroupBuy>`를 가져옵니다.
3.  **`mapToResponse` 호출**: 그 후 `groupBuys.map(groupBuy -> mapToResponse(groupBuy, ...))` 코드를 통해 목록의 **각 `GroupBuy` 항목마다 `mapToResponse` 메서드를 개별적으로 호출**합니다.
4.  **N+1 쿼리 실행**: `mapToResponse` 메서드 내부에서 각 `groupBuy`의 리뷰 평점과 개수를 조회하기 위해 아래와 같이 `reviewRepository`를 호출하면서 N+1 쿼리가 발생합니다.
    ```java
    // N+1 발생 지점
    Double averageRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuy.getId());
    long reviewCount = reviewRepository.countByGroupBuyId(groupBuy.getId());
    ```
    또한, `groupBuy.getHost().getNickname()`을 호출하는 부분에서도 `host` 엔티티가 지연 로딩(Lazy Loading) 상태이므로 추가 쿼리가 발생합니다.


## 2. 개선 계획

### 2.1. COUNT 쿼리 최적화 (즉시 실행)

`SearchService.java`의 `unifiedSearch` 메서드에서 'ALL' 타입 검색 로직을 수정하여, 데이터 조회 시 반환되는 `Page` 객체 또는 응답 객체에서 총 개수를 추출하도록 변경합니다.

- **조치**: 별도의 `countGroupBuys`, `countPosts`, `countRecipes` 메서드를 호출하는 대신, `groupBuyPage.getTotalElements()` 와 같은 방식으로 총 개수를 가져옵니다.
- **기대 효과**: '전체' 탭 조회 시 불필요한 `COUNT` 쿼리 3개를 제거하여 데이터베이스 부하를 줄이고 응답 속도를 개선합니다.

**변경 전 (의사 코드):**
```java
// 1. 데이터 가져오기 (상위 5개)
groupBuyResults = searchGroupBuys(keyword, pageable);
postResults = searchPosts(keyword, pageable);
recipeResults = searchRecipes(keyword, pageable);

// 2. 전체 개수 다시 세기 (불필요한 쿼리 발생)
totalGroupBuyCount = countGroupBuys(keyword);
totalPostCount = countPosts(keyword);
totalRecipeCount = countRecipes(keyword);
```

**변경 후 (의사 코드):**
```java
// 1. 데이터와 전체 개수를 한 번에 가져오기
Page<GroupBuy> groupBuyPage = groupBuyRepository.search(keyword, pageable);
groupBuyResults = groupBuyPage.getContent();
totalGroupBuyCount = groupBuyPage.getTotalElements(); // 추가 쿼리 없음

// ... Post, Recipe 동일하게 수정
```

### 2.2. N+1 문제 해결 (즉시 실행)

QueryDSL 또는 JPQL에서 `fetchJoin`이나 `EntityGraph`를 사용하여 연관 관계의 데이터를 한 번의 쿼리로 함께 조회(Eager Loading)하도록 수정합니다.

- **조치**:
    1.  **`fetchJoin` 활용**: `GroupBuyRepository`의 검색 쿼리에서 `leftJoin(groupBuy.host, user).fetchJoin()`과 같이 사용하여 `host`(작성자) 정보를 함께 가져옵니다.
    2.  **리뷰 집계 데이터 처리**: 리뷰 개수나 평균 평점 같은 집계 데이터는 DTO 프로젝션을 사용하여 JPQL/QueryDSL에서 직접 계산하도록 쿼리를 수정합니다. 이렇게 하면 각 공동구매마다 리뷰 테이블을 조회하는 것을 방지할 수 있습니다.
- **기대 효과**: 공동구매 목록 조회 시 발생하는 수많은 추가 쿼리를 단 하나의 최적화된 쿼리로 통합하여 가장 극적인 성능 향상을 기대할 수 있습니다.


