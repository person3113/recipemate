# 홈 화면 개선 및 기능 통합 계획서

## 1. 개요

본 문서는 현재 `index.html`의 구성과 초기 기획서(`docs/project_plan/1_홈화면.md`)의 요구사항을 비교 분석하고, 두 버전을 통합하여 사용자 경험을 극대화하는 새로운 홈 화면을 구현하기 위한 구체적인 계획을 제안합니다.

## 2. 현황 분석

### 2.1. 현재 홈 화면 (`index.html`) 구성

현재 홈 화면은 다음과 같은 섹션으로 구성되어 있습니다.

- **Hero Section**: 서비스의 슬로건과 핵심 기능(공동구매, 레시피 찾기)으로 이동하는 메인 버튼을 제공합니다.
- **Features Section**: '레시피 기반 공구', '합리적인 가격', '신뢰 커뮤니티' 등 서비스의 특장점을 소개합니다.
- **Hot Group Purchases Section**: '인기 공동구매' 목록을 카드 형태로 보여줍니다.
- **Random Recipe Section**: '오늘의 추천 레시피'로 랜덤한 단일 레시피를 상세 정보와 함께 보여줍니다.
- **CTA Section**: 비로그인 사용자에게 회원가입/로그인을 유도합니다.

### 2.2. 초기 기획서 (`1_홈화면.md`) 요구사항

초기 기획에서는 다음과 같은 기능들을 명시하고 있습니다.

- **인기 레시피 캐러셀**: 사용자의 관심이 높은 레시피(예: 파스타, 카레)를 캐러셀(Carousel) 형태로 보여줍니다.
- **마감 임박 공구 배너**: 마감 기한이 임박한(D-1, D-2) 공동구매를 눈에 띄는 배너 형태로 노출합니다.
- **오늘의 추천 레시피**: 랜덤 레시피 3개를 카드 형태로 나열하여 보여줍니다.

### 2.3. Gap 분석

- **인기 레시피**: 현재 '인기 공동구매'는 있으나, '인기 레시피'를 보여주는 기능은 없습니다.
- **마감 임박 공구**: 현재 '인기 공동구매' 목록에서 '임박' 상태를 뱃지로 표시하고 있으나, 기획서에서 요구하는 독립적이고 강조된 '배너' 형태는 아닙니다.
- **추천 레시피**: 현재는 단일 추천 레시피만 보여주고 있어, 기획서의 '3개 레시피 표시' 요구사항과 차이가 있습니다.

## 3. 통합 및 구현 방안

### 3.1. 제안하는 홈 화면 구조

사용자에게 다양한 콘텐츠를 효과적으로 노출하고 초기 기획 의도를 반영하기 위해 다음과 같은 구조를 제안합니다.

1.  **Hero Section** (유지)
2.  **🔥 인기 레시피 캐러셀** (신규)
3.  **⏰ 마감 임박 공구 배너** (신규)
4.  **💡 오늘의 추천 레시피** (개선)
5.  **🔥 인기 공동구매** (개선)
6.  **✨ RecipeMate의 특별함** (기존 Features 섹션, 후순위 배치)
7.  **CTA Section** (유지)

### 3.2. 세부 구현 계획

#### 1. 인기 레시피 캐러셀 (신규)

-   **재사용성 검토**:
    -   `RecipeService`의 `findRecipes` 메소드 내부에 `sort="popularity"` 처리를 위한 로직이 이미 구현되어 있습니다. 이 로직은 '연관된 공동구매 수'를 기준으로 인기도를 계산하며, 이는 홈 화면의 인기 레시피 기준으로 사용하기에 매우 적합합니다.
    -   따라서, 기존 로직을 재사용하여 홈 화면에 필요한 만큼의 데이터만 간단히 조회하는 `findPopularRecipes(int size)` 메소드를 신설하는 것이 효율적입니다.

