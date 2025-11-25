# Render 배포 전략 분석 - 캐싱 vs 디버깅

## 1. 현재 상황 요약

### 배포 시도 타임라인
1. **v6 (첫 배포)**: `SerializationException` - 빈 리스트 직렬화 문제
2. **v7 (CacheConfig 수정)**: 메모리 부족 - 512MB 초과
3. **tmp_log8 (현재)**: 
   - 캐시 직렬화 에러 재발생
   - SQL GROUP BY 에러 (새로운 문제)

### 현재 발생 중인 2가지 에러

#### 에러 1: Redis 캐싱 직렬화 문제
```
InvalidTypeIdException: Could not resolve type id 'java.util.ImmutableCollections$ListN' 
as a subtype of `java.util.List<...>`: Configured `PolymorphicTypeValidator` denied resolution
```

**원인 분석:**
- `CacheConfig.java`에서 `allowIfSubType("com.recipemate")`로 제한
- 하지만 Java 내부 컬렉션 클래스(`ImmutableCollections$ListN`)는 `java.util` 패키지에 속함
- `com.recipemate` 패키지만 허용했기 때문에 Java 표준 컬렉션 역직렬화가 거부됨
- v6에서 제안한 `allowIfBaseType(Object.class)`가 더 적절했으나, 메모리 문제로 범위를 좁히면서 새로운 문제 발생

#### 에러 2: PostgreSQL GROUP BY 절 에러
```
ERROR: column "h1_0.id" must appear in the GROUP BY clause or be used in an aggregate function
```

**원인 분석:**
- `GroupBuyRepositoryImpl.searchGroupBuysWithReviewStats()` 메서드 (line 117-202)
- `.groupBy(groupBuy.id)` (line 176)만 지정했으나, SELECT 절에서 `groupBuy` 전체 엔티티와 `host` 조인 결과를 가져옴
- PostgreSQL은 H2보다 GROUP BY 절 검증이 엄격함
- H2(dev)에서는 동작하지만 PostgreSQL(prod)에서는 실패

**발생 위치:** `GroupBuyRepositoryImpl.java:176`
```java
.groupBy(groupBuy.id)  // 문제: host의 모든 컬럼도 그룹화 필요
```

## 2. 해결 옵션 비교

### 옵션 A: 캐싱 완전 제거 (추천)

#### 장점
1. **즉시 배포 가능**: 캐시 관련 모든 에러 회피
2. **메모리 절약**: Redis 연결 풀, 직렬화 오버헤드 제거
3. **단순성**: 디버깅/유지보수 복잡도 감소
4. **SQL 에러 집중**: GROUP BY 문제에만 집중 가능

#### 단점
1. **성능 저하**: DB 부하 증가 (특히 레시피 API 조회)
2. **기능 손실**: 조회수 캐싱 등 성능 최적화 기능 상실

#### 예상 임팩트 분석

**현재 캐싱 사용 현황:**
1. `findPopularRecipes` - **DB 조회** (연결된 공구 수 기준 인기 레시피)
2. `getCategories` - **DB 조회** (카테고리 목록)
3. `getPopularGroupBuys` - **DB 조회** (인기 공구 목록)
4. `getRecipeDetail` - **외부 API 호출** (TheMealDB/식품안전나라, 사용자가 상세 클릭 시)
5. `getRecipesByCategory` - **외부 API 호출** (TheMealDB)
6. `searchRecipes` - **외부 API 호출** (`@Deprecated`)

**캐싱 제거 시 영향도:**
- **인기 레시피 조회** (DB): 영향도 **매우 낮음**
  - QueryDSL 최적화된 쿼리, 응답 속도 빠름
- **카테고리 목록** (DB): 영향도 **매우 낮음**
  - 단순 SELECT DISTINCT 쿼리
- **인기 공구 조회** (DB): 영향도 **낮음**
  - 쿼리 최적화 되어 있음, 5분 캐시 제거 → 실시간 데이터로 개선
- **레시피 상세 조회** (외부 API): 영향도 **중간**
  - 사용자가 직접 상세 페이지 클릭 시에만 발생
  - 메인 페이지/목록 조회는 DB 기반이라 영향 없음

**결론**: **대부분 DB 조회**라서 캐싱 제거 시 성능 저하 **거의 없음**. MVP 단계에서 전혀 문제없는 수준.

---

## 3. SQL GROUP BY 문제 해결

**두 옵션 모두 이 문제는 필수 해결 사항**

### 문제 코드
```java
// GroupBuyRepositoryImpl.java:165-178
JPAQuery<GroupBuyWithReviewStatsDto> query = queryFactory
    .select(Projections.constructor(
        GroupBuyWithReviewStatsDto.class,
        groupBuy,                              // 전체 엔티티
        review.rating.avg().coalesce(0.0),
        review.count()
    ))
    .from(groupBuy)
    .leftJoin(groupBuy.host).fetchJoin()       // host도 조회됨
    .leftJoin(review).on(review.groupBuy.eq(groupBuy))
    .where(builder)
    .groupBy(groupBuy.id)                      // id만 그룹화 → 문제!
```

