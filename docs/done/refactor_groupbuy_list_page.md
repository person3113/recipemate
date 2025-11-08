# 공구 목록 페이지 개선 계획

## 📋 요구사항 정리

```text
3. 공구 목록 페이지
- **검색 범위**: 공구 제목 + 내용 + 재료명
- http://localhost:8080/recipes 여기처럼  키워드: 고기
 재료:
 카테고리:
 이런 식으로 표시되고. "필터해제" 버튼도 있었으면 좋겠음. 해당 템플릿 참고해서
 - ### 4) 정렬 변경 추가. 

- **최신순**: 등록일 최신순
- **마감임박순**: 마감일 가까운 순 (D-1 → D-2 → D-3...)
- **가격순**: 1인당 가격 낮은 순
- **참여자순**: 현재 참여 인원 많은 순
- 내림차순, 오름차순 둘 다 가능하도록.

- 공구 목록에서 각 공구 카드에 추가해야 할 거. 1인당 가격. 마감일 (D-day, D-1이면 🔥 표시), 수령 방법 (직거래 장소 또는 "택배 가능")
현재 관련 코드베이스 점검 및 분석하고. 어떻게 수정하면 될 지 docs 폴더 속에 md 파일에 작성해줘.
```

### 1. 검색 범위 확대
- **현재**: 제목 + 내용
- **개선**: 제목 + 내용 + **재료명** 추가

### 2. 필터 표시 영역 추가
- recipes 페이지처럼 현재 적용된 필터를 표시하는 영역 추가
- 표시 항목:
  - 키워드
  - 재료 (새로 추가)
  - 카테고리
- "필터 해제" 버튼 추가

### 3. 정렬 옵션 추가
현재는 `createdAt` 기준 DESC만 가능
- **최신순**: 등록일 기준 (createdAt)
- **마감임박순**: 마감일 가까운 순 (deadline)
- **가격순**: 1인당 가격 낮은 순 (totalPrice / targetHeadcount)
- **참여자순**: 현재 참여 인원 많은 순 (currentHeadcount)
- 각 정렬에 대해 오름차순/내림차순 선택 가능

### 4. 공구 카드 정보 추가
각 카드에 표시할 추가 정보:
- **1인당 가격**: totalPrice / targetHeadcount
- **마감일**: D-day 표시 (D-1이면 🔥 아이콘)
- **수령 방법**: 직거래 장소 또는 "택배 가능"

---

## 🔍 현재 코드베이스 분석

### 1. 관련 파일 목록
- **Controller**: `GroupBuyController.java:55-88` - listPage 메서드
- **Service**: `GroupBuyService.java:218-245` - getGroupBuyList 메서드
- **Repository**: `GroupBuyRepositoryImpl.java:32-105` - searchGroupBuys 메서드
- **DTO**: 
  - `GroupBuySearchCondition.java` - 검색 조건
  - `GroupBuyResponse.java` - 응답 데이터
- **Entity**: `GroupBuy.java` - 공구 엔티티
- **Template**: `templates/group-purchases/list.html`
- **참고 Template**: `templates/recipes/list.html:24-43` - 필터 표시 영역

### 2. 현재 구조

#### GroupBuySearchCondition (현재)
```java
- category: GroupBuyCategory
- status: GroupBuyStatus  
- recipeOnly: Boolean
- keyword: String // 제목 또는 내용 검색
```

#### GroupBuyRepositoryImpl.searchGroupBuys (현재)
```java
// 키워드 검색 (42-47행)
if (StringUtils.hasText(condition.getKeyword())) {
    builder.and(
        groupBuy.title.containsIgnoreCase(condition.getKeyword())
            .or(groupBuy.content.containsIgnoreCase(condition.getKeyword()))
    );
}

// 정렬 (82-100행)
// createdAt, deadline, title, currentHeadcount만 지원
```

#### GroupBuy Entity (관련 필드)
```java
- ingredients: String // 재료 목록 (있으면 검색 가능)
- deadline: LocalDateTime
- totalPrice: Integer
- targetHeadcount: Integer
- currentHeadcount: Integer
- deliveryMethod: DeliveryMethod (DIRECT, PARCEL, BOTH)
- meetupLocation: String
```

