# 공동구매 기능 개선 제안: 결제, 배송, 수량 처리

## 1. 제안 배경 및 목표

현재 공동구매 기능은 참여 인원만 집계하고 있어, 실제 금액 기반의 목표 달성 관리 및 참여자의 다양한 요구(여러 개 구매, 배송 요청)를 처리하기 어렵습니다. 본 문서는 포인트 기반 결제 시스템, 배송지 관리, 다중 수량 구매 기능을 도입하여 공동구매 기능을 고도화하는 방안을 제안합니다.

## 2. 코드베이스 분석 요약

`codebase_investigator`를 통해 분석한 결과, 다음과 같은 사실을 확인했습니다.

- **User**: `points` 필드와 `usePoints`, `earnPoints` 메서드가 이미 존재하여 포인트 시스템의 기반이 마련되어 있습니다.
- **GroupBuy**: 공동구매의 주체이며, 참여자(`Participation`) 목록을 관리합니다.
- **Participation**: `User`와 `GroupBuy`를 잇는 중간 테이블이며, `quantity`(수량) 필드를 가지고 있습니다.
- **PointHistory**: 포인트 변동 내역을 기록하는 엔티티가 존재합니다.

**부족한 부분:**
- **배송지**: `User`에 배송지 정보를 저장할 `Address` 엔티티가 없습니다.
- **결제 상태**: `Participation`에 결제 완료 여부를 추적할 상태 필드가 없습니다.
- **모금액**: `GroupBuy`에 목표 금액 대비 현재 모인 금액을 추적할 필드가 없습니다.

## 3. 기능 명세

### 3.1. 포인트 기반 결제
- **결제**: 공동구매 참여 시, `(상품 단가 * 수량)`에 `택배비`를 더한 금액만큼 포인트를 즉시 차감합니다.
- **환불**:
    - 사용자가 참여를 취소할 경우, 결제한 포인트를 즉시 환불합니다.
    - 공동구매가 목표 인원/금액 미달 또는 관리자 취소로 무산될 경우, 참여자 전원에게 포인트를 환불합니다.

### 3.2. 배송지 관리
- **배송지 등록/수정/삭제**: 사용자는 '마이페이지'에서 여러 개의 배송지를 관리할 수 있습니다.
- **배송지 선택**: 공동구매 참여 시 '택배' 수령을 선택한 경우, 등록된 배송지 목록에서 하나를 선택합니다.
- **주소 열람**: 공동구매 주최자는 참여자 관리 페이지에서 '택배' 수령을 선택한 참여자들의 배송지 정보를 확인할 수 있습니다.

### 3.3. 공동구매 금액 및 수량 처리
- **용어 변경**: 혼동을 줄이기 위해 기존 `totalPrice`(총 금액)를 `targetAmount`(목표 금액)으로 변경합니다.
- **모금액 추적**: `GroupBuy`에 `currentAmount`(현재 모인 금액) 필드를 추가하여 실시간으로 업데이트합니다.
- **달성률 표시**: UI에 목표 금액, 현재 모인 금액, 그리고 달성률(%)을 시각적으로 표시하여 참여를 유도합니다. (예: 15,000원 / 10,000원 (150%))

### 3.4. 포인트 충전 (간단 버전)
- **기능**: 실제 결제 없이, 사용자가 원하는 금액을 입력하면 해당 금액만큼 포인트가 충전되는 시뮬레이션 기능입니다.
- **위치**: '마이페이지' 내에 '포인트 충전' 페이지를 신설합니다.
- **프로세스**: 사용자가 금액을 입력하고 '충전하기' 버튼을 누르면, `User`의 `points`가 증가하고 `PointHistory`에 `CHARGE` 타입으로 기록이 남습니다.

## 4. 구현 계획

### 4.1. 데이터베이스 모델링

1.  **`Address` 엔티티 신규 생성**
    - `user` (FK, `User`와 N:1)
    - `addressName` (e.g., "집", "회사")
    - `recipientName` (수령인)
    - `street`, `city`, `zipcode`
    - `recipientPhoneNumber` (수령인 연락처): 배송 시 필요한 연락처. `User`의 `phoneNumber`(계정용)와는 별개이며, 주문 시점에 `User`의 정보로 자동 완성 후 수정 가능하게 합니다.
    - `isDefault` (기본 배송지 여부)

2.  **`Participation` 엔티티 수정**
    - `address` (FK, `Address`와 N:1, Nullable)
    - `status` (Enum: `PAYMENT_COMPLETED`, `CANCELLED`)
    - `totalPayment` (결제한 총 포인트)

3.  **`GroupBuy` 엔티티 수정**
    - `totalPrice` 필드명을 `targetAmount`로 변경 (DB 마이그레이션 필요)
    - `currentAmount` 필드 추가 (초기값 0)

### 4.2. 서비스 로직 구현

1.  **`AddressService` 신규 생성**
    - 사용자의 배송지 CRUD 로직을 구현합니다.

2.  **`PointService` 신규 생성 (또는 `UserService`에 통합)**
    - **`chargePoints(userId, amount)`**:
        1. `User`를 조회합니다.
        2. `user.earnPoints(amount)`를 호출하여 포인트를 충전합니다.
        3. `PointHistory`에 `CHARGE` 타입으로 내역을 기록합니다.

