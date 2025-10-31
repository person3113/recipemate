# RecipeMate Backlog

> 미구현 기능, 기술 부채, 개선 사항 기록 및 우선순위별 관리

📝 원칙
- **TDD**: 백로그 항목도 테스트 먼저 작성
- **YAGNI**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새 느껴질 때 백로그 항목 처리

---

## 🔴 HIGH Priority (즉시 수정)

---

## 🟡 MEDIUM Priority 

### 1. 테스트 코드 리팩터링 - 슬라이스 테스트 및 단위 테스트 전환
- **현황**: Service와 Controller 테스트가 `@SpringBootTest`로 전체 Context 로딩 (무거움)
- **문제점**:
  - Service 테스트: 전체 ApplicationContext 로딩, 실제 DB 연동 → 느림
  - Controller 테스트: `@SpringBootTest` + `@AutoConfigureMockMvc` → 통합 테스트 수준
  - `@MockBean` deprecated (Spring Boot 3.4.0+) 경고 발생
- **개선 방안**:
  
  **✅ Entity 테스트 (현재 방식 유지)**
  - Plain JUnit, 프레임워크 의존성 없음
  - 순수 도메인 로직 검증 (속도: ⚡ 매우 빠름)
  
  **✅ Repository 테스트 (현재 방식 유지)**
  - `@DataJpaTest` 슬라이스 테스트 (JPA 관련 빈만 로딩)
  - 인메모리 DB (H2) 사용, QueryDSL 설정 @Import
  - 쿼리 메서드, 커스텀 쿼리 검증 (속도: 🚀 빠름)
  
  **⚠️ Service 테스트 (전환 필요)**
  - 현재: `@SpringBootTest` (무거움)
  - 개선: `@ExtendWith(MockitoExtension.class)` + `@Mock`
  - Repository와 의존성을 Mock으로 주입, 순수 비즈니스 로직만 테스트
  - Context 로딩 없이 격리된 단위 테스트 (속도: ⚡ 매우 빠름)
  
  **⚠️ Controller 테스트 (전환 필요)**
  - 현재: `@SpringBootTest` + `@AutoConfigureMockMvc` (무거움)
  - 개선: `@WebMvcTest(ControllerName.class)`
  - MVC 레이어만 로딩, Service는 `@MockitoBean`으로 주입
  - MockMvc를 통한 HTTP 요청/응답 검증 (속도: 🚀 빠름)
  
  **📌 @MockBean Deprecation 대응 (Spring Boot 3.4.0+)**
  - Deprecated: `@MockBean`, `@SpyBean`
  - 대체: `@MockitoBean`, `@MockitoSpyBean`
  - import 변경: `org.springframework.boot.test.mock.mockito.MockBean`
    → `org.springframework.test.context.bean.override.mockito.MockitoBean`
  
  **Integration 테스트 (최소화)**
  - `@SpringBootTest`는 전체 플로우 E2E 테스트에만 사용
  - 핵심 시나리오 몇 개만 작성 (속도: 🐌 느림)
  
- **장점**:
  - 테스트 실행 속도 5~10배 향상 (Context 로딩 시간 제거)
  - 레이어별 책임 명확화 (단위 테스트 vs 통합 테스트)
  - Mock을 통한 의존성 제어로 테스트 격리성 향상
  - TDD 사이클 단축 (빠른 피드백)
- **처리 시점**: Phase 1 완료 전 (테스트 속도 개선 필수)
- **예상 시간**: 4-6시간
- **참고**: 기존 테스트 로직 유지하면서 어노테이션 및 Mock 설정만 변경

---

## 🟢 LOW Priority 

### 1. htmx HTML Fragment 엔드포인트 구현
- **현황**: 현재 JSON API와 페이지 렌더링 엔드포인트만 존재
- **개선**: 
  1. 컨트롤러에 htmx HTML Fragment 엔드포인트 추가 (TODO 주석으로 표시된 부분 구현)
  2. Thymeleaf Fragment 템플릿 생성 (list-items, detail-content, form-fields 등)
  3. 기존 템플릿의 htmx 호출 경로를 HTML Fragment 엔드포인트로 변경
- **장점**: 
  - htmx 철학 완전 준수 (서버가 HTML 반환)
  - JSON 파싱 오버헤드 제거
  - 서버 사이드 렌더링 완전 활용