#### GroupBuyResponse (현재)
```java
// 이미 모든 필요한 정보 포함
- totalPrice, targetHeadcount, currentHeadcount
- deadline
- deliveryMethod, meetupLocation, parcelFee
```

---

## 🛠️ 수정 계획

### 1단계: 백엔드 - 검색 및 정렬 개선

#### 1.1 GroupBuySearchCondition 수정
**파일**: `GroupBuySearchCondition.java`

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupBuySearchCondition {
    private GroupBuyCategory category;
    private GroupBuyStatus status;
    
    @Builder.Default
    private Boolean recipeOnly = false;
    
    private String keyword;      // 제목 + 내용 검색
    private String ingredients;  // 재료명 검색 (새로 추가)
    
    // 정렬 관련 (새로 추가)
    private String sortBy;       // latest, deadline, price, participants
    private String direction;    // asc, desc
}
```

**변경사항**:
- `ingredients` 필드 추가
- `sortBy`, `direction` 필드 추가

---

#### 1.2 GroupBuyRepositoryImpl 수정
**파일**: `GroupBuyRepositoryImpl.java`

**1.2.1 검색 조건 추가 (42-47행 이후)**
```java
// 기존: 키워드 검색 (제목 또는 내용)
if (StringUtils.hasText(condition.getKeyword())) {
    builder.and(
        groupBuy.title.containsIgnoreCase(condition.getKeyword())
            .or(groupBuy.content.containsIgnoreCase(condition.getKeyword()))
    );
}

// 새로 추가: 재료명 검색
if (StringUtils.hasText(condition.getIngredients())) {
    builder.and(groupBuy.ingredients.containsIgnoreCase(condition.getIngredients()));
}
```

**1.2.2 정렬 로직 개선 (82-100행 수정)**
현재 코드는 Pageable의 Sort를 사용하지만, 1인당 가격 정렬은 계산 필드이므로 커스텀 정렬 필요.

```java
// sortBy와 direction 파라미터 기반 정렬
String sortBy = condition.getSortBy() != null ? condition.getSortBy() : "latest";
String direction = condition.getDirection() != null ? condition.getDirection() : "desc";
Order sortOrder = "asc".equals(direction) ? Order.ASC : Order.DESC;

OrderSpecifier<?> orderSpecifier = switch (sortBy) {
    case "latest" -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
    case "deadline" -> new OrderSpecifier<>(sortOrder, groupBuy.deadline);
    case "participants" -> new OrderSpecifier<>(sortOrder, groupBuy.currentHeadcount);
    case "price" -> {
        // 1인당 가격 = totalPrice / targetHeadcount
        // QueryDSL에서 나눗셈 연산 사용
        NumberExpression<Double> pricePerPerson = 
            groupBuy.totalPrice.doubleValue().divide(groupBuy.targetHeadcount.doubleValue());
        yield new OrderSpecifier<>(sortOrder, pricePerPerson);
    }
    default -> new OrderSpecifier<>(sortOrder, groupBuy.createdAt);
};

query.orderBy(orderSpecifier);

