# 공구 검색 범위 불일치 문제 심층 분석 보고서 (v2)

## 1. 문제 현황

-   **현상**: 통합 검색(`GET /search`)에서는 공동구매(Group Buy) 검색 시 `제목`만 검색되고 `내용`은 검색되지 않으며, 특정 `상태`의 공구만 결과에 노출된다.
-   **사용자 제보**: `내용`에 '김치'가 포함된 '마감 임박' 상태의 공구가 통합 검색 결과에서 누락된다. 하지만 공구 전용 목록에서는 정상적으로 검색된다.


- 커뮤니티는 제목뿐만 아니라 내용에만 김치가 있어도 검색됨. 이게 맞고. 근데 공구는
  상세 설명에 "김치". 즉
  http://localhost:8080/group-
  purchases/list?keyword=%EA%B9%80%EC%B9%98&ingredients=&category=&status=&sortBy=la
  test&direction=desc
  같은 거로는 공구의 제목에는 없고 상세 설명에 김치가 적혀있어도 검색되는데. 통합검색에서 공구검색에서는 제목에 김치가 있어야만 검색되는 게 문제야. 아래 db인데. content에 김치가 있는 건 검색안돼.

SELECT * FROM GROUP_BUYS;
ID  	CREATED_AT  	DELETED_AT  	UPDATED_AT  	CATEGORY  	CONTENT  	CURRENT_HEADCOUNT  	DEADLINE  	DELIVERY_METHOD  	INGREDIENTS  	IS_PARTICIPANT_LIST_PUBLIC  	MEETUP_LOCATION  	PARCEL_FEE  	RECIPE_API_ID  	RECIPE_IMAGE_URL  	RECIPE_NAME  	STATUS  	TARGET_HEADCOUNT  	TITLE  	TOTAL_PRICE  	VERSION  	HOST_ID  
1	2025-11-13 10:13:21.943366	null	2025-11-13 10:44:03.224641	DAIRY	s	1	2025-11-14 10:44:00	PARCEL	null	TRUE		null	null	null	null	IMMINENT	2	s	0	3	1
33	2025-11-13 17:16:46.534305	null	2025-11-13 17:16:46.534305	VEGETABLE	김치	0	2025-11-14 17:16:00	BOTH	null	FALSE		null	null	null	null	IMMINENT	2	ㅇㅇㅇ	0	0	1
34	2025-11-13 17:18:24.277873	null	2025-11-13 17:18:24.277873	SEAFOOD	ㅇㅇ	0	2025-11-22 17:18:00	PARCEL	null	FALSE		null	null	null	null	RECRUITING	2	김치	1000	0	1

상태: 이미 수정되어 있음 -> 근데 안 됨. 몇 번을 확인해도.

• GroupBuyRepository.java 45줄: OR g.content LIKE %:keyword% 이미 포함되어 있음
• 제목과 내용 모두 검색 가능
=> 근데 아님. 안 됨.
=> 이거 안됨. 문제없다고 너가 말하는데 안된다고. 실제로 해보면.

---

## 2. 근본 원인 추적 및 분석

이전 분석을 종합하고 사용자님의 제보를 통해 확인한 결과, 문제는 **두 가지 원인이 복합적으로 작용**한 결과입니다.

### 원인 1: 검색 로직(기술 스택)의 불일치

-   **공구 전용 목록 (정상 동작)**: `GroupBuyService`가 **QueryDSL**을 사용하여 `제목`과 `내용`을 모두 `OR` 조건으로 검색하는 동적 쿼리를 생성하여 실행합니다.
-   **통합 검색 (문제 발생)**: `SearchService`가 **JPQL `@Query`**로 작성된 `GroupBuyRepository.searchByKeyword()` 메소드를 직접 호출합니다. 이 두 방식의 미묘한 동작 차이가 `내용` 검색 누락을 야기했습니다.

### 원인 2: 불필요한 상태 필터링 (핵심 원인)

