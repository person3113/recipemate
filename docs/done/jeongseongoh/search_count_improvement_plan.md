# 검색 수 집계 방식 개선 계획

## 1. 현황 (AS-IS)

- 현재 시스템에서는 사용자가 검색 API를 호출할 때마다 `SearchService`의 `saveSearchKeywordAsync` 메서드가 호출됩니다.
- 이 메서드는 `SearchKeyword` 엔티티의 `search_count`를 **매번 1씩 증가**시킵니다.
- 검색 기능은 인증 없이도 사용할 수 있도록 공개(`permitAll`)되어 있습니다.

## 2. 문제 분석

- **검색 수 인플레이션**: 한 명의 사용자가 동일한 키워드를 짧은 시간 안에 여러 번 검색하면, 검색 수가 비정상적으로 높게 집계됩니다. 예를 들어 '김치'를 5번 연속 검색하면 `search_count`가 5 증가합니다.
- **인기 검색어 순위 왜곡**: 위와 같은 이유로 실제 사용자들의 관심사와 무관하게 특정 키워드의 순위가 과도하게 높아질 수 있습니다. 이는 인기 검색어 기능의 신뢰도를 저하시킵니다.
- **의도치 않은 시스템 부하**: 불필요한 데이터베이스 `UPDATE` 연산이 빈번하게 발생하여 시스템에 미세한 부하를 추가합니다.

## 3. 개선 방안 (TO-BE)

**HTTP 세션(Session)을 활용하여 동일 세션 내 중복 검색 수 증가를 방지합니다.**

사용자가 검색을 수행하면, 서버는 사용자의 세션에 어떤 키워드를 언제 검색했는지 기록합니다. 동일한 키워드에 대한 다음 검색 요청이 들어왔을 때, 마지막 검색 시간과 현재 시간을 비교하여 일정 시간(예: 5분)이 지나지 않았다면 `search_count`를 증가시키지 않습니다.

이 방식은 다음과 같은 장점이 있습니다.
- **세션 기반**: 사용자의 브라우저 세션을 기준으로 동작하므로, 로그인 여부와 관계없이 모든 사용자에게 일관되게 적용할 수 있습니다.
- **정확성 향상**: 단시간의 반복적인 행위(새로고침, 오타 수정 후 재검색 등)를 필터링하여 더 의미 있는 검색 통계를 얻을 수 있습니다.
- **구현 용이성**: 스프링이 기본적으로 제공하는 `HttpSession`을 활용하므로, 기존 구조에 큰 변경 없이 적용할 수 있습니다.

## 4. 실행 계획

1. **`SearchController` 수정**:
    - `searchResults` 메서드의 파라미터에 `HttpServletRequest`를 추가합니다.
    - `searchService.unifiedSearch` 메서드를 호출할 때 `request` 객체를 함께 전달합니다.

2. **`SearchService` 수정**:
    - `unifiedSearch` 메서드가 `HttpServletRequest`를 받도록 시그니처를 변경합니다.
    - `saveSearchKeywordAsync`를 호출하기 전에, 세션에 기반한 중복 체크 로직을 수행합니다.

    ```java
    // SearchService.java 내 의사 코드

    public ... unifiedSearch(String keyword, Pageable pageable, HttpServletRequest request) {
        // ... 기존 검색 로직 ...

        if (shouldIncrementSearchCount(keyword, request.getSession())) {
            saveSearchKeywordAsync(keyword);
        }

        // ... 결과 반환 ...
    }

    private boolean shouldIncrementSearchCount(String keyword, HttpSession session) {
        // 세션에서 'searchedKeywords' 맵 가져오기 (없으면 생성)
        Map<String, Long> searchedKeywords = (Map<String, Long>) session.getAttribute("searchedKeywords");
        if (searchedKeywords == null) {
            searchedKeywords = new ConcurrentHashMap<>();
            session.setAttribute("searchedKeywords", searchedKeywords);
        }

        long now = System.currentTimeMillis();
        Long lastSearchedTime = searchedKeywords.get(keyword);

        // 5분(300,000ms) 이내에 검색한 기록이 없으면 카운트 증가 허용
        if (lastSearchedTime == null || (now - lastSearchedTime) > 300000) {
            searchedKeywords.put(keyword, now);
            return true; // 카운트 증가 필요
        }

        return false; // 카운트 증가 불필요
    }
    ```

    - `saveSearchKeywordAsync`는 비동기로 동작하므로, 중복 체크 로직은 동기적으로 처리되는 `unifiedSearch` 메서드 내에서 수행해야 합니다.

