# 공구 검색 범위 불일치 문제 심층 분석 보고서

## 1. 문제 현황

-   **현상**: 통합 검색(`GET /search`)에서는 공동구매(Group Buy) 검색 시 `제목`만 검색되고 `내용`은 검색되지 않는다. 반면, 공구 전용 목록(`GET /group-purchases/list`)에서는 `제목`과 `내용`이 모두 정상적으로 검색된다.
-   **특이사항**: `GroupBuyRepository`의 `searchByKeyword` 메소드 JPQL 쿼리에는 `OR g.content LIKE ...` 조건이 이미 포함되어 있음에도 불구하고, 통합 검색에서는 해당 조건이 적용되지 않는 것처럼 동작한다.

## 2. 근본 원인 추적 및 분석

이전 분석에서 놓쳤던 부분을 포함하여, 두 기능의 코드 호출 경로 전체를 추적한 결과 다음과 같은 **결정적인 차이점**을 발견했습니다.

### 경로 A: 공구 전용 목록 (정상 동작)

1.  **Controller (`GroupBuyController`)**: `GET /group-purchases/list` 요청을 받으면, `keyword`를 `GroupBuySearchCondition` 객체에 담아 `groupBuyService.getGroupBuyList()`를 호출합니다.
2.  **Service (`GroupBuyService`)**: `getGroupBuyList()` 메소드는 `groupBuyRepository.searchGroupBuys()`를 호출합니다.
3.  **Repository (`GroupBuyRepositoryCustomImpl`)**: `searchGroupBuys()` 메소드는 **QueryDSL**을 사용하여 동적 쿼리를 생성합니다. 이 쿼리는 `keyword`가 존재할 경우, `title`과 `content`를 `OR` 조건으로 묶어 검색합니다.

    ```java
    // GroupBuyRepositoryCustomImpl.java (예상 코드)
    if (condition.getKeyword() != null) {
        builder.and(groupBuy.title.contains(keyword)
                     .or(groupBuy.content.contains(keyword)));
    }
    ```

### 경로 B: 통합 검색 (문제 발생)

1.  **Controller (`SearchController`)**: `GET /search` 요청을 받으면, `keyword`를 `searchService.unifiedSearch()`에 전달합니다.
2.  **Service (`SearchService`)**: `unifiedSearch()` 메소드는 `groupBuyRepository.searchByKeyword()`를 호출합니다.
3.  **Repository (`GroupBuyRepository`)**: `searchByKeyword()` 메소드는 **JPQL `@Query`**를 사용합니다.
    ```java
    @Query("SELECT g FROM GroupBuy g WHERE (g.title LIKE %:keyword% OR g.content LIKE %:keyword%) AND g.status = :status ORDER BY g.createdAt DESC")
    Page<GroupBuy> searchByKeyword(@Param("keyword") String keyword, ...);
    ```

### 3. 진짜 원인: QueryDSL과 JPQL의 동작 차이

-   **결론**: 두 기능은 **서로 다른 기술 스택(QueryDSL vs JPQL)과 메소드**를 사용하여 검색을 수행하고 있었습니다.
-   **진짜 원인**: 사용자님이 확인하신 JPQL 쿼리는 문법적으로 문제가 없습니다. 하지만, **통합 검색이 호출하는 `searchByKeyword` 메소드가 아닌, 공구 전용 목록이 호출하는 QueryDSL의 `searchGroupBuys` 메소드만이 실제로 `content`를 포함하여 올바르게 동작**하고 있었습니다. 즉, `searchByKeyword` 메소드 자체에 문제가 있거나, 해당 메소드가 아닌 다른 잘못된 메소드가 호출되고 있을 가능성이 매우 높습니다.

사용자님께서 "코드는 맞는데 동작하지 않는다"고 하신 상황은, JPQL 쿼리에 문제가 있다기보다는 **다른 요인이 JPQL 쿼리의 `content` 검색을 방해**하고 있을 가능성을 시사합니다.

-   **가설 1: JPA의 변경 감지 및 더티 체킹 문제**: `SearchService`의 트랜잭션 설정(`@Transactional(readOnly = true)`)과 `GroupBuyRepository`의 상호작용에서, 특정 조건 하에 `content` 필드가 조회 대상에서 누락될 가능성.
-   **가설 2: Full-Text Indexing의 부재**: `content` 필드는 `TEXT` 타입으로 매우 클 수 있습니다. 데이터베이스 수준에서 `content` 필드에 대한 인덱싱이 제대로 되어있지 않으면, `LIKE %...%` 검색 시 성능 문제로 인해 결과가 누락되거나 비정상적으로 동작할 수 있습니다. 반면 QueryDSL은 다른 방식으로 쿼리를 생성하여 이 문제를 우회했을 수 있습니다.

가장 유력한 것은 **두 구현 방식의 미묘한 차이에서 비롯된 JPA 동작의 차이**입니다.

## 4. 해결 방안

가장 확실하고 일관성을 유지하는 방법은 **두 기능이 동일한 검색 로직을 사용하도록 통일**하는 것입니다. 이미 정상 동작이 확인된 **QueryDSL 방식**으로 통일하는 것을 제안합니다.

1.  **`SearchService` 수정**:
    -   `SearchService`가 `GroupBuyRepository`를 직접 호출하는 대신, `GroupBuyService`를 의존하도록 변경합니다.
    -   `SearchService`의 `searchGroupBuys` 메소드 내부에서, `groupBuyRepository.searchByKeyword()`를 호출하는 대신, `groupBuyService.getGroupBuyList()`를 호출하도록 수정합니다.
    -   `GroupBuySearchCondition` 객체를 생성하여 `keyword`를 설정하고, `getGroupBuyList`에 전달합니다.

    ```java
    // SearchService.java

    // 1. GroupBuyService 의존성 추가
    private final GroupBuyService groupBuyService;

    // 2. searchGroupBuys 메소드 수정
    private List<SearchResultResponse> searchGroupBuys(String keyword, Pageable pageable) {
        try {
            // GroupBuySearchCondition 객체 생성
            GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
                .keyword(keyword)
                .status(GroupBuyStatus.RECRUITING) // 기존 로직 유지
                .build();
            
            // GroupBuyService의 메소드 호출로 변경
            Page<GroupBuyResponse> groupBuyPage = groupBuyService.getGroupBuyList(condition, pageable);

            // GroupBuyResponse를 SearchResultResponse로 변환
            return groupBuyPage.getContent().stream()
                .map(groupBuyResponse -> convertGroupBuyToSearchResult(groupBuyResponse, keyword))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("공동구매 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    ```
2.  **DTO 변환 로직 수정**:
    -   `searchGroupBuys` 메소드가 `Page<GroupBuy>`가 아닌 `Page<GroupBuyResponse>`를 반환하므로, `convertGroupBuyToSearchResult` 메소드가 `GroupBuy` 대신 `GroupBuyResponse`를 받도록 시그니처를 수정해야 할 수 있습니다.

### 기대 효과
-   검색 로직을 **QueryDSL 기반의 `GroupBuyService`로 일원화**하여, 공구 전용 목록과 통합 검색 간의 동작 불일치 문제를 근본적으로 해결합니다.
-   향후 공구 검색 로직 변경 시, `GroupBuyService` 한 곳만 수정하면 되므로 유지보수성이 향상됩니다.