// Pageable의 Sort는 무시하고 condition의 정렬 사용
```

**변경사항**:
- 재료 검색 조건 추가
- 커스텀 정렬 로직 구현 (특히 1인당 가격 계산 정렬)

---

#### 1.3 GroupBuyController 수정
**파일**: `GroupBuyController.java:55-88`

```java
@GetMapping("/list")
public String listPage(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) GroupBuyStatus status,
    @RequestParam(required = false, defaultValue = "false") Boolean recipeOnly,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String ingredients,  // 새로 추가
    @RequestParam(required = false, defaultValue = "latest") String sortBy,  // 새로 추가
    @RequestParam(required = false, defaultValue = "desc") String direction,  // 새로 추가
    @PageableDefault(size = 20) Pageable pageable,  // sort 제거 - condition에서 처리
    Model model
) {
    GroupBuyCategory categoryEnum = null;
    if (category != null && !category.isBlank()) {
        try {
            categoryEnum = GroupBuyCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category value: {}", category);
        }
    }
    
    GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
        .category(categoryEnum)
        .status(status)
        .recipeOnly(recipeOnly)
        .keyword(keyword)
        .ingredients(ingredients)  // 새로 추가
        .sortBy(sortBy)            // 새로 추가
        .direction(direction)      // 새로 추가
        .build();
    
    Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);
    model.addAttribute("groupBuys", result);
    model.addAttribute("searchCondition", condition);
    model.addAttribute("categories", GroupBuyCategory.values());
    
    return "group-purchases/list";
}
```

**변경사항**:
- `ingredients`, `sortBy`, `direction` 파라미터 추가
- PageableDefault에서 sort 제거 (condition에서 직접 처리)

---

### 2단계: 프론트엔드 - 템플릿 개선

#### 2.1 필터 표시 영역 추가
**파일**: `templates/group-purchases/list.html`

**위치**: 38행 (검색 폼 위) - recipes/list.html의 24-43행 참고

```html
<!-- Filter Indicators -->
<div th:if="${searchCondition.keyword != null or searchCondition.ingredients != null or searchCondition.category != null}" 
     class="alert alert-primary mb-4">
    <div class="d-flex flex-wrap align-items-center gap-3">
        <div th:if="${searchCondition.keyword != null}">
            <i class="bi bi-search me-2"></i>
            키워드: <strong th:text="${searchCondition.keyword}"></strong>
        </div>
        <div th:if="${searchCondition.ingredients != null}">
            <i class="bi bi-egg-fill me-2"></i>
            재료: <strong th:text="${searchCondition.ingredients}"></strong>
        </div>
        <div th:if="${searchCondition.category != null}">
            <i class="bi bi-tag-fill me-2"></i>
            카테고리: <strong th:text="${searchCondition.category.displayName}"></strong>
        </div>
        <a href="/group-purchases/list" class="btn btn-sm btn-outline-primary">
            <i class="bi bi-x-circle"></i> 필터 해제
        </a>
    </div>
</div>
```

---

#### 2.2 검색 폼 개선
**파일**: `templates/group-purchases/list.html:38-77`

```html
<!-- Search Form -->
<div class="card mb-4">
    <div class="card-body">
        <form method="get" action="/group-purchases/list" class="row g-3">
            <!-- 키워드 검색 -->
            <div class="col-md-3">
                <input type="text" name="keyword" class="form-control" 
                       th:value="${searchCondition.keyword}" 
                       placeholder="제목 또는 내용 검색">
            </div>
            
            <!-- 재료 검색 (새로 추가) -->
            <div class="col-md-3">
                <input type="text" name="ingredients" class="form-control" 
                       th:value="${searchCondition.ingredients}" 
                       placeholder="재료명 검색 (예: 소고기, 양파)">
            </div>
            
            <!-- 카테고리 선택 -->
            <div class="col-md-2">
                <select name="category" class="form-select">
                    <option value="">전체 카테고리</option>
                    <option th:each="cat : ${categories}" 
                            th:value="${cat.name()}" 
                            th:text="${cat.displayName}"
                            th:selected="${searchCondition.category?.name() == cat.name()}"></option>
                </select>
            </div>
            
            <!-- 상태 선택 -->
            <div class="col-md-2">
                <select name="status" class="form-select">
                    <option value="">전체 상태</option>
                    <option value="RECRUITING" th:selected="${searchCondition.status?.name() == 'RECRUITING'}">모집중</option>
                    <option value="IMMINENT" th:selected="${searchCondition.status?.name() == 'IMMINENT'}">마감 임박</option>
                    <option value="CLOSED" th:selected="${searchCondition.status?.name() == 'CLOSED'}">마감</option>
                </select>
            </div>
            
            <!-- 레시피 기반 필터 -->
            <div class="col-md-2">
                <div class="form-check mt-2">
                    <input type="checkbox" name="recipeOnly" class="form-check-input" id="recipeOnly"
                           th:checked="${searchCondition.recipeOnly}">
                    <label class="form-check-label" for="recipeOnly">레시피 기반만</label>
                </div>
            </div>
            
            <!-- 정렬 기준 (새로 추가) -->
            <div class="col-md-3">
                <select name="sortBy" class="form-select">
                    <option value="latest" th:selected="${searchCondition.sortBy == null or searchCondition.sortBy == 'latest'}">최신순</option>
                    <option value="deadline" th:selected="${searchCondition.sortBy == 'deadline'}">마감임박순</option>
                    <option value="price" th:selected="${searchCondition.sortBy == 'price'}">가격순</option>
                    <option value="participants" th:selected="${searchCondition.sortBy == 'participants'}">참여자순</option>
                </select>
            </div>
            
            <!-- 정렬 방향 (새로 추가) -->
            <div class="col-md-2">
                <select name="direction" class="form-select">
                    <option value="desc" th:selected="${searchCondition.direction == null or searchCondition.direction == 'desc'}">내림차순 ↓</option>
                    <option value="asc" th:selected="${searchCondition.direction == 'asc'}">오름차순 ↑</option>
                </select>
            </div>
            
            <!-- 검색 버튼 -->
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="bi bi-search"></i> 검색
                </button>
            </div>
        </form>
    </div>