-   **Backend (Controller/Service)**:
    -   `RecipeService`에 `findPopularRecipes(int size)` 메소드를 추가합니다. 이 메소드는 기존의 인기순 정렬 로직을 활용하여, 연결된 공구 수가 많은 순으로 레시피를 조회합니다.
    -   `HomeController`에서 해당 서비스를 호출하여 `popularRecipes`라는 이름으로 Model에 데이터를 추가합니다.
    -   **예상 코드 (RecipeService):**
        ```java
        // 기존 로직을 재활용한 인기 레시피 조회
        public List<Recipe> findPopularRecipes(int size) {
            // RecipeService.findRecipes의 popularity 정렬 로직을 참고하여
            // GroupBuy와 join 후 count가 높은 순으로 정렬하는 QueryDSL 쿼리 작성
            // ...
        }
        ```

-   **Frontend (Thymeleaf)**:
    -   Bootstrap Carousel 컴포넌트를 활용하여 캐러셀 UI를 구현합니다.
    -   `th:each`를 사용해 `popularRecipes` 목록을 순회하며 캐러셀 아이템(이미지, 제목, 링크)을 생성합니다.
    -   기획서에 따라 5초 자동 슬라이드 및 좌우 컨트롤 기능을 추가합니다. (JavaScript 필요)

#### 2. 마감 임박 공구 배너 (신규)

-   **재사용성 검토**:
    -   `GroupBuyService`의 private 메소드 `determineStatus`가 마감 임박(2일 이내)을 판단하는 로직을 포함하고 있으나, 이는 상태 지정에만 사용됩니다.
    -   마감 임박 공구를 직접 조회(Query)하는 Public 메소드는 존재하지 않으므로, 계획대로 `findImminentGroupPurchases`를 신규 작성해야 합니다.

-   **Backend (Controller/Service)**:
    -   `GroupBuyService`에 마감일이 임박(예: 48시간 이내)하고 '모집중' 상태인 공구를 마감일 오름차순으로 조회하는 `findImminentGroupPurchases(int limit)` 메소드를 추가합니다.
    -   `HomeController`에서 이 메소드를 호출하여 `imminentGroupBuys` 이름으로 Model에 데이터를 추가합니다.
    -   **예상 코드 (GroupBuyRepository):**
        ```java
        // 마감일 임박 공구 조회 JPQL
        @Query("SELECT g FROM GroupBuy g WHERE g.status = 'RECRUITING' AND g.deadline BETWEEN :now AND :threshold ORDER BY g.deadline ASC")
        List<GroupBuy> findImminentGroupBuys(LocalDateTime now, LocalDateTime threshold, Pageable pageable);
        ```

-   **Frontend (Thymeleaf)**:
    -   기존 섹션과 시각적으로 구분되는 배너 컴포넌트를 디자인합니다.
    -   `th:if`를 사용하여 `imminentGroupBuys`가 비어있지 않을 때만 배너를 표시합니다.
    -   배너 클릭 시 해당 공구 상세 페이지로 이동하도록 `<a>` 태그로 전체를 감쌉니다.

#### 3. 오늘의 추천 레시피 (개선)

-   **재사용성 검토**:
    -   `RecipeService`에 `getRandomRecipes(int count)` 메소드가 이미 존재하며, DB에서 지정된 개수만큼 랜덤 레시피를 효율적으로 조회하는 기능이 완벽하게 구현되어 있습니다.
    -   따라서, 이 메소드를 그대로 재사용하면 됩니다.

-   **Backend (Controller/Service)**:
    -   `HomeController`에서 기존 `RecipeService.getRandomRecipes(3)`를 호출하고 `randomRecipes` 이름으로 Model에 데이터를 추가합니다. (기존에는 1개만 조회했으나 3개로 변경)

-   **Frontend (Thymeleaf)**:
    -   기존의 단일 카드 레이아웃을 `row`와 `col`을 사용한 3열 카드 레이아웃으로 변경합니다.
    -   `th:each`를 사용하여 `randomRecipes` 목록을 순회하며 각 레시피 카드를 생성합니다.
    -   각 카드는 이미지, 제목, 그리고 상세 페이지로 가는 링크를 포함해야 합니다.

#### 4. 인기 공동구매 (개선)

