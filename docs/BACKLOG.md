# RecipeMate Backlog

> 미구현 기능, 기술 부채, 개선 사항 기록 및 우선순위별 관리

---

## 🔴 HIGH Priority

### 1. Controller 아키텍처 변경 (Thymeleaf 통합)
- 현재 모든 Controller가 @RestController로 JSON만 반환
- Thymeleaf 템플릿 없어 브라우저 직접 접근 불가
- htmx 미사용 상태
- 해결: 페이지용 @Controller + API용 @RestController 이원화
- 작업: User 도메인 Controller 수정, Thymeleaf 템플릿 작성, htmx 통합, 테스트 수정
- 처리 시점: GroupBuy 백엔드 완성 후 전체 화면 통합 (권장)
- 예상 시간: 4-6시간

---

## 🟡 MEDIUM Priority

### 2. Validator 패턴 도입
- 현재 검증 로직이 Service 클래스에 직접 구현
- 해결: 별도 Validator 컴포넌트 분리로 Service 간결화, 재사용성 향상
- 처리 시점: GroupBuy 개발 중 검증 로직 복잡해질 때
- 예상 시간: 2-3시간

### 3. PermissionChecker 패턴 도입
- GroupBuy 도메인에서 권한 체크 필요 (주최자/참가자 확인, 수정/삭제/리뷰 권한)
- 처리 시점: GroupBuy Service 구현 중 권한 체크 반복 시
- 예상 시간: 1-2시간

### 4. GroupBuy 엔티티 양방향 연관관계 추가
- 현재 GroupBuy → Participation 단방향만 존재
- isParticipant(User) 메서드 미구현으로 권한 체크 불가
- 해결: @OneToMany(mappedBy) 추가
- 처리 시점: Participation 엔티티 구현 시
- 예상 시간: 30분

### 5. GroupBuy update() 메서드 targetHeadcount 변경 제한
- 현재 목표 인원 변경 시 검증 없음
- 현재 참여자보다 적은 목표 인원으로 변경 가능한 문제
- 해결: targetHeadcount < currentHeadcount 검증 추가
- 처리 시점: GroupBuy Service 구현 중
- 예상 시간: 30분

---

## 🟢 LOW Priority

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

### 8. QueryDSL 활용
- 현재 의존성 추가되었으나 미사용
- 활용 시점: 복잡한 동적 쿼리 필요 시 (공동구매 검색 - 카테고리, 상태, 키워드)
- 처리 시점: 검색 기능 구현 시 (Phase 2)
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

### 11. 예외 처리 일관성 개선 (Validation Exception)
- 현재 Service 검증 로직에서 IllegalArgumentException 사용 (GroupBuyService:126-146)
- CustomException + ErrorCode 인프라 존재하나 일부 미활용
- 해결: ErrorCode에 검증 관련 에러코드 추가 (INVALID_TITLE, INVALID_CONTENT 등), Service에서 CustomException 사용
- 장점: 에러코드 추적 가능, HTTP 상태 자동 매핑, 클라이언트 친화적 구조화된 응답, i18n 확장 용이
- 처리 시점: GroupBuy 도메인 완성 후 리팩터링
- 예상 시간: 1-2시간

---

## 📊 우선순위 요약

| 항목 | 우선순위 | 처리 시점 | 예상 시간 |
|------|----------|-----------|-----------|
| Controller Thymeleaf 통합 | 🔴 HIGH | GroupBuy 백엔드 완성 후 | 4-6시간 |
| Validator 패턴 도입 | 🟡 MEDIUM | GroupBuy 개발 중 복잡도 증가 시 | 2-3시간 |
| PermissionChecker 도입 | 🟡 MEDIUM | GroupBuy 권한 체크 반복 시 | 1-2시간 |
| GroupBuy 양방향 연관관계 | 🟡 MEDIUM | Participation 엔티티 구현 시 | 30분 |
| GroupBuy update() 검증 | 🟡 MEDIUM | GroupBuy Service 구현 중 | 30분 |
| Remember-Me 구현 | 🟢 LOW | 프로덕션 배포 전 | 2-3시간 |
| Custom Validation | 🟢 LOW | 검증 로직 복잡 시 | 3-4시간 |
| QueryDSL 활용 | 🟢 LOW | 검색 기능 구현 시 | 2-3시간 |
| Repository 쿼리 최적화 | 🟢 LOW | Phase 2 검색 최적화 | 2-3시간 |
| 엔티티 불변성 강화 | 🟢 LOW | Phase 4 리팩터링 | 1-2시간 |
| 예외 처리 일관성 개선 | 🟢 LOW | GroupBuy 도메인 완성 후 | 1-2시간 |

---

## 🎯 권장 처리 순서

1. **현재**: GroupBuy 도메인 백엔드 개발 (Entity, Service, RestController)
2. **Phase 1 완료 후**: Controller → Thymeleaf 통합 (모든 도메인 화면 구현)
3. **Phase 2**: 필요 시 Validator/PermissionChecker 리팩터링
4. **Phase 3**: QueryDSL 검색 기능, Remember-Me 등 부가 기능

---

## 📝 원칙

- **TDD**: 백로그 항목도 테스트 먼저 작성
- **YAGNI**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새 느껴질 때 백로그 항목 처리