</div>
```

---

#### 2.3 공구 카드 개선
**파일**: `templates/group-purchases/list.html:92-127`

현재 카드에 추가할 정보:
1. 1인당 가격
2. 마감일 (D-day 표시, D-1이면 🔥)
3. 수령 방법

```html
<div th:if="${!groupBuys.isEmpty()}" class="row">
    <div th:each="groupBuy : ${groupBuys}" class="col-md-6 col-lg-4 mb-4">
        <div class="card h-100 shadow-sm group-buy-item">
            <div class="card-body">
                <!-- 제목과 상태 배지 -->
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h5 class="card-title mb-0">
                        <a th:href="@{/group-purchases/{id}(id=${groupBuy.id})}" 
                           class="text-decoration-none text-dark"
                           th:text="${groupBuy.title}"></a>
                    </h5>
                    <span class="badge" 
                          th:classappend="${groupBuy.status.name() == 'RECRUITING'} ? 'bg-success' : (${groupBuy.status.name() == 'IMMINENT'} ? 'bg-warning' : 'bg-secondary')"
                          th:text="${groupBuy.status.name() == 'RECRUITING' ? '모집중' : (groupBuy.status.name() == 'IMMINENT' ? '마감 임박' : '마감')}"></span>
                </div>
                
                <!-- 내용 -->
                <p class="card-text text-muted small" th:text="${#strings.abbreviate(groupBuy.content, 60)}"></p>
                
                <!-- 카테고리 -->
                <div class="mb-2">
                    <span class="badge bg-light text-dark border">
                        <i class="bi bi-tag"></i> <span th:text="${groupBuy.category.displayName}"></span>
                    </span>
                </div>
                
                <!-- 가격 정보 (개선) -->
                <div class="mb-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-primary fw-bold">
                            총 <span th:text="${#numbers.formatInteger(groupBuy.totalPrice, 0, 'COMMA')}"></span>원
                        </span>
                        <span class="text-success fw-bold">
                            <!-- 1인당 가격 (새로 추가) -->
                            1인당 <span th:text="${#numbers.formatInteger(groupBuy.totalPrice / groupBuy.targetHeadcount, 0, 'COMMA')}"></span>원
                        </span>
                    </div>
                </div>
                
                <!-- 참여 인원 -->
                <div class="mb-2">
                    <span class="text-muted small">
                        <i class="bi bi-people"></i>
                        <span th:text="${groupBuy.currentHeadcount}"></span> / <span th:text="${groupBuy.targetHeadcount}"></span>명
                    </span>
                </div>
                
                <!-- 마감일 (새로 추가) -->
                <div class="mb-2">
                    <th:block th:with="now=${#temporals.createNow()}, 
                                       daysUntil=${#temporals.daysBetween(now, groupBuy.deadline)}">
                        <span class="text-muted small">
                            <i class="bi bi-calendar-event"></i>
                            <span th:if="${daysUntil == 0}">
                                오늘 마감 🔥
                            </span>
                            <span th:if="${daysUntil == 1}">
                                D-1 🔥
                            </span>
                            <span th:if="${daysUntil > 1}">
                                D-<span th:text="${daysUntil}"></span>
                            </span>
                            <span th:if="${daysUntil < 0}">
                                마감
                            </span>
                        </span>
                    </th:block>
                </div>
                
                <!-- 수령 방법 (새로 추가) -->
                <div class="mb-2">
                    <span class="text-muted small">
                        <i class="bi bi-truck"></i>
                        <th:block th:switch="${groupBuy.deliveryMethod.name()}">
                            <span th:case="'DIRECT'" th:text="${groupBuy.meetupLocation}"></span>
                            <span th:case="'PARCEL'">택배 가능</span>
                            <span th:case="'BOTH'">
                                <span th:text="${groupBuy.meetupLocation}"></span> 또는 택배
                            </span>
                        </th:block>
                    </span>
                </div>
                
                <!-- 주최자 정보 -->
                <div class="text-muted small">
                    <i class="bi bi-person"></i> <span th:text="${groupBuy.hostNickname}"></span>
                </div>
            </div>
        </div>
    </div>
</div>
```

---

#### 2.4 페이지네이션 개선
**파일**: `templates/group-purchases/list.html:129-148`

페이지네이션 링크에 검색 조건 유지:

```html
<!-- Pagination -->
<nav th:if="${!groupBuys.isEmpty()}" aria-label="Page navigation" class="mt-4">
    <ul class="pagination justify-content-center">
        <li class="page-item" th:classappend="${!groupBuys.hasPrevious()} ? 'disabled'">
            <a class="page-link" 
               th:href="@{/group-purchases/list(
                   keyword=${searchCondition.keyword},
                   ingredients=${searchCondition.ingredients},
                   category=${searchCondition.category?.name()},
                   status=${searchCondition.status?.name()},
                   recipeOnly=${searchCondition.recipeOnly},
                   sortBy=${searchCondition.sortBy},
                   direction=${searchCondition.direction},
                   page=${groupBuys.number - 1})}">
                <i class="bi bi-chevron-left"></i> 이전
            </a>
        </li>
        <li class="page-item active">
            <span class="page-link">
                <span th:text="${groupBuys.number + 1}"></span> / <span th:text="${groupBuys.totalPages}"></span>
            </span>
        </li>
        <li class="page-item" th:classappend="${!groupBuys.hasNext()} ? 'disabled'">
            <a class="page-link" 
               th:href="@{/group-purchases/list(
                   keyword=${searchCondition.keyword},
                   ingredients=${searchCondition.ingredients},
                   category=${searchCondition.category?.name()},
                   status=${searchCondition.status?.name()},
                   recipeOnly=${searchCondition.recipeOnly},
                   sortBy=${searchCondition.sortBy},
                   direction=${searchCondition.direction},
                   page=${groupBuys.number + 1})}">
                다음 <i class="bi bi-chevron-right"></i>
            </a>
        </li>
    </ul>
