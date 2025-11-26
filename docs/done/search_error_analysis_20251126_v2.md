### 통합 검색 기능 2차 오류 분석 및 해결 방안 (2025-11-26)

이전 `read-only transaction` 오류 해결을 위해 트랜잭션을 분리하는 코드를 적용한 후, 새로운 유형의 오류가 발생했습니다. 이 문서는 해당 오류의 원인을 분석하고 해결 방안을 제시합니다.

---

#### 1. 오류 현상

검색어를 저장하는 과정에서 `TransactionRequiredException`이 발생합니다. 이는 주로 데이터베이스에 변경(update/delete)을 가하는 작업을 트랜잭션 없이 실행하려고 할 때 발생합니다.

- **발생 시점**: 이미 데이터베이스에 존재하는 검색어를 다시 검색했을 때. 즉, `search_keywords` 테이블의 `search_count`를 1 증가시키는 `UPDATE` 쿼리 실행 시점.
- **정상 동작**: 데이터베이스에 없는 새로운 검색어는 `INSERT` 구문이 정상적으로 실행됩니다.

#### 2. 관련 오류 로그

```log
2025-11-26T13:32:14.662+09:00 ERROR 16324 --- [RecipeMate] [nio-8080-exec-7] c.r.domain.search.service.SearchService  : 검색 키워드 저장 중 오류 발생

org.springframework.dao.InvalidDataAccessApiUsageException: Executing an update/delete query
	at org.springframework.orm.jpa.EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(EntityManagerFactoryUtils.java:400) ~[spring-orm-6.2.12.jar:6.2.12]
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.translateExceptionIfPossible(HibernateJpaDialect.java:246) ~[spring-orm-6.2.12.jar:6.2.12]
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.translateExceptionIfPossible(AbstractEntityManagerFactoryBean.java:560) ~[spring-orm-6.2.12.jar:6.2.12]
    ...
Caused by: jakarta.persistence.TransactionRequiredException: Executing an update/delete query
	at org.hibernate.internal.AbstractSharedSessionContract.checkTransactionNeededForUpdateOperation(AbstractSharedSessionContract.java:560) ~[hibernate-core-6.6.33.Final.jar:6.6.33.Final]
	at org.hibernate.query.sqm.internal.QuerySqmImpl.executeUpdate(QuerySqmImpl.java:489) ~[hibernate-core-6.6.33.Final.jar:6.6.33.Final]
	at org.springframework.data.jpa.repository.query.JpaQueryExecution$ModifyingExecution.doExecute(JpaQueryExecution.java:269) ~[spring-data-jpa-3.5.5.jar:3.5.5]
    ...
```

#### 3. 근본 원인: Spring AOP와 Self-Invocation (자가 호출) 문제

`SearchService`의 구조는 다음과 같이 변경되었습니다.

```java
// SearchService.java
@Service
@RequiredArgsConstructor
public class SearchService {
    // ...
    
    // 이 메소드에는 @Transactional 어노테이션이 없음
    public UnifiedSearchResponse unifiedSearch(...) {
        // ...
        // 1. 같은 클래스 내의 다른 메소드 호출
        saveSearchKeyword(keyword); 
        // ...
        // 2. 같은 클래스 내의 다른 메소드 호출
        return performSearch(keyword, searchType, pageable);
    }

    // 쓰기 트랜잭션이 필요함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearchKeyword(String keyword) {
        // ...
        // 'UPDATE' 쿼리를 실행하는 레포지토리 메소드 호출
        searchKeywordRepository.incrementSearchCount(normalizedKeyword); 
        // ...
    }

    // 읽기 전용 트랜잭션
    @Transactional(readOnly = true)
    public UnifiedSearchResponse performSearch(...) {
        // ...
    }
}
```

