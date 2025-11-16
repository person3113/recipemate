# 매너온도 로직 분석 및 개선 제안

## 1. 현황 분석

코드베이스 분석 결과, 매너온도 관련 로직은 다음과 같이 구현되어 있습니다.

### 1.1. 관련 주요 코드

-   **`UserService.java`**: 사용자의 매너온도를 직접 수정하는 `updateMannerTemperature` 메소드 포함.
-   **`ReviewService.java`**: 후기 생성(`createReview`) 시 `ReviewCreatedEvent`를 발행하여 매너온도 변경을 트리거.
-   **`MannerTemperatureEventListener.java`**: `ReviewCreatedEvent`를 수신하여 `UserService`의 매너온도 변경 로직을 호출.
-   **`Review.java`**: 후기 별점에 따라 매너온도 증감치(`delta`)를 계산하는 `calculateMannerTemperatureDelta` 메소드 포함.
-   **`User.java`**: `mannerTemperature` 필드(기본값 36.5)를 정의.

### 1.2. 매너온도 로직 흐름

1.  사용자가 후기를 작성하면 `ReviewService.createReview`가 호출됩니다.
2.  메소드 내부에서 `ReviewCreatedEvent`가 발행됩니다.
3.  `MannerTemperatureEventListener`가 이벤트를 감지하고, 후기를 받은 사용자(공구 주최자)의 매너온도를 변경하기 위해 `UserService.updateMannerTemperature`를 호출합니다.
4.  `Review.calculateMannerTemperatureDelta`를 통해 별점 기반의 증감치가 계산됩니다.
    -   **5점**: +0.5
    -   **4점**: +0.3
    -   **3점**: 0.0
    -   **2점**: -1.0
    -   **1점**: -2.0
5.  `UserService`에서 해당 사용자의 `mannerTemperature` 필드 값을 업데이트합니다.

---

## 2. 문제점

분석 결과, 다음과 같은 세 가지 주요 문제점을 발견했습니다.

### 2.1. 버그: 매너온도 변경이 DB에 미반영

-   **현상**: 사용자가 지적한 대로, 후기 작성 시 매너온도가 변경되지 않습니다.
-   **원인**: `UserService.updateMannerTemperature` 메소드에서 매너온도 값을 변경한 후, `userRepository.save(user)`를 호출하여 변경 사항을 데이터베이스에 저장하는 코드가 누락되었습니다. 이로 인해 변경된 온도가 메모리에서만 갱신되고 영속화되지 않습니다.

### 2.2. 정책 불일치 및 비대칭적 증감 규칙

-   **현상**: 긍정 후기(5점: +0.5)에 비해 부정 후기(1점: -2.0)의 온도 변화 폭이 매우 큽니다.
-   **문제**: 이는 사용자에게 부정적 경험을 과도하게 부각시킬 수 있으며, `docs/project_plan/7_마이페이지.md`에 명시된 "나쁜 후기: -0.5°C"와 일치하지 않습니다.

### 2.3. 기능 누락

1.  **후기 수정 시 미반영**: `ReviewService.updateReview` (후기 수정) 메소드에서는 매너온도 재계산 로직이 전혀 호출되지 않습니다. 사용자가 후기 별점을 수정해도 온도가 바뀌지 않습니다.
2.  **변경 내역 부재**: 매너온도가 왜, 언제, 얼마나 변경되었는지 사용자가 확인할 수 있는 내역(History) 기능이 없습니다. 이는 투명성을 저해합니다.

---

## 3. 개선 제안

위 문제점들을 해결하기 위해 다음과 같은 개선안을 제안합니다.

### 3.1. 버그 수정: DB 저장 로직 추가

-   `UserService.updateMannerTemperature` 메소드에 `userRepository.save(user)` 호출 코드를 추가하여, 변경된 매너온도가 데이터베이스에 정상적으로 저장되도록 수정합니다.

### 3.2. 정책 통일: 증감 규칙 대칭적으로 변경

-   `Review.calculateMannerTemperatureDelta` 메소드의 로직을 다음과 같이 변경하여 증감 규칙을 문서와 일치시키고 대칭적으로 만듭니다.
    -   **5점**: +0.5
    -   **4점**: +0.2
    -   **3점**: 0.0
    -   **2점**: -0.2
    -   **1점**: -0.5

### 3.3. 누락 기능 추가

1.  **후기 수정 시 온도 재계산**:
    -   후기 수정 시에도 `ReviewUpdatedEvent` 같은 이벤트를 발행하도록 `ReviewService.updateReview`를 수정합니다.
    -   `MannerTemperatureEventListener`가 이 이벤트를 처리하여 기존 온도 변화분을 되돌리고, 수정된 별점 기준으로 새로 온도를 계산하여 반영하도록 로직을 추가합니다.

2.  **매너온도 변경 내역 기능 추가**:
    -   **엔티티 신설**: `MannerTempHistory` 엔티티를 새로 만듭니다. (`user`, `changeValue`, `reason`, `relatedReviewId` 등 포함)
    -   **서비스/리포지토리 구현**: `MannerTempHistoryService`와 `MannerTempHistoryRepository`를 구현합니다.
    -   **내역 저장**: 매너온도가 변경될 때마다 `MannerTempHistoryService`를 통해 변경 내역을 DB에 기록하도록 `MannerTemperatureEventListener`를 수정합니다.
    -   **API 엔드포인트 추가**: 마이페이지에서 내역을 조회할 수 있도록 `GET /users/me/manner-histories` API를 추가합니다.
    -   **UI 추가**: 마이페이지에 '온도 내역'을 확인할 수 있는 탭 또는 버튼을 추가합니다.