</nav>
```

---

## 📝 구현 순서

### Phase 1: 백엔드 수정 (우선순위: 높음)
1. ✅ `GroupBuySearchCondition.java` - ingredients, sortBy, direction 필드 추가
2. ✅ `GroupBuyRepositoryImpl.java` - 재료 검색 조건 및 정렬 로직 개선
3. ✅ `GroupBuyController.java` - 파라미터 추가

### Phase 2: 프론트엔드 수정 (우선순위: 높음)
4. ✅ `list.html` - 필터 표시 영역 추가
5. ✅ `list.html` - 검색 폼 개선 (재료 검색, 정렬 옵션)
6. ✅ `list.html` - 공구 카드 개선 (1인당 가격, 마감일, 수령 방법)
7. ✅ `list.html` - 페이지네이션 링크 개선

### Phase 3: 테스트 및 검증 (우선순위: 중간)
8. ⬜ 재료 검색 기능 테스트
9. ⬜ 정렬 기능 테스트 (특히 1인당 가격 정렬)
10. ⬜ 필터 표시 및 해제 기능 테스트

---

## ⚠️ 주의사항

### 1. 재료 검색 제한
- `ingredients` 필드가 null인 공구도 있을 수 있음
- 검색 시 null 체크 필요

### 2. 1인당 가격 계산
- `targetHeadcount`가 0일 수 없지만 (최소 2명), 방어 코드 필요
- QueryDSL에서 나눗셈 연산 시 정수/실수 변환 주의

### 3. 마감일 계산
- Thymeleaf에서 날짜 계산 시 타임존 고려
- `#temporals.daysBetween`는 LocalDateTime 비교 시 날짜만 비교

