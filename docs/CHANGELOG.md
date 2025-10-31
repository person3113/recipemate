# RecipeMate Changelog

> 완료된 작업 이력 및 변경 사항 기록

---

## [2025-10-31] GroupBuyController Form-Based 리팩터링 완료

### 처리 항목

#### 1. ✅ GroupBuyController 폼 기반 아키텍처로 전환
- **변경 파일**:
  - `GroupBuyController.java`:
    - JSON API 엔드포인트 모두 제거
    - 폼 제출 엔드포인트로 변경 (`POST` 방식 사용)
    - `BindingResult` 추가로 유효성 검증 에러 처리
    - RedirectAttributes로 성공/에러 메시지 전달

#### 2. ✅ DTO 필드명 통일
- **변경 파일**:
  - `GroupBuyResponse.java`: `allowedDeliveryMethods` → `deliveryMethod`
  - `ParticipateRequest.java`: `phoneNumber` 필드 제거 (User 엔티티에서 가져옴)

#### 3. ✅ 뷰 템플릿 수정
- **변경 파일**:
  - `form.html`: 
    - CSRF 토큰 null 체크 추가
    - 날짜 포맷 escape 문자 수정
    - 필드명 통일
  - `detail.html`:
    - `phoneNumber` 입력 필드 제거
    - CSRF 토큰 null 체크 추가
    - 불필요한 Edit/Delete 버튼 로직 제거

#### 4. ✅ 테스트 코드 수정
- **변경 파일**:
  - `GroupBuyControllerTest.java`:
    - 참여자 전용 테스트 유저 추가 (`participantUser`)
    - 호스트가 자신의 공구에 참여하지 않도록 수정
    - 유효성 검증 테스트 수정
    - 중복 메서드 제거

#### 5. ✅ SecurityConfig 업데이트
- **변경 파일**:
  - `SecurityConfig.java`:
    - 구식 JSON API 경로 제거 (`/group-purchases`, `/{id}/page`)
    - Form-based 경로만 유지 (`/list`, `/{id}`)
    - 불필요한 주석 정리

### URL 설계 변경

**변경 전** (JSON API):
```
PUT /group-purchases/{id}
DELETE /group-purchases/{id}
DELETE /group-purchases/{id}/participate
```

**변경 후** (Form-Based):
```
POST /group-purchases/{id}
POST /group-purchases/{id}/delete
POST /group-purchases/{id}/participate/cancel
```

### 효과
- ✅ Pure Form-Based 아키텍처로 통일
- ✅ 모든 테스트 통과 (11/11 tests passing)
- ✅ htmx 통합 준비 완료
- ✅ 빌드 성공 (BUILD SUCCESSFUL)

### 소요 시간
약 1시간

---

## [2025-10-31] MEDIUM 우선순위 리팩터링 완료

### 처리 항목

#### 1. ✅ Participation 예외 처리 표준화 (Item #3)
- **변경 파일**:
  - `ErrorCode.java`: 4개 에러코드 추가
    - `INVALID_QUANTITY`: "수량은 1 이상이어야 합니다."
    - `INVALID_SELECTED_DELIVERY_METHOD`: "선택한 수령 방법은 DIRECT 또는 PARCEL이어야 합니다."
    - `DELIVERY_METHOD_INCOMPATIBLE`: "선택한 수령 방법이 공구의 수령 방법과 호환되지 않습니다."
    - `NO_PARTICIPANTS`: "참여 인원이 0명입니다."
  - `Participation.java`: IllegalArgumentException → CustomException 변경
    - `validateCreateArgs()` 메서드
    - `updateQuantity()` 메서드
    - `updateDeliveryMethod()` 메서드
  - `ParticipationTest.java`: 테스트 코드 예외 타입 변경

#### 2. ✅ GroupBuy 비즈니스 로직 예외 처리 표준화 (Item #4)
- **변경 파일**:
  - `GroupBuy.java:190-202`: IllegalStateException → CustomException
    - `increaseParticipant()`: ErrorCode.MAX_PARTICIPANTS_EXCEEDED 사용
    - `decreaseParticipant()`: ErrorCode.NO_PARTICIPANTS 사용
  - `GroupBuyTest.java`: 테스트 코드 예외 타입 변경

#### 3. ✅ UserService DTO 변환 로직 중복 제거 (Item #5)
- **변경 파일**:
  - `GroupBuyResponse.java`: `from(GroupBuy, List<String>)` 정적 팩토리 메서드 추가
  - `UserService.java:119-154`: `mapToResponse()` 메서드 제거 (26라인 중복 코드 제거)
    - `getMyGroupBuys()`: GroupBuyResponse.from() 사용
    - `getParticipatedGroupBuys()`: GroupBuyResponse.from() 사용

