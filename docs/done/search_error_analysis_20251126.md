### 통합 검색 기능 오류 분석 및 해결 방안

#### 1. 오류 현상
2025년 11월 26일, 통합 검색창에 키워드를 입력하고 검색을 실행했을 때, 서버에서 500 오류가 발생하며 검색이 실패함.

#### 2. 로그 분석
```
org.springframework.orm.jpa.JpaSystemException: could not execute statement 
[ERROR: cannot execute INSERT in a read-only transaction] 
[insert into search_keywords (created_at,deleted_at,keyword,search_count,updated_at) values (?,?,?,?,?)]
```
*   **핵심 원인**: `cannot execute INSERT in a read-only transaction`
    *   '읽기 전용'으로 설정된 트랜잭션 내에서 `search_keywords` 테이블에 검색어를 저장(`INSERT`)하려다 데이터베이스 오류가 발생했습니다.

*   **오류 전파**:
    1.  `INSERT` 실패 후, 해당 트랜잭션은 '중단(aborted)' 상태로 전환됩니다.
    2.  이후 진행되려던 실제 검색 로직(`SELECT` 쿼리 등)은 이미 중단된 트랜잭션 위에서 실행되려다 `current transaction is aborted` 오류와 함께 연쇄적으로 실패합니다.

#### 3. 근본 원인 추정
`SearchService`의 통합 검색을 처리하는 특정 메소드(예: `unifiedSearch`)에 성능 최적화를 위해 `@Transactional(readOnly = true)` 어노테이션이 적용되어 있을 것입니다.

해당 메소드는 아래 두 가지 작업을 하나의 트랜잭션으로 처리하려고 시도합니다.
1.  **쓰기 작업**: 검색어를 `search_keywords` 테이블에 저장 (`INSERT`)
2.  **읽기 작업**: 저장된 데이터를 바탕으로 실제 검색 수행 (`SELECT`)

이 구조에서는 '읽기 전용' 설정과 '쓰기 작업'이 충돌하여 오류가 발생할 수밖에 없습니다.

#### 4. 해결 방안
**트랜잭션 분리(Transaction Separation)** 를 통해 이 문제를 해결해야 합니다. 즉, '쓰기 작업'은 쓰기 가능한 트랜잭션에서, '읽기 작업'은 읽기 전용 트랜잭션에서 수행하도록 로직을 분리하는 것입니다.

##### 추천 해결 방법: 서비스 내 메소드 분리

`SearchService` 내부의 로직을 다음과 같이 수정합니다.

1.  **`saveSearchKeyword` 메소드 생성**
    *   검색어를 저장하는 로직을 별도의 `public` 메소드로 분리하고, 일반 `@Transactional` 어노테이션을 붙여 '쓰기'가 가능하도록 설정합니다. 이 트랜잭션은 새로운 트랜잭션에서 실행되도록 `propagation = Propagation.REQUIRES_NEW` 옵션을 추가하는 것이 더 안전합니다.

2.  **`performSearch` 메소드로 분리**
    *   기존의 실제 검색 로직(모든 `SELECT` 쿼리)을 별도의 메소드로 분리하고, `@Transactional(readOnly = true)`를 유지하여 읽기 성능 최적화를 유지합니다.

3.  **기존 `unifiedSearch` 메소드 수정**
    *   기존 메소드에서는 트랜잭션 어노테이션을 제거하고, 위에서 생성한 두 메소드를 순서대로 호출하는 역할만 담당하도록 구조를 변경합니다.