### 4. 수령 방법 표시
- `meetupLocation`이 null일 수 있음 (PARCEL only인 경우)
- BOTH인 경우 meetupLocation 필수 검증

### 5. 정렬 기본값
- sortBy 파라미터가 없으면 "latest" 사용
- direction 파라미터가 없으면 "desc" 사용

---

## 기대 동작

### 1. 재료 검색
- [ ] "소고기"로 검색 시 ingredients에 "소고기" 포함된 공구만 표시
- [ ] 재료가 없는 일반 공구는 재료 검색 시 제외됨
- [ ] 대소문자 구분 없이 검색됨

### 2. 정렬 기능
- [ ] 최신순: 최근 등록된 공구가 먼저 표시
- [ ] 마감임박순: deadline이 가까운 순서대로 표시
- [ ] 가격순: 1인당 가격이 낮은 순서대로 표시
- [ ] 참여자순: currentHeadcount가 많은 순서대로 표시
- [ ] 오름차순/내림차순 전환 정상 작동

### 3. 필터 표시
- [ ] 키워드 입력 시 필터 표시 영역에 표시됨
- [ ] 재료 입력 시 필터 표시 영역에 표시됨
- [ ] 카테고리 선택 시 displayName으로 표시됨
- [ ] "필터 해제" 클릭 시 모든 필터 초기화

### 4. 카드 정보
- [ ] 1인당 가격이 정확하게 계산되어 표시됨
- [ ] D-day가 정확하게 표시됨 (D-0, D-1 등)
- [ ] D-1 이하일 때 🔥 아이콘 표시됨
- [ ] 수령 방법이 정확하게 표시됨

### 5. 페이지네이션
- [ ] 페이지 이동 시 검색 조건이 유지됨
- [ ] 정렬 옵션이 유지됨

---

## 📊 예상 영향도

### 변경 범위
- **백엔드**: 3개 파일 (DTO, Repository, Controller)
- **프론트엔드**: 1개 파일 (Template)

### 하위 호환성
- ✅ 기존 API 호환 (새로운 파라미터는 optional)
- ✅ 기존 검색 기능 유지

### 성능 영향
- ⚠️ 재료 검색: `ingredients` 컬럼에 인덱스 없음 → 필요 시 추가 고려
- ⚠️ 1인당 가격 정렬: 실시간 계산 → 캐싱 고려

---

## 🔧 선택적 최적화

### 1. 재료 검색 성능 개선
**문제**: `ingredients` 컬럼에 LIKE 검색 시 인덱스 미사용
**해결책**: Full-text search 인덱스 추가 또는 재료 테이블 정규화

```sql
-- H2에서는 Full-text search 제한적 지원
-- 필요 시 별도 재료 테이블로 분리 고려
CREATE INDEX idx_groupbuy_ingredients ON group_buys(ingredients);
```

### 2. 1인당 가격 캐싱
**문제**: 매번 실시간 계산
**해결책**: GroupBuyResponse에 계산된 값 추가

```java
// GroupBuyResponse.java
private Integer pricePerPerson; // totalPrice / targetHeadcount

// GroupBuyService.mapToResponse()
.pricePerPerson(groupBuy.getTotalPrice() / groupBuy.getTargetHeadcount())
```