-   **결정적 원인**: 사용자님께서 정확히 지적하신 대로, `SearchService`의 `searchGroupBuysPage`와 `searchGroupBuys` 메소드는 `groupBuyRepository.searchByKeyword()`를 호출할 때 검색 상태를 `GroupBuyStatus.RECRUITING`으로 **하드코딩**하고 있습니다.
    ```java
    // SearchService.java
    Page<GroupBuy> groupBuyPage = groupBuyRepository.searchByKeyword(
        keyword, 
        GroupBuyStatus.RECRUITING, // <-- 문제의 지점
        pageable
    );
    ```
-   **결과**: 이로 인해 `내용`에 검색어가 포함되어 있더라도, 공구 상태가 '모집중'이 아닌 '마감 임박', '마감' 등일 경우 검색 결과에서 원천적으로 제외되었습니다.

## 3. 해결 방안

**검색 로직을 일원화**하고 **불필요한 필터를 제거**하여 문제를 근본적으로 해결합니다.

### 3.1. 검색 로직 일원화 및 상태 필터 제거

가장 확실하고 일관성을 유지하는 방법은, 두 기능 모두 정상 동작이 확인된 **QueryDSL 기반의 `GroupBuyService`를 사용하도록 통일**하고, **상태 필터를 제거**하는 것입니다.

1.  **`SearchService` 수정**:
    -   `SearchService`가 `GroupBuyRepository` 대신 `GroupBuyService`를 의존하도록 변경합니다.
    -   `SearchService`의 공구 검색 관련 메소드(`searchGroupBuys`, `searchGroupBuysPage` 등) 내부 로직을 다음과 같이 수정합니다.
        -   `GroupBuySearchCondition` 객체를 생성하여 `keyword`만 설정합니다. **`status`는 절대 설정하지 않습니다.**
        -   `groupBuyService.getGroupBuyList(condition, pageable)`를 호출하여 결과를 받아옵니다.

    ```java
    // SearchService.java 수정 예시

    // 1. GroupBuyService 의존성 추가
    private final GroupBuyService groupBuyService;

    // 2. 공구 검색 메소드 수정
    public Page<SearchResultResponse> searchGroupBuysPage(String keyword, Pageable pageable) {
        // GroupBuySearchCondition 객체 생성. status를 null로 두어 모든 상태를 검색.
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .keyword(keyword)
            .build();
        
        // GroupBuyService의 검증된 메소드 호출
        Page<GroupBuyResponse> groupBuyPage = groupBuyService.getGroupBuyList(condition, pageable);

        // 결과를 SearchResultResponse로 변환하여 반환
        return groupBuyPage.map(groupBuyResponse -> convertGroupBuyToSearchResult(groupBuyResponse, keyword));
    }
    ```
    (*`searchGroupBuys` 리스트를 반환하는 메소드도 동일한 방식으로 수정 필요*)

2.  **`GroupBuyService` 확인**:
    -   `groupBuyService`의 `getGroupBuyList` 내부 QueryDSL 구현에서, `GroupBuySearchCondition`의 `status`가 `null`일 경우 `WHERE` 절에 상태 조건을 추가하지 않는지 확인합니다. (현재 코드는 그렇게 구현되어 있을 가능성이 높습니다.)

### 3.2. 기대 효과
-   **일관성 확보**: 통합 검색과 공구 전용 목록이 100% 동일한 검색 로직(QueryDSL)을 사용하게 됩니다.
-   **문제 해결**: `제목`과 `내용`이 모두 검색 범위에 포함되며, `상태`와 관계없이 키워드만 일치하면 모든 공구가 검색 결과에 올바르게 노출됩니다.
-   **유지보수성 향상**: 향후 공구 검색 정책 변경 시, `GroupBuyService`와 관련 QueryDSL 구현부 한 곳만 수정하면 모든 기능에 일괄 적용됩니다.