##### 예시 코드
```java
// SearchService.java

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    // ... other dependencies

    /**
     * 트랜잭션을 관리하고 두 메소드를 호출하는 진입점 역할
     */
    public Page<UnifiedSearchResponse> unifiedSearch(String keyword, ...) {
        // '쓰기' 트랜잭션을 가진 메소드 호출
        saveSearchKeyword(keyword);

        // '읽기 전용' 트랜잭션을 가진 메소드 호출
        return performSearch(keyword, ...);
    }

    /**
     * 검색어 저장 (쓰기 트랜잭션)
     * REQUIRES_NEW: 항상 새로운 트랜잭션을 시작하여 실행되도록 보장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearchKeyword(String keyword) {
        // 검색어를 저장하거나 카운트를 업데이트하는 로직
        // ...
    }

    /**
     * 실제 검색 수행 (읽기 전용 트랜잭션)
     */
    @Transactional(readOnly = true)
    public Page<UnifiedSearchResponse> performSearch(String keyword, ...) {
        // 레시피, 커뮤니티, 공구 등에서 검색하는 SELECT 로직
        // ...
    }
}
```
*참고: 위 코드는 이해를 돕기 위한 예시이며, 실제 구현 시에는 클래스 구조에 맞게 적용해야 합니다.*

### 로컬(H2)과 운영(PostgreSQL) 환경의 검색 오류 차이 분석

#### 1. 현상 요약
- **운영 환경(PostgreSQL)**: 통합 검색 시 `cannot execute INSERT in a read-only transaction` 오류가 발생하며 실패.
- **로컬 환경(H2)**: 동일한 통합 검색 기능이 오류 없이 정상 동작하며, 검색 결과도 올바르게 렌더링됨.

#### 2. 원인 분석: 왜 로컬에서는 성공하는가?
이 문제의 핵심은 **두 환경에서 사용하는 데이터베이스 시스템의 특성 차이**에 있습니다.

1.  **트랜잭션 관리의 엄격성 차이**
    *   **PostgreSQL (운영)**: 트랜잭션의 속성(특히 `readOnly`)을 매우 엄격하게 준수하고 강제합니다. `readOnly = true`로 선언된 트랜잭션 내에서 데이터 변경(`INSERT`, `UPDATE`, `DELETE`) 시도는 즉시 오류로 처리합니다. 이는 데이터 정합성을 보장하기 위한 표준적인 동작입니다.
    *   **H2 (로컬)**: 개발 및 테스트의 편의성을 위해 설계된 경량 데이터베이스입니다. `PostgreSQL 호환 모드`로 동작하더라도, 실제 PostgreSQL만큼 트랜잭션 규칙을 엄격하게 강제하지 않는 경우가 많습니다. 이번 경우, H2는 `readOnly` 트랜잭션 내의 `INSERT` 구문을 **오류로 처리하지 않고 무시하거나 특별하게 처리**하여 문제가 없는 것처럼 보이게 만든 것으로 강력히 추정됩니다.

2.  **실제 로그의 증거**
    - 운영 환경의 로그에서는 `INSERT into search_keywords`를 실행하려다 오류가 발생한 기록이 명확히 남아있습니다.
    - 반면 로컬 환경의 로그에서는 해당 `INSERT` 쿼리가 아예 관찰되지 않고, `SELECT` 구문만 정상적으로 실행되었습니다.

#### 3. 최종 결론
- **버그는 `SearchService` 코드에 원래 존재했습니다.** '읽기 전용 트랜잭션'에서 '쓰기'를 시도하는 로직은 изначально 잘못된 설계입니다.
- **로컬의 H2 데이터베이스가 이 버그를 숨겨주고 있었을 뿐입니다.** 개발 편의성을 위한 DB의 유연한 동작이 잠재적 문제를 감춘 것입니다.
- **운영의 PostgreSQL은 데이터베이스 표준을 엄격하게 지켰기 때문에 숨어있던 버그를 정상적으로 드러낸 것입니다.**

#### 4. 해결 방안
이전 분석 문서(`search_error_analysis_20251126.md`)에서 제시한 **트랜잭션 분리** 방법이 이 문제의 근본적인 해결책입니다.

`SearchService`에서 '검색어 저장(쓰기)' 로직과 '검색 실행(읽기)' 로직의 트랜잭션을 명확하게 분리하는 리팩토링을 적용해야 합니다. 이 방법은 특정 데이터베이스의 동작에 의존하지 않는, 안정적이고 표준적인 해결책입니다.