1.  **문제의 핵심**: `unifiedSearch` 메소드 내에서 `saveSearchKeyword(keyword)`를 호출하는 것은 '내부 호출(self-invocation)'에 해당합니다.
2.  **Spring의 동작 방식**: Spring의 `@Transactional`은 AOP 프록시(Proxy)를 통해 동작합니다. 외부에서 `SearchService`의 메소드를 호출할 때는 이 프록시를 거치므로 트랜잭션이 적용됩니다.
3.  **Self-Invocation의 한계**: 하지만 클래스 내부의 한 메소드가 다른 메소드를 `this.saveSearchKeyword()`와 같은 형태로 호출하면, 프록시를 거치지 않고 직접 대상 객체의 메소드를 호출하게 됩니다.
4.  **결론**: 따라서, `unifiedSearch`가 호출한 `saveSearchKeyword` 메소드에 붙어있는 `@Transactional` 어노테이션은 **무시**됩니다. 결국 `saveSearchKeyword`는 트랜잭션 없이 실행되고, 내부의 `UPDATE` 쿼리는 `TransactionRequiredException`을 발생시킵니다.

> **참고**: 신규 키워드 저장이 성공했던 이유는, `JpaRepository`가 기본으로 제공하는 `save()` 메소드는 그 자체적으로 트랜잭션이 보장되지만, `@Modifying` 어노테이션을 사용하는 사용자 정의 쿼리(e.g., `incrementSearchCount`)는 호출하는 쪽에서 트랜잭션을 보장해주어야 하기 때문입니다.

#### 4. 해결 방안

##### 방안 1: Repository 메소드에 `@Transactional` 추가 (권장)

가장 간단하고 확실한 해결책은, 데이터 변경을 직접 실행하는 Repository의 메소드에 `@Transactional`을 추가하는 것입니다.

**`SearchKeywordRepository.java`**
```java
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    // ...

    @Modifying
    @Transactional // 이 어노테이션을 추가
    @Query("UPDATE SearchKeyword sk SET sk.searchCount = sk.searchCount + 1 WHERE sk.keyword = :keyword")
    int incrementSearchCount(String keyword);
}
```

- **이유**: 이렇게 하면 `incrementSearchCount` 메소드는 호출되는 방식과 상관없이 항상 독립적인 트랜잭션 내에서 실행됩니다. `SearchService`의 구조를 변경할 필요 없이 현재의 오류를 즉시 해결할 수 있습니다.

##### 방안 2: `SearchKeywordService`로 클래스 분리 (장기적으로 더 나은 구조)

'Self-invocation' 문제를 근본적으로 해결하려면, 트랜잭션이 필요한 로직을 별도의 클래스로 분리하고 의존성 주입(DI)을 통해 호출해야 합니다.

1.  **`SearchKeywordService` 생성**: 검색어 저장 로직을 전담하는 새로운 서비스를 만듭니다.
    ```java
    @Service
    @RequiredArgsConstructor
    public class SearchKeywordService {
        private final SearchKeywordRepository searchKeywordRepository;

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveSearchKeyword(String keyword) {
            // ... 기존 saveSearchKeyword 로직 ...
        }
    }
    ```
2.  **`SearchService` 수정**: 새로 만든 서비스를 주입받아 사용합니다.
    ```java
    @Service
    @RequiredArgsConstructor
    public class SearchService {
        private final SearchKeywordService searchKeywordService;
        // ...

        public UnifiedSearchResponse unifiedSearch(...) {
            // ...
            // 외부 빈(Bean)의 메소드 호출 -> 프록시를 통해 호출되므로 트랜잭션 정상 적용
            searchKeywordService.saveSearchKeyword(keyword); 
            // ...
        }
    }
    ```
- **장점**: 이 방식은 각 서비스의 책임(SRP)을 명확히 하고 Spring AOP의 동작 원리를 자연스럽게 따르므로 더 안정적이고 확장성 있는 구조입니다.

#### 5. 최종 제안

- **즉시 해결**: **방안 1**을 적용하여 `SearchKeywordRepository`의 `incrementSearchCount` 메소드에 `@Transactional`을 추가하는 것을 권장합니다.
- **장기적 개선**: 추후 리팩토링 시점에는 **방안 2**를 적용하여 서비스 클래스를 분리하는 것을 고려해볼 수 있습니다.