### 효과
- ✅ 일관된 예외 처리 패턴으로 전체 도메인 통일
- ✅ 에러코드 추적 및 HTTP 상태 자동 매핑
- ✅ DTO 변환 로직 일원화로 유지보수성 향상
- ✅ 모든 테스트 통과 (BUILD SUCCESSFUL)

### 소요 시간
약 1시간

---

## [2025-10-31] 예외 처리 일관성 개선 및 데이터 검증 강화

### 처리 항목

#### 1. ✅ GroupBuy.update() targetHeadcount 검증 추가
- **변경 파일**:
  - `GroupBuy.java:163-181`: targetHeadcount < currentHeadcount 검증 추가
  - `ErrorCode.TARGET_HEADCOUNT_BELOW_CURRENT`: "목표 인원은 현재 참여 인원보다 작을 수 없습니다."
- **효과**: 데이터 무결성 보장

#### 2. ✅ ErrorCode에 검증 관련 에러코드 추가
- **변경 파일**:
  - `ErrorCode.java:18-26`: 9개 검증 에러코드 추가
    - `INVALID_TITLE`: "제목은 필수입니다."
    - `INVALID_CONTENT`: "내용은 필수입니다."
    - `INVALID_CATEGORY`: "카테고리는 필수입니다."
    - `INVALID_TOTAL_PRICE`: "총 금액은 0원 이상이어야 합니다."
    - `INVALID_TARGET_HEADCOUNT`: "목표 인원은 2명 이상이어야 합니다."
    - `INVALID_DEADLINE`: "마감일은 현재보다 이후여야 합니다."
    - `INVALID_DELIVERY_METHOD`: "수령 방법은 필수입니다."
    - `INVALID_RECIPE_API_ID`: "레시피 API ID는 필수입니다."
    - `TARGET_HEADCOUNT_BELOW_CURRENT`: "목표 인원은 현재 참여 인원보다 작을 수 없습니다."

#### 3. ✅ GroupBuyService 예외 처리 표준화
- **변경 파일**:
  - `GroupBuyService.java:104, 303-325`: IllegalArgumentException → CustomException
    - `validateRequest()` 메서드 전체 변경
    - `createRecipeBasedGroupBuy()` 메서드 변경

#### 4. ✅ GroupBuy 엔티티 예외 처리 표준화
- **변경 파일**:
  - `GroupBuy.java:1-17`: CustomException, ErrorCode import 추가
  - `GroupBuy.java:151-161`: validateCreateArgs() IllegalArgumentException → CustomException
  - `GroupBuy.java:163-181`: update() 메서드 검증 추가 및 CustomException 사용

#### 5. ✅ 테스트 코드 업데이트
- **변경 파일**:
  - `GroupBuyTest.java`: 예외 타입 변경 (IllegalArgumentException → CustomException)
  - `GroupBuyServiceTest.java`: 예외 타입 및 검증 방식 변경
- **결과**: 모든 테스트 통과 (64 tests)

### 효과
- ✅ 에러코드 추적 가능
- ✅ HTTP 상태 자동 매핑
- ✅ 클라이언트 친화적 구조화된 응답
- ✅ 일관된 예외 처리 패턴

### 소요 시간
약 1.5시간

---

## 작업 이력 요약

| 날짜 | 작업 항목 | 우선순위 | 소요 시간 |
|------|----------|----------|-----------|
| 2025-10-31 | GroupBuyController Form-Based 리팩터링 | 🔴 HIGH | 1시간 |
| 2025-10-31 | Controller 아키텍처 htmx 철학 정렬 리팩터링 | 🔴 HIGH | 2시간 |
| 2025-10-31 | GroupBuy 도메인 검증 로직 및 예외 처리 개선 | 🔴 HIGH | 1.5시간 |
| 2025-10-31 | Participation 예외 처리 표준화 | 🟡 MEDIUM | 30분 |
| 2025-10-31 | GroupBuy 비즈니스 로직 예외 처리 표준화 | 🟡 MEDIUM | 20분 |
| 2025-10-31 | UserService DTO 변환 로직 중복 제거 | 🟡 MEDIUM | 20분 |

**총 소요 시간**: 약 5.5시간

---

## 원칙

- **TDD**: 테스트 먼저 작성 → 코드 구현 → 리팩터링
- **일관성**: 모든 예외는 CustomException + ErrorCode 사용
- **유지보수성**: 중복 코드 제거, 정적 팩토리 메서드 활용