이 조치를 통해 로컬과 운영 환경 모두에서 코드가 일관되게 동작하도록 보장할 수 있습니다.

#### 5. 기대 효과
*   트랜잭션의 역할과 책임을 명확히 분리하여 `read-only` 관련 오류를 해결할 수 있습니다.
*   '읽기' 작업은 계속 `readOnly = true` 설정을 통해 성능상 이점을 유지할 수 있습니다.
*   '쓰기' 작업은 독립적인 트랜잭션으로 관리되어 데이터 정합성을 보장할 수 있습니다.

---

### 로컬(H2)과 운영(PostgreSQL) 환경의 검색 오류 차이 분석

#### 1. 현상 요약
- **운영 환경(PostgreSQL)**: 통합 검색 시 `cannot execute INSERT in a read-only transaction` 오류가 발생하며 실패.
- **로컬 환경(H2)**: 동일한 통합 검색 기능이 오류 없이 정상 동작하며, 검색 결과도 올바르게 렌더링됨.

#### 2. 원인 분석: 왜 로컬에서는 성공하는가?
이 문제의 핵심은 **두 환경에서 사용하는 데이터베이스 시스템의 특성 차이**에 있습니다.

1.  **트랜잭션 관리의 엄격성 차이**
    *   **PostgreSQL (운영)**: 트랜잭션의 속성(특히 `readOnly`)을 매우 엄격하게 준수하고 강제합니다. `readOnly = true`로 선언된 트랜잭션 내에서 데이터 변경(`INSERT`, `UPDATE`, `DELETE`) 시도는 즉시 오류로 처리합니다. 이는 데이터 정합성을 보장하기 위한 표준적인 동작입니다.
    *   **H2 (로컬)**: 개발 및 테스트의 편의성을 위해 설계된 경량 데이터베이스입니다. `PostgreSQL 호환 모드`로 동작하더라도, 실제 PostgreSQL만큼 트랜잭션 규칙을 엄격하게 강제하지 않는 경우가 많습니다. 이번 경우, H2는 `readOnly` 트랜잭션 내의 `INSERT` 구문을 **오류로 처리하지 않고 무시하거나 특별하게 처리**하여 문제가 없는 것처럼 보이게 만든 것으로 강력히 추정됩니다.

2.  **실제 로그의 증거**
    - 운영 환경의 로그에서는 `INSERT into search_keywords`를 실행하려다 오류가 발생한 기록이 명확히 남아있습니다.
    - 반면 로컬 환경의 로그에서는 해당 `INSERT` 쿼리가 아예 관찰되지 않고, `SELECT` 구문만 정상적으로 실행되었습니다.

#### 3. 최종 결론
- **버그는 `SearchService` 코드에 원래 존재했습니다.** '읽기 전용 트랜잭션'에서 '쓰기'를 시도하는 로직은  잘못된 설계입니다.
- **로컬의 H2 데이터베이스가 이 버그를 숨겨주고 있었을 뿐입니다.** 개발 편의성을 위한 DB의 유연한 동작이 잠재적 문제를 감춘 것입니다.
- **운영의 PostgreSQL은 데이터베이스 표준을 엄격하게 지켰기 때문에 숨어있던 버그를 정상적으로 드러낸 것입니다.**

#### 4. 해결 방안
이전 분석 문서(`search_error_analysis_20251126.md`)에서 제시한 **트랜잭션 분리** 방법이 이 문제의 근본적인 해결책입니다.

`SearchService`에서 '검색어 저장(쓰기)' 로직과 '검색 실행(읽기)' 로직의 트랜잭션을 명확하게 분리하는 리팩토링을 적용해야 합니다. 이 방법은 특정 데이터베이스의 동작에 의존하지 않는, 안정적이고 표준적인 해결책입니다.

이 조치를 통해 로컬과 운영 환경 모두에서 코드가 일관되게 동작하도록 보장할 수 있습니다.
