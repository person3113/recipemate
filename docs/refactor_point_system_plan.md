# 포인트 시스템 리팩토링 계획 (완료)

## 1. 현행 시스템 분석

### 1.1. 최종 구현 상태

- **포인트 미적립 현상**: **해결 완료**. `PointEventListener`의 모든 핸들러에 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 적용하여 트랜잭션 문제를 해결했습니다.
- **포인트 중복 획득 문제**: **해결 완료**. `PointService`에 `hasEarnedPointsToday` 메서드를 구현하고 `earnPoints` 메서드에 적용하여, 모든 포인트 획득 활동(이벤트 기반, 직접 호출)은 하루에 한 번만 가능하도록 제한되었습니다.

### 1.2. 기능별 상세 구현 현황

- **공동구매 생성/참여, 댓글 작성**:
    - **상태**: **정상 작동 및 하루 1회 제한 적용 완료**.
    - **로직**: `PointEventListener`가 관련 이벤트를 수신하여 `pointService.earnPoints`를 호출합니다. `PointService`는 `PointHistory`를 조회하여 오늘 동일한 활동으로 포인트를 받은 내역이 없으면 포인트를 지급합니다.

- **후기 작성**:
    - **상태**: **정상 작동 및 하루 1회 제한 적용 완료**.
    - **선행 조건**:
        1.  공동구매에 **참여했던 사용자**여야 함.
        2.  해당 공동구매가 **`CLOSED` 상태**여야 함.
        3.  해당 공동구매에 대해 **작성한 후기가 없어야 함** (첫 작성).
    - **로직**: 위 조건을 모두 만족하여 `ReviewService.createReview`가 성공하면 `ReviewCreatedEvent`가 발생합니다. `PointEventListener`가 이를 수신하여 "후기 작성"이라는 설명으로 `pointService.earnPoints`를 호출하고, 중복 지급 방지 로직을 거쳐 30P가 지급됩니다.

- **출석 체크**:
    - **상태**: **구현 완료**.
    - **API**: `POST /users/me/check-in`
    - **로직**: `UserController`가 `pointService.dailyCheckIn` 메서드를 호출합니다. 이 메서드는 `PointHistory`에서 오늘 "출석 체크" 기록이 있는지 조회합니다.
        - 기록이 없으면: 5P를 지급하고 "출석 체크" 내역을 저장합니다.
        - 기록이 있으면: `ALREADY_CHECKED_IN_TODAY` 예외를 발생시켜 중복 지급을 막습니다.

### 1.3. 관련 파일

- `domain/user/controller/UserController.java`: 출석 체크 API 엔드포인트(`POST /users/me/check-in`) 구현.
- `domain/user/service/PointService.java`: 포인트 적립/사용 및 **하루 1회 중복 지급 방지** 비즈니스 로직 구현.
- `domain/review/service/ReviewService.java`: 후기 작성 로직 및 `ReviewCreatedEvent` 발생 지점.
- `domain/review/entity/Review.java`: 후기 생성 시 `GroupBuyStatus.CLOSED` 상태 검증.
- `global/event/listeners/PointEventListener.java`: 이벤트를 수신하여 `PointService`를 호출하는 리스너.
- `domain/user/repository/PointHistoryRepository.java`: `existsByUserAndDescriptionAndCreatedAtBetween` 쿼리 메서드를 통해 중복 확인 로직 지원.

## 2. 리팩토링 목표 (달성 완료)

1.  **포인트 획득 조건 추가**: 각 포인트 획득 활동에 대해 **하루에 한 번만** 포인트를 획득할 수 있도록 제한. **(완료)**
2.  **출석 체크 기능 구현**: 매일 1회 출석 체크 시 5P를 지급하는 기능 추가. **(완료)**

## 3. 리팩토링 계획 (모두 완료)

(이하 내용은 이전과 동일)

## 4. 최종 포인트 정책

| 활동 | 포인트 | 비고 |
| --- | --- | --- |
| 공동구매 생성 | 100P | 하루에 한 번만 |
| 공동구매 참여 | 50P | 하루에 한 번만 |
| 후기 작성 | 30P | 하루에 한 번만 |
| 댓글 작성 | 10P | 하루에 한 번만 |
| 출석 체크 | 5P | 하루에 한 번만 |