- **처리 시점**: Phase 2 UI 개선 단계 또는 프론트엔드 최적화 시
- **예상 시간**: 3-4시간
- **참고**: 기존 기능에 영향 없이 점진적 추가 가능

### 7. ParticipationService 낙관적 락 재시도 로직 개선
- **현황**: `@Retryable` 어노테이션 사용 (maxAttempts=3, backoff=100ms)
- **문제**: 재시도 실패 시 사용자 친화적 메시지 부족, 로깅 없음
- **개선**: 
  1. 재시도 실패 시 명확한 에러 메시지 (`ErrorCode.CONCURRENCY_FAILURE`)
  2. 재시도 로그 추가 (`@Recover` 메서드 활용)
  3. Exponential backoff 적용 고려
- **처리 시점**: Phase 3-4 안정화 단계
- **예상 시간**: 1-2시간

### 8. Repository 페치 조인 최적화
- **현황**: 
  - `ParticipationRepository.findByGroupBuyIdWithUser()`: User 페치 조인
  - `ParticipationRepository.findByUserIdWithGroupBuyAndHost()`: GroupBuy, Host 페치 조인
  - `GroupBuyRepository.findByIdWithHost()`: Host 페치 조인
- **장점**: N+1 문제 방지, 쿼리 최적화
- **주의**: 페이징과 @OneToMany 페치 조인 혼용 시 메모리 문제 가능 (현재는 @ManyToOne만 페치 조인하여 안전)
- **개선**: Hibernate default_batch_fetch_size 설정 추가 고려
- **처리 시점**: Phase 3-4 성능 테스트 후
- **예상 시간**: 1-2시간

### 9. GroupBuySearchCondition 빌더 패턴 적용
- **현황**: DTO 필드가 많아 생성자 방식 복잡해질 가능성
- **개선**: 빌더 패턴 적용으로 가독성 향상
- **처리 시점**: Phase 2 검색 기능 확장 시
- **예상 시간**: 30분

### 10. Remember-Me 기능 완전 구현
- 현재 기본 세션 인증만 동작
- 필요 작업: PersistentTokenRepository 설정, DB 테이블 생성, LoginRequest 수정
- 장점: 7일간 로그인 상태 유지
- 단점: 보안 설정 복잡, 토큰 관리 필요
- 처리 시점: 프로덕션 배포 전 또는 사용자 요청 시
- 예상 시간: 2-3시간

### 7. DTO Validation 강화
- 현재 기본 어노테이션만 사용 (@NotBlank, @Email, @Pattern)
- 개선: 커스텀 Validation 어노테이션 (예: @UniqueEmail)
- 장점: DTO에 검증 로직 응집
- 단점: 구현 복잡도 증가
- 처리 시점: 검증 로직 복잡해지고 재사용 많을 때
- 예상 시간: 3-4시간

### 8. QueryDSL 활용 (JPA Specification 대체)
- 현재 Task 1-3-4에서 JPA Specification으로 동적 쿼리 구현
- 문제점: 타입 안정성 부족, 복잡한 쿼리 작성 어려움, 컴파일 타임 검증 불가
- 해결: QueryDSL로 리팩터링하여 타입 안전한 쿼리 작성
- 개선 사항:
  - Q타입 클래스 생성으로 컴파일 타임 검증
  - 메서드 체이닝으로 가독성 향상
  - 복잡한 조인 및 서브쿼리 작성 용이
  - IDE 자동완성 지원
- 처리 시점: Phase 2 검색 기능 최적화 또는 복잡한 쿼리 추가 시
- 예상 시간: 2-3시간

### 9. GroupBuy Repository 쿼리 메서드 최적화
- 현재 LIKE 쿼리로 Full scan, IN 절 비효율적
- 개선: Full-Text Search 도입 또는 QueryDSL 동적 쿼리
- 처리 시점: Phase 2 검색 기능 최적화
- 예상 시간: 2-3시간

### 10. GroupBuy 엔티티 불변성 강화
- 현재 일부 필드 Setter 사용, 명시적 캡슐화 부족
- 개선: @Setter 제거, 모든 상태 변경 메서드로만 수행, 불변 필드 final 선언
- 장점: 객체 불변성 보장, 의도하지 않은 상태 변경 방지
- 처리 시점: Phase 4 리팩터링 단계
- 예상 시간: 1-2시간