3.  **`GroupBuyService` 수정**

    - **`participateInGroupBuy(userId, groupBuyId, quantity, deliveryMethod, addressId)`**:
        1.  `GroupBuy`와 `User` 엔티티를 조회합니다.
        2.  `paymentAmount = groupBuy.getItemPrice() * quantity`를 계산합니다.
        3.  **만약 `deliveryMethod`가 '택배'이면, `paymentAmount += groupBuy.getParcelFee()` 로 택배비를 추가합니다.**
        4.  `user.usePoints(paymentAmount)`를 호출하여 포인트 차감을 시도합니다. (실패 시 `InsufficientPointsException` 발생)
        5.  `PointHistory`에 포인트 사용 내역을 기록합니다.
        6.  `Participation` 객체를 생성하고, 전달받은 `addressId`와 `status=PAYMENT_COMPLETED`, `totalPayment=paymentAmount`를 설정하여 저장합니다.
        7.  `groupBuy.addParticipant()`와 `groupBuy.increaseCurrentAmount(paymentAmount)`를 호출하여 공동구매 상태를 업데이트합니다.

    - **`cancelParticipation(userId, participationId)`**:
        1.  `Participation` 엔티티를 조회하고, 요청한 `userId`가 참여자인지 확인합니다.
        2.  `refundAmount = participation.getTotalPayment()` 금액을 가져옵니다.
        3.  `user.earnPoints(refundAmount)`를 호출하여 포인트를 환불합니다.
        4.  `PointHistory`에 포인트 환불 내역을 기록합니다.
        5.  `groupBuy.decreaseCurrentAmount(refundAmount)`를 호출합니다.
        6.  `Participation` 레코드를 삭제하거나 `status`를 `CANCELLED`로 변경합니다. (논의 필요)

### 4.3. URL 및 API 설계 (Thymeleaf + htmx 기반)

#### 4.3.1. 포인트 충전
- `GET /users/me/points`: 포인트 충전 페이지 렌더링
- `POST /users/me/points/charge`: 포인트 충전 처리
  http://localhost:8080/users/me (마이페이지)에 현재 포인트와 함께 '포인트 충전' 버튼을
  추가합니다. 이 버튼을 누르면 http://localhost:8080/users/me/points/charge 와 같은 별도의
  충전 페이지로 이동하게 됩니다.                                

  해당 페이지에는 금액을 입력하는 폼이 있고, 제출 시 POST /users/me/points/charge로 요청을
  보내 포인트를 충전하고 다시 마이페이지로 리다이렉트하는 흐름이 자연스럽습니다. 이렇게 하면
  기능이 명확하게 분리되고 PRG(Post-Redirect-Get) 패턴을 따를 수 있습니다.

#### 4.3.2. 배송지 관리
- `GET /users/me/addresses`: 내 배송지 목록 페이지 렌더링
- `GET /users/me/addresses/new`: 새 배송지 등록 폼 페이지
- `POST /users/me/addresses`: 새 배송지 생성 처리
- `GET /users/me/addresses/{addressId}/edit`: 배송지 수정 폼 페이지
- `POST /users/me/addresses/{addressId}/edit`: 배송지 수정 처리
- `POST /users/me/addresses/{addressId}/delete`: 배송지 삭제 처리

#### 4.3.3. 공동구매 참여
- GroupBuyController.java 파일을 검토한 결과, 공동구매 참여와 취소를 처리하는 메서드가 이미
    존재합니다.
- 공동구매 참여: POST /{purchaseId}/participate
- 공동구매 참여 취소: POST /{purchaseId}/participate/cancel
- `participate(...)` 메서드 수정:
    - 기존 @RequestParam Integer quantity, @RequestParam DeliveryMethod deliveryMethod에  █
      더해, addressId를 받을 수 있도록 @RequestParam(required = false) Long addressId
      파라미터를 추가합니다.
    - 내부 로직에서 participationService.participate를 호출할 때, 포인트 차감 및 택배비
      계산 로직이 포함된 새로운 서비스 메서드를 호출하도록 변경합니다.
  - `cancelParticipation(...)` 메서드 수정:
      - 컨트롤러 메서드의 시그니처는 변경할 필요가 없습니다.
      - 내부에서 호출하는 participationService.cancelParticipation의 구현을 포인트 환불


## 5. 단계적 구현 제안

1.  **1단계 (핵심 로직 구현)**: DB 모델 변경, 포인트 차감/환불, 금액/수량 처리 로직을 우선적으로 구현합니다.
2.  **2단계 (포인트 및 배송지 기능 구현)**: 포인트 충전 기능과 `Address` 엔티티 및 관련 CRUD 기능을 구현합니다.
3.  **3단계 (UI/UX 개선)**: 프론트엔드에서 변경된 명세에 맞춰 UI를 수정하고, 목표/현재 금액, 달성률 등을 표시합니다.

이 계획을 바탕으로 점진적으로 기능을 개발하고 안정화해나갈 것을 제안합니다.