-   **'인기' 기준에 대한 심층 분석 및 기존 코드 활용 방안**:
    -   **문제점**: 단순 참여율이나 참여 인원수만으로는 '현재 인기 있는' 공구를 정확히 반영하기 어렵습니다.
    -   **해결책**: 실무에서 널리 쓰이는 **'인기 점수(Hotness Score)'** 알고리즘을 도입하여, **최신성과 참여도를 모두 반영**합니다.
        -   **`인기 점수 = (참여 인원수 - 1) / (경과 시간(시간) + 2) ^ 1.8`**
    -   **기존 코드 분석 결과**: `GroupBuyService`에 `getPopularGroupBuys`라는 메소드가 이미 존재합니다. 코드 검색 결과, 이 메소드는 현재 **실제 사용되는 곳이 없으며** 테스트 코드에서만 주석 처리된 채로 남아있습니다.
    -   **최적의 구현 방안**: 새로운 메소드를 만드는 대신, **기존의 `getPopularGroupBuys` 메소드의 내부 로직을 위의 '인기 점수' 알고리즘으로 교체**합니다. 이는 사용되지 않는 코드를 재활용하고, '인기 공구'에 대한 비즈니스 로직을 명확하게 통합하는 가장 깔끔한 방법입니다.

-   **Backend (Service)**:
    -   `GroupBuyService.getPopularGroupBuys(int limit)` 메소드를 다음과 같이 수정합니다.
    -   **구현 방안**:
        1.  **DB에서 후보군 조회**: `RECRUITING` 상태이고, 최근 N일(예: 7일) 이내에 생성된 공구들을 조회합니다.
        2.  **메모리에서 점수 계산 및 정렬**: 조회된 후보군에 대해 Java 코드로 '인기 점수'를 계산하고, 점수가 높은 순으로 정렬하여 최종 결과를 반환합니다.
    -   **예상 코드 (GroupBuyService):**
        ```java
        // getPopularGroupBuys 메소드 내부 로직 변경
        public List<GroupBuyResponse> getPopularGroupBuys(int limit) {
            // 1. DB에서 최근 일주일 내의 모집중인 공구를 100개 조회
            List<GroupBuy> candidates = groupBuyRepository.findRecentRecruitingGroupBuys(
                LocalDateTime.now().minusDays(7),
                PageRequest.of(0, 100) // 정렬은 메모리에서 하므로 DB 정렬 불필요
            );

            // 2. DTO로 변환하며 '인기 점수' 계산
            // HotGroupBuyDto는 GroupBuy와 score를 멤버로 가지는 내부 클래스 또는 레코드로 정의
            List<HotGroupBuyDto> scoredBuys = candidates.stream()
                .map(gb -> {
                    long hours = ChronoUnit.HOURS.between(gb.getCreatedAt(), LocalDateTime.now());
                    double score = (gb.getCurrentHeadcount() - 1) / Math.pow(hours + 2, 1.8);
                    return new HotGroupBuyDto(gb, score);
                })
                .sorted(Comparator.comparingDouble(HotGroupBuyDto::getScore).reversed())
                .limit(limit)
                .toList();

            // 3. 최종 Response DTO로 변환하여 반환
            // (N+1 방지를 위해 이미지, 리뷰 정보 등은 Batch 조회 후 매핑 필요)
            return scoredBuys.stream().map(dto -> mapToResponseWithStats(dto.getGroupBuy(), ...)).toList();
        }
        ```

-   **Frontend (Thymeleaf)**:
    -   `HomeController`는 수정된 `getPopularGroupBuys`를 호출하고, 템플릿은 이 데이터를 사용하여 UI를 렌더링합니다.

### 3.3. UI/UX 고려사항

-   **일관성**: 새롭게 추가되는 컴포넌트들은 기존의 디자인(폰트, 색상, 간격)과 일관성을 유지해야 합니다.
-   **반응형 디자인**: 캐러셀, 배너, 카드 그리드 모두 모바일 환경에서 적절하게 보이도록 반응형 디자인을 적용합니다.
-   **성능**: 캐러셀에 사용되는 이미지나 추가되는 데이터 요청이 페이지 로딩 속도에 미치는 영향을 최소화해야 합니다. 이미지 지연 로딩(lazy loading) 등의 기법을 고려할 수 있습니다.

## 4. 결론

위 계획에 따라 홈 화면을 개선하면, 사용자에게 더욱 풍부하고 동적인 콘텐츠를 제공할 수 있습니다. 이는 사용자의 참여를 유도하고 서비스의 핵심 가치를 효과적으로 전달하여 초기 기획 의도에 부합하는 결과물을 만들어낼 것입니다.