### 해결 방법 2가지

#### 방법 1: GROUP BY에 모든 컬럼 추가 (간단하지만 비효율적)
```java
.groupBy(groupBuy.id, groupBuy.host.id, 
         groupBuy.host.email, groupBuy.host.nickname, /* ... 모든 컬럼 */)
```
- **문제**: 컬럼 많으면 비효율적, 유지보수 어려움

#### 방법 2: 서브쿼리로 분리 (추천)
```java
// 1단계: 리뷰 통계만 먼저 조회
Map<Long, ReviewStats> reviewStatsMap = queryFactory
    .select(groupBuy.id, review.rating.avg(), review.count())
    .from(groupBuy)
    .leftJoin(review).on(review.groupBuy.eq(groupBuy))
    .where(builder)
    .groupBy(groupBuy.id)
    .fetch()
    .stream()
    .collect(Collectors.toMap(...));

// 2단계: 엔티티 조회 후 통계 결합
List<GroupBuy> groupBuys = queryFactory
    .selectFrom(groupBuy)
    .leftJoin(groupBuy.host).fetchJoin()
    .where(builder.and(groupBuy.id.in(reviewStatsMap.keySet())))
    .fetch();

// 3단계: DTO 생성
List<GroupBuyWithReviewStatsDto> result = groupBuys.stream()
    .map(gb -> new GroupBuyWithReviewStatsDto(
        gb, 
        reviewStatsMap.get(gb.getId()).avgRating(),
        reviewStatsMap.get(gb.getId()).count()
    ))
    .collect(Collectors.toList());
```

---

## 4. 최종 권장 전략

### 🎯 추천: **옵션 A (캐싱 제거) + SQL 수정**

#### 캐싱 비활성화 작동 원리

`application.yml`이나 Render 환경 변수에서 `spring.cache.type=none`으로 설정하면, 스프링 부트 애플리케이션은 캐싱 기능을 **완전히 비활성화**합니다. 이 설정은 Redis를 포함한 어떤 캐시 프로바이더와도 연결을 시도하지 않도록 만듭니다.

따라서 `REDIS_HOST` 같은 환경변수가 설정되어 있더라도 애플리케이션이 그 값을 읽거나 사용하지 않으므로, 배포 시 아무런 영향을 주지 않습니다.

**중요한 점:**
- `CacheConfig` 빈이 생성되지 않음 (`@ConditionalOnProperty` 조건 불만족)
- Redis 연결 풀이 생성되지 않아 메모리 절약
- `@Cacheable` 어노테이션이 붙은 메서드는 캐싱 없이 매번 실행됨
- 기존 `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` 환경변수는 그대로 두어도 무방

#### 실행 순서
1. **즉시**: 캐싱 비활성화
   - **Render 환경변수 추가 (권장)**: `SPRING_CACHE_TYPE=none`
   - 또는 `application.yml` prod 프로파일: `spring.cache.type: none`
   - **기존 Redis 환경변수는 삭제하지 않아도 됨**

2. **즉시**: SQL GROUP BY 문제 수정
   - `GroupBuyRepositoryImpl.searchGroupBuysWithReviewStats()` 리팩토링
   - 방법 2(서브쿼리) 적용

3. **배포 및 검증**: 핵심 기능 동작 확인

#### 이 전략을 선택하는 이유
1. **MVP 우선**: 일단 동작하는 서비스 배포가 최우선
2. **위험 최소화**: 확실한 해결책 선택
3. **시간 효율**: 1회 배포로 해결 가능
4. **확장성**: 추후 트래픽 증가 시 캐싱 재도입 가능
5. **메모리 여유**: 512MB 내에서 안정적 운영 가능

---

## 6. 결론

현재 상황에서는 **캐싱 제거(옵션 A)**가 합리적입니다.

### 근거
1. MVP 단계에서 성능보다 안정성이 중요
2. 캐싱 디버깅은 시간 대비 불확실성이 큼
3. SQL 문제는 어차피 해결 필수
4. 512MB 메모리는 기본 Spring Boot 앱에는 충분함

### 다음 액션 아이템
- [ ] Render 서비스에 환경변수 `SPRING_CACHE_TYPE=none` 추가
- [ ] `GroupBuyRepositoryImpl.searchGroupBuysWithReviewStats()` 수정
- [ ] 배포 및 기능 테스트
- [ ] 성능 모니터링 (선택사항)

### 요약
1. **`SPRING_CACHE_TYPE=none` 환경변수만 Render 서비스에 설정하세요.**
2. **기존 REDIS_HOST, REDIS_PORT 변수는 그대로 두셔도 됩니다.**
3. **이제 코드에서 GROUP BY 문제를 해결하는 데 집중하시면 됩니다.**

**추가 질문이 있다면 말씀해 주세요!**
