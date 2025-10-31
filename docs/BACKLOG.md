# RecipeMate Backlog

> 미구현 기능, 기술 부채, 개선 사항 기록 및 우선순위별 관리

---

## ✅ COMPLETED (완료된 작업)

### [2025-10-31] 예외 처리 일관성 개선 및 데이터 검증 강화
**처리 항목**:
1. ✅ **GroupBuy.update() targetHeadcount 검증 추가**
   - `GroupBuy.java:163-181`: targetHeadcount < currentHeadcount 검증 추가
   - `ErrorCode.TARGET_HEADCOUNT_BELOW_CURRENT` 추가
   - 데이터 무결성 보장

2. ✅ **ErrorCode에 검증 관련 에러코드 추가**
   - `ErrorCode.java:18-24`: 9개 검증 에러코드 추가
   - `INVALID_TITLE`, `INVALID_CONTENT`, `INVALID_CATEGORY`
   - `INVALID_TOTAL_PRICE`, `INVALID_TARGET_HEADCOUNT`, `INVALID_DEADLINE`
   - `INVALID_DELIVERY_METHOD`, `INVALID_RECIPE_API_ID`
   - `TARGET_HEADCOUNT_BELOW_CURRENT`

3. ✅ **GroupBuyService 예외 처리 표준화**
   - `GroupBuyService.java:104, 303-325`: IllegalArgumentException → CustomException
   - `validateRequest()` 메서드 전체 변경
   - `createRecipeBasedGroupBuy()` 메서드 변경

4. ✅ **GroupBuy 엔티티 예외 처리 표준화**
   - `GroupBuy.java:1-17`: CustomException, ErrorCode import 추가
   - `GroupBuy.java:151-161`: validateCreateArgs() IllegalArgumentException → CustomException
   - `GroupBuy.java:163-181`: update() 메서드 검증 추가 및 CustomException 사용

5. ✅ **테스트 코드 업데이트**
   - `GroupBuyTest.java`: 예외 타입 변경 (IllegalArgumentException → CustomException)
   - `GroupBuyServiceTest.java`: 예외 타입 및 검증 방식 변경
   - 모든 테스트 통과 확인 (64 tests)

**효과**:
- 에러코드 추적 가능
- HTTP 상태 자동 매핑
- 클라이언트 친화적 구조화된 응답
- 일관된 예외 처리 패턴

**소요 시간**: 약 1.5시간

---

## 🔴 HIGH Priority (즉시 수정)

> 현재 HIGH Priority 항목 없음

---

## 🟡 MEDIUM Priority (Phase 1 완료 전)

### 3. GroupBuy 엔티티 Participation 양방향 연관관계 추가
- **필요성**: `isParticipant(User)` 메서드로 권한 체크
- **해결**: 
  1. `GroupBuy`에 `@OneToMany(mappedBy = "groupBuy") List<Participation> participations` 추가
  2. `isParticipant(User user)` 메서드 구현
- **처리 시점**: Participation 엔티티 구현 시 (Task 1-4-1)
- **예상 시간**: 30분

### 4. Controller 아키텍처 변경 (Thymeleaf 통합)
- **현황**: 모든 Controller가 @RestController (JSON만 반환)
- **문제**: Thymeleaf 템플릿 없어 브라우저 직접 접근 불가, htmx 미사용
- **해결**: 페이지용 @Controller + API용 @RestController 이원화
- **처리 시점**: Phase 1 백엔드 완성 후 전체 화면 통합
- **예상 시간**: 4-6시간

---

## 🟢 LOW Priority (Phase 2 이후)

### 6. Remember-Me 기능 완전 구현
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

---

## 📊 우선순위 요약

| 항목 | 우선순위 | 처리 시점 | 예상 시간 | 상태 |
|------|----------|-----------|-----------|------|
| GroupBuy update() 검증 | 🔴 HIGH | 즉시 | 30분 | ✅ 완료 |
| 예외 처리 일관성 개선 | 🔴 HIGH | 즉시 | 1시간 | ✅ 완료 |
| GroupBuy 양방향 연관관계 | 🟡 MEDIUM | Participation 엔티티 구현 시 | 30분 | ⏳ 대기 |
| Controller Thymeleaf 통합 | 🟡 MEDIUM | Phase 1 백엔드 완성 후 | 4-6시간 | ⏳ 대기 |
| Remember-Me 구현 | 🟢 LOW | 프로덕션 배포 전 | 2-3시간 | ⏳ 대기 |
| Custom Validation | 🟢 LOW | 검증 로직 복잡 시 | 3-4시간 | ⏳ 대기 |
| QueryDSL 활용 | 🟢 LOW | 검색 기능 구현 시 | 2-3시간 | ⏳ 대기 |
| Repository 쿼리 최적화 | 🟢 LOW | Phase 2 검색 최적화 | 2-3시간 | ⏳ 대기 |
| 엔티티 불변성 강화 | 🟢 LOW | Phase 4 리팩터링 | 1-2시간 | ⏳ 대기 |

---

## 🎯 권장 처리 순서

1. ✅ **완료**: GroupBuy 도메인 검증 로직 및 예외 처리 개선 (2025-10-31)
2. **다음**: Participation 엔티티 구현 (Task 1-4-1) → 양방향 연관관계 추가
3. **Phase 1 완료 후**: Controller → Thymeleaf 통합 (모든 도메인 화면 구현)
4. **Phase 2**: QueryDSL 검색 기능, Remember-Me 등 부가 기능
5. **Phase 4**: 필요 시 Validator/PermissionChecker 리팩터링

---

## 📝 원칙

- **TDD**: 백로그 항목도 테스트 먼저 작성
- **YAGNI**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새 느껴질 때 백로그 항목 처리
