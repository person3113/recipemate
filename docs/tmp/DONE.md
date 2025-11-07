
- [x] 로컬에서 실행할 때 미리 어드민 계정되도록
    - 구현 완료: `AdminUserInitializer` 클래스 생성
    - 로컬 환경(dev 프로파일)에서만 실행
    - 어드민 계정: admin@recipemate.com / admin123
- [x] 로그인 에러나 공동구매에서 "마감일은 현재보다 이후여야 합니다" 에러 뜨면서 새로고침되는데 기존에 작성하던 내용은 그대로 유지되게.

### 3.1. 도메인 모델과 비즈니스 로직 (Anemic Domain Model)
- **문제점**: `GroupBuy`, `User`, `Post` 등 핵심 엔티티들이 대부분 필드와 Getter만 가진 '빈약한 도메인 모델(Anemic Domain Model)'입니다. `increaseParticipant()`, `close()` 같은 일부 로직은 엔티티에 존재하지만, 생성(`createGeneral`), 수정(`update`) 등 핵심 비즈니스 로직 대부분이 **서비스 계층(`GroupBuyService`)에 위임**되어 있습니다. 이로 인해 엔티티는 단순 데이터 전달 객체(DTO)처럼 사용되고, 서비스 계층은 점점 비대해져 응집도가 낮아지고 유지보수가 어려워집니다.
- **제안**:
    1. **- [x] 엔티티에 비즈니스 로직 위임**: `GroupBuyService`의 참여/취소 로직을 `GroupBuy` 엔티티로 옮겨 풍부한 도메인 모델을 구축했습니다.
        ```java
        // GroupBuy.java
        public Participation addParticipant(User user, ...) { ... }
        public void cancelParticipation(Participation participation) { ... }
        ```

### 3.2. 서비스 계층의 과도한 책임 (Fat Service)
- **문제점**: `GroupBuyService`와 `ParticipationService`가 너무 많은 책임을 가지고 있습니다. 예를 들어, `GroupBuyService`는 공구 생성 로직 외에도 이미지 업로드, 뱃지 수여, 포인트 적립까지 직접 처리합니다. 이는 단일 책임 원칙(SRP)에 위배됩니다.
- **제안**:
    1. **- [x] 도메인 이벤트 발행**: `Spring ApplicationEventPublisher`를 사용하여 도메인 이벤트를 발행하는 구조로 리팩터링했습니다.
        - `GroupBuyService`, `ReviewService` 등은 자신의 핵심 로직을 처리한 후 `GroupBuyCreatedEvent`, `ReviewCreatedEvent` 같은 이벤트를 발행합니다.
        - `BadgeService`, `PointService`, `NotificationService`는 각각 이 이벤트를 구독(`@EventListener`)하여 자신의 책임에 맞는 로직을 처리하여 서비스 간 **결합도(Coupling)를 크게 낮췄습니다.**
    2. **- [x] 유틸리티 및 헬퍼 분리**: `ImageUploadUtil`처럼, 도메인과 직접 관련 없는 부가 기능은 이미 별도의 유틸리티 클래스로 잘 분리되어 있으며, 이 원칙을 계속 유지합니다.
