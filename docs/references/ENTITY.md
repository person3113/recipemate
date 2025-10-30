# 엔티티 설계 가이드라인

## 공통 원칙
- **Spring Data JPA Auditing** 사용 (`@CreatedDate`, `@LastModifiedDate`)
- **지연 로딩** (`FetchType.LAZY`) 기본 사용
- **소프트 삭제** (`deletedAt` 필드)
- **인덱스** 최적화: 자주 조회되는 필드에 인덱스 설정
- **제약 조건**: 유니크, Check 제약을 통한 데이터 무결성 보장
- **동시성 제어**: 필요 시 낙관적 락(`@Version`) 적용

## 연관관계 설정
- `@ManyToOne`: `FetchType.LAZY` 사용
- `@OneToMany`: `mappedBy`, `cascade`, `orphanRemoval` 적절히 설정
- 양방향 연관관계는 필요한 경우에만 사용
- FK 관계는 명시적으로 정의하여 참조 무결성 보장

## 데이터 무결성
- `@Column(nullable = false)`: 필수 필드는 NOT NULL 제약
- `unique = true`: 고유 제약이 필요한 필드에 설정
- `length`: 문자열 필드는 적절한 길이 제한
- `@UniqueConstraint`: 복합 유니크 키 설정
- `@Min`, `@Max`: 숫자 범위 제약 (Bean Validation)

## 성능 최적화
- 조회가 빈번한 필드에 인덱스 추가
- `@ToString(exclude = {...})`: 순환 참조 방지
- 비정규화 전략: 조회 성능이 중요한 경우 계산 필드 캐싱

---

# RecipeMate 엔티티 상세 설계

기획서와 기능 명세서를 기반으로 RecipeMate 서비스의 핵심 도메인 엔티티를 설계합니다.

## 1. ERD (Entity Relationship Diagram)

```mermaid
    User {
        Long id PK
        String email UK "이메일(로그인 ID)"
        String password "암호화된 비밀번호"
        String nickname UK "닉네임"
        String phoneNumber "전화번호(010-xxxx-xxxx)"
        String profileImageUrl "프로필 이미지 URL"
        Double mannerTemperature "매너온도(기본 36.5)"
        UserRole role "사용자 권한"
    }

    GroupBuy {
        Long id PK
        Long hostId FK "주최자 ID"
        Long version "낙관적 락 버전"
        String title "공구 제목"
        String content "공구 내용"
        String category "카테고리"
        Integer totalPrice "총 금액"
        Integer targetHeadcount "목표 인원"
        Integer currentHeadcount "현재 참여 인원"
        LocalDateTime deadline "마감일"
        DeliveryMethod deliveryMethod "수령 방법"
        String meetupLocation "직거래 장소"
        Integer parcelFee "택배비"
        Boolean isParticipantListPublic "참여자 목록 공개"
        GroupBuyStatus status "공구 상태"
        String recipeApiId "외부 레시피 API ID"
        String recipeName "레시피 이름(캐싱)"
        String recipeImageUrl "레시피 이미지 URL(캐싱)"
    }

    Participation {
        Long id PK
        Long userId FK "참여자 ID"
        Long groupBuyId FK "공구 ID"
        Integer quantity "참여 수량"
        DeliveryMethod selectedDeliveryMethod "선택한 수령 방법"
        LocalDateTime participatedAt "참여 일시"
    }

    Post {
        Long id PK
        Long authorId FK "작성자 ID"
        String title "게시글 제목"
        String content "게시글 내용"
        PostCategory category "게시판 카테고리"
        Integer viewCount "조회수"
    }

    Comment {
        Long id PK
        Long authorId FK "작성자 ID"
        Long groupBuyId FK "대상 공구(Nullable)"
        Long postId FK "대상 게시글(Nullable)"
        Long parentId FK "부모 댓글 ID"
        String content "댓글 내용"
        CommentType type "댓글 타입(Q_AND_A/GENERAL)"
    }

    Review {
        Long id PK
        Long reviewerId FK "작성자 ID"
        Long groupBuyId FK "공구 ID"
        Integer rating "별점(1-5)"
        String content "후기 내용"
    }

    Wishlist {
        Long id PK
        Long userId FK "사용자 ID"
        Long groupBuyId FK "공구 ID"
        LocalDateTime wishedAt "찜한 일시"
    }

    GroupBuyImage {
        Long id PK
        Long groupBuyId FK "공구 ID"
        String imageUrl "이미지 URL"
        Integer displayOrder "표시 순서"
    }

    PostImage {
        Long id PK
        Long postId FK "게시글 ID"
        String imageUrl "이미지 URL"
        Integer displayOrder "표시 순서"
    }

    Notification {
        Long id PK
        Long userId FK "알림 수신자 ID"
        Long actorId FK "알림 발생자 ID"
        String content "알림 내용"
        String url "클릭 시 이동 URL"
        Boolean isRead "읽음 여부"
        NotificationType type "알림 종류"
        Long relatedEntityId "관련 엔티티 ID"
        EntityType relatedEntityType "관련 엔티티 타입"
    }

    User ||--o{ GroupBuy : "주최 (1:N)"
    User }o--o{ Participation : "참여 (N:M)"
    GroupBuy }o--o{ Participation : "참여자 목록 (1:N)"
    User ||--o{ Post : "작성 (1:N)"
    User ||--o{ Comment : "작성 (1:N)"
    User ||--o{ Review : "작성 (1:N)"
    User }o--o{ Wishlist : "찜 (N:M)"
    GroupBuy }o--o{ Wishlist : "찜 목록 (1:N)"
    Post ||--o{ Comment : "댓글 (1:N)"
    GroupBuy ||--o{ Comment : "댓글 (1:N)"
    GroupBuy ||--o{ Review : "후기 (1:N)"
    GroupBuy ||--o{ GroupBuyImage : "이미지 (1:N)"
    Post ||--o{ PostImage : "이미지 (1:N)"
    User ||--o{ Notification : "알림 (1:N)"
```

## 2. 공통 필드 및 Enum 정의

### BaseEntity (모든 엔티티가 상속)
- `createdAt` (LocalDateTime): 생성일시 (`@CreatedDate`)
- `updatedAt` (LocalDateTime): 수정일시 (`@LastModifiedDate`)
- `deletedAt` (LocalDateTime): 소프트 삭제 일시 (Nullable)

### Enums

#### GroupBuyStatus (공구 상태)
- `RECRUITING`: 모집중
- `IMMINENT`: 마감임박 (D-1, D-2)
- `CLOSED`: 마감 (목표 달성 또는 마감일 경과)

#### DeliveryMethod (수령 방법)
- `DIRECT`: 직거래
- `PARCEL`: 택배
- `BOTH`: 직거래+택배 모두 가능

#### PostCategory (게시판 카테고리)
- `TIPS`: 꿀팁 공유
- `FREE`: 자유 게시판
- `REVIEW`: 레시피 후기

#### CommentType (댓글 타입)
- `Q_AND_A`: 질문/답변 댓글
- `GENERAL`: 일반 댓글

#### UserRole (사용자 권한)
- `USER`: 일반 사용자
- `ADMIN`: 관리자

#### NotificationType (알림 종류)
- `JOIN_GROUP_BUY`: 공구 참여 알림
- `CANCEL_PARTICIPATION`: 참여 취소 알림
- `COMMENT_GROUP_BUY`: 공구 댓글 알림
- `COMMENT_POST`: 게시글 댓글 알림
- `REPLY_COMMENT`: 대댓글 알림
- `REVIEW_GROUP_BUY`: 공구 후기 알림
- `GROUP_BUY_DEADLINE`: 공구 마감 알림
- `GROUP_BUY_COMPLETED`: 공구 목표 달성 알림

#### EntityType (엔티티 타입)
- `GROUP_BUY`: 공동구매
- `POST`: 게시글
- `COMMENT`: 댓글
- `REVIEW`: 후기

---

## 3. 엔티티 명세

### 3.1. User (회원)
회원 정보, 프로필, 권한 등을 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 회원 고유 ID |
| `email` | `String(100)` | UK, NOT NULL | 이메일 (로그인 ID) |
| `password` | `String(255)` | NOT NULL | 암호화된 비밀번호 (BCrypt) |
| `nickname` | `String(50)` | UK, NOT NULL | 닉네임 |
| `phoneNumber` | `String(13)` | NOT NULL | 전화번호 (010-xxxx-xxxx) |
| `profileImageUrl` | `String(500)` | Nullable | 프로필 이미지 URL |
| `mannerTemperature` | `Double` | NOT NULL, DEFAULT 36.5 | 매너온도 (기본 36.5) |
| `role` | `UserRole` | NOT NULL, DEFAULT USER | 사용자 권한 Enum |

**인덱스:**
- `idx_email` (email): 로그인 시 조회
- `idx_nickname` (nickname): 검색 시 조회

**제약조건:**
- `email`: 이메일 형식 검증 (@Email)
- `phoneNumber`: 전화번호 형식 검증 (정규식)
- `mannerTemperature`: 0.0 ~ 99.9 범위

**관계:**
- `GroupBuy` (1:N): 주최한 공구 목록
- `Participation` (1:N): 참여한 공구 목록
- `Post` (1:N): 작성한 게시글 목록
- `Comment` (1:N): 작성한 댓글 목록
- `Review` (1:N): 작성한 후기 목록
- `Wishlist` (1:N): 찜한 공구 목록
- `Notification` (1:N): 받은 알림 목록

**비즈니스 규칙:**
- 회원가입 시 매너온도는 36.5°C로 시작
- 좋은 후기(4-5점): 매너온도 상승
- 나쁜 후기(1-2점): 매너온도 하락
- 신고 누적 시: 매너온도 차감

**개발 우선순위:**
- ✅ 1순위: 기본 필드 (email ~ role)
- ⏸️ 4순위 제외: `points` 필드 (향후 확장)

---

### 3.2. GroupBuy (공동구매)
서비스의 핵심 기능인 공동구매 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 공구 고유 ID |
| `host` | `User` | FK, NOT NULL | 주최자 (User 엔티티) |
| `version` | `Long` | NOT NULL, DEFAULT 0 | 낙관적 락 버전 (@Version) |
| `title` | `String(100)` | NOT NULL | 공구 제목 |
| `content` | `String(2000)` | NOT NULL | 공구 상세 내용 |
| `category` | `String(50)` | NOT NULL | 카테고리 (식재료, 간식 등) |
| `totalPrice` | `Integer` | NOT NULL, >= 0 | 총 금액 (원) |
| `targetHeadcount` | `Integer` | NOT NULL, 2-100 | 목표 인원 |
| `currentHeadcount` | `Integer` | NOT NULL, DEFAULT 0 | 현재 참여 인원 |
| `deadline` | `LocalDateTime` | NOT NULL | 마감 일시 |
| `deliveryMethod` | `DeliveryMethod` | NOT NULL | 수령 방법 Enum |
| `meetupLocation` | `String(200)` | Nullable | 직거래 장소 |
| `parcelFee` | `Integer` | Nullable, >= 0 | 1인당 택배비 (원) |
| `isParticipantListPublic` | `Boolean` | NOT NULL, DEFAULT true | 참여자 목록 공개 여부 |
| `status` | `GroupBuyStatus` | NOT NULL, DEFAULT RECRUITING | 공구 상태 Enum |
| `recipeApiId` | `String(100)` | Nullable, IDX | 외부 레시피 API ID |
| `recipeName` | `String(200)` | Nullable | 레시피 이름 (캐싱용) |
| `recipeImageUrl` | `String(500)` | Nullable | 레시피 이미지 URL (캐싱용) |

**인덱스:**
- `idx_status_deadline` (status, deadline): 목록 조회 최적화
- `idx_recipe_api_id` (recipeApiId): 레시피 기반 공구 조회
- `idx_category` (category): 카테고리별 필터링
- `idx_host_id` (host_id): 주최자 공구 목록 조회

**제약조건:**
- `CHECK (current_headcount <= target_headcount)`: 참여 인원 초과 방지
- `CHECK (deadline > created_at)`: 마감일은 생성일 이후
- `CHECK (meetup_location IS NOT NULL OR parcel_fee IS NOT NULL)`: 최소 하나의 수령 방법 필수

**관계:**
- `User` (N:1): 주최자
- `Participation` (1:N, CASCADE, orphanRemoval): 참여자 목록
- `Review` (1:N, CASCADE): 후기 목록
- `Comment` (1:N, CASCADE): 댓글 목록
- `GroupBuyImage` (1:N, CASCADE, orphanRemoval): 이미지 목록
- `Wishlist` (1:N, CASCADE): 찜 목록

**비즈니스 규칙:**
- 레시피 기반 공구: `recipeApiId` NOT NULL
- 일반 공구: `recipeApiId` NULL
- `currentHeadcount`는 Participation 테이블과 동기화 (동시성 제어 필요)
- 참여 시: currentHeadcount++
- 취소 시: currentHeadcount--
- 목표 달성 또는 마감일 경과 시: status → CLOSED

**동시성 제어:**
- `@Version` 필드로 낙관적 락 적용
- 동시 참여 시 충돌 감지 및 재시도

**비정규화 전략:**
- `recipeName`, `recipeImageUrl`: API 호출 최소화, 조회 성능 향상
- 트레이드오프: 데이터 일관성 < 성능

---

### 3.3. Participation (참여 정보)
`User`와 `GroupBuy`의 다대다(N:M) 관계를 연결하는 중간 테이블입니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 참여 고유 ID |
| `user` | `User` | FK, NOT NULL | 참여자 (User 엔티티) |
| `groupBuy` | `GroupBuy` | FK, NOT NULL | 참여한 공구 (GroupBuy 엔티티) |
| `quantity` | `Integer` | NOT NULL, DEFAULT 1, >= 1 | 참여 수량 |
| `selectedDeliveryMethod` | `DeliveryMethod` | NOT NULL | 선택한 수령 방법 Enum |
| `participatedAt` | `LocalDateTime` | NOT NULL, DEFAULT NOW | 참여 일시 |

**인덱스:**
- `idx_user_id` (user_id): 사용자별 참여 공구 조회
- `idx_group_buy_id` (group_buy_id): 공구별 참여자 조회

**제약조건:**
- `UNIQUE (user_id, group_buy_id)`: 중복 참여 방지
- `CHECK (selected_delivery_method IN ('DIRECT', 'PARCEL'))`: BOTH는 선택 불가 (DIRECT 또는 PARCEL만)

**관계:**
- `User` (N:1): 참여자
- `GroupBuy` (N:1): 참여한 공구

**비즈니스 규칙:**
- 한 사용자는 같은 공구에 한 번만 참여 가능
- 공구의 `deliveryMethod`가 BOTH인 경우, 참여자는 DIRECT 또는 PARCEL 중 선택
- 참여 시 GroupBuy.currentHeadcount 증가 (트랜잭션)
- 취소 시 GroupBuy.currentHeadcount 감소 (트랜잭션)
- 마감 1일 전 취소 제한 가능 (비즈니스 로직)

**중요:**
- ⚠️ `selectedDeliveryMethod` 필드 필수 (기획서 요구사항)
- 참여 시 사용자가 선택한 수령 방법 저장

---

### 3.4. Post (커뮤니티 게시글)
커뮤니티의 게시글 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 게시글 고유 ID |
| `author` | `User` | FK, NOT NULL | 작성자 (User 엔티티) |
| `title` | `String(100)` | NOT NULL | 게시글 제목 |
| `content` | `TEXT` | NOT NULL | 게시글 내용 (최대 5,000자) |
| `category` | `PostCategory` | NOT NULL | 게시판 카테고리 Enum |
| `viewCount` | `Integer` | NOT NULL, DEFAULT 0 | 조회수 |

**인덱스:**
- `idx_category_created_at` (category, created_at): 카테고리별 최신순 조회
- `idx_author_id` (author_id): 작성자별 게시글 조회

**관계:**
- `User` (N:1): 작성자
- `Comment` (1:N, CASCADE): 댓글 목록
- `PostImage` (1:N, CASCADE, orphanRemoval): 이미지 목록 (최대 5장)

**비즈니스 규칙:**
- 레시피 후기(REVIEW) 게시글: 관련 공구 정보 포함 가능 (확장)
- 조회수는 중복 방지 로직 적용 (세션/쿠키)

---

### 3.5. Comment (댓글)
`GroupBuy`와 `Post`에 달리는 댓글 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 댓글 고유 ID |
| `author` | `User` | FK, NOT NULL | 작성자 (User 엔티티) |
| `groupBuy` | `GroupBuy` | FK, Nullable | 대상 공구 (Nullable) |
| `post` | `Post` | FK, Nullable | 대상 게시글 (Nullable) |
| `parent` | `Comment` | FK, Nullable | 부모 댓글 (대댓글용) |
| `content` | `String(1000)` | NOT NULL | 댓글 내용 |
| `type` | `CommentType` | NOT NULL, DEFAULT GENERAL | 댓글 타입 Enum |

**인덱스:**
- `idx_group_buy_id` (group_buy_id): 공구별 댓글 조회
- `idx_post_id` (post_id): 게시글별 댓글 조회
- `idx_parent_id` (parent_id): 대댓글 조회

**제약조건:**
- `CHECK ((group_buy_id IS NOT NULL AND post_id IS NULL) OR (group_buy_id IS NULL AND post_id IS NOT NULL))`: 
  - 공구 또는 게시글 중 정확히 하나만 연결

**관계:**
- `User` (N:1): 작성자
- `GroupBuy` (N:1, Nullable): 대상 공구
- `Post` (N:1, Nullable): 대상 게시글
- `Comment` (N:1, Nullable): 부모 댓글 (자기참조)

**비즈니스 규칙:**
- 댓글은 공구 또는 게시글 중 하나에만 연결
- Q&A 댓글: 주최자 답변에 하이라이트
- 대댓글 깊이: 1단계만 허용 (parent가 있으면 더 이상 자식 불가)
- 소프트 삭제 시: 내용을 "삭제된 댓글입니다"로 변경

**검증 로직 (Application Layer):**
```java
@PrePersist
private void validate() {
    if ((groupBuy == null && post == null) || 
        (groupBuy != null && post != null)) {
        throw new IllegalStateException("Comment must have exactly one target");
    }
}
```

---

### 3.6. Review (후기)
완료된 `GroupBuy`에 대한 참여자의 후기 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 후기 고유 ID |
| `reviewer` | `User` | FK, NOT NULL | 작성자 (User 엔티티) |
| `groupBuy` | `GroupBuy` | FK, NOT NULL | 대상 공구 (GroupBuy 엔티티) |
| `rating` | `Integer` | NOT NULL, 1-5 | 별점 (1~5) |
| `content` | `String(1000)` | Nullable | 후기 내용 (선택) |

**인덱스:**
- `idx_group_buy_id` (group_buy_id): 공구별 후기 조회
- `idx_reviewer_id` (reviewer_id): 작성자별 후기 조회

**제약조건:**
- `UNIQUE (reviewer_id, group_buy_id)`: 중복 후기 방지
- `CHECK (rating >= 1 AND rating <= 5)`: 별점 범위 제한

**관계:**
- `User` (N:1): 작성자
- `GroupBuy` (N:1): 대상 공구

**비즈니스 규칙:**
- 참여자만 후기 작성 가능 (Participation 확인 필요)
- 공구 완료(CLOSED) 후에만 작성 가능
- 매너온도 반영:
  - 5점: +0.5°C
  - 4점: +0.3°C
  - 3점: 변동 없음
  - 2점: -1.0°C
  - 1점: -2.0°C
- 후기 작성 시 포인트 +5P (향후 확장)
- 한 공구당 한 번만 작성 가능

---

### 3.7. Wishlist (찜)
`User`가 `GroupBuy`를 찜하는 다대다(N:M) 관계를 연결합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 찜 고유 ID |
| `user` | `User` | FK, NOT NULL | 찜한 사용자 (User 엔티티) |
| `groupBuy` | `GroupBuy` | FK, NOT NULL | 찜한 공구 (GroupBuy 엔티티) |
| `wishedAt` | `LocalDateTime` | NOT NULL, DEFAULT NOW | 찜한 일시 |

**인덱스:**
- `idx_user_id_wished_at` (user_id, wished_at): 사용자별 찜 목록 (최신순)
- `idx_group_buy_id` (group_buy_id): 공구별 찜 개수 집계

**제약조건:**
- `UNIQUE (user_id, group_buy_id)`: 중복 찜 방지

**관계:**
- `User` (N:1): 찜한 사용자
- `GroupBuy` (N:1): 찜한 공구

**비즈니스 규칙:**
- 같은 공구를 여러 번 찜할 수 없음
- 찜한 공구가 마감되면 알림 발송 (선택)

---

### 3.8. GroupBuyImage (공구 이미지)
`GroupBuy`에 첨부되는 이미지 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 이미지 고유 ID |
| `groupBuy` | `GroupBuy` | FK, NOT NULL | 공구 (GroupBuy 엔티티) |
| `imageUrl` | `String(500)` | NOT NULL | 이미지 저장 URL |
| `displayOrder` | `Integer` | NOT NULL, DEFAULT 0 | 표시 순서 (0부터 시작) |

**인덱스:**
- `idx_group_buy_id_order` (group_buy_id, display_order): 공구별 이미지 순서 조회

**제약조건:**
- `UNIQUE (group_buy_id, display_order)`: 같은 공구 내 순서 중복 방지

**관계:**
- `GroupBuy` (N:1): 공구 (orphanRemoval = true)

**비즈니스 규칙:**
- 최대 3장 제한 (Application Layer)
- 공구 삭제 시 이미지 자동 삭제 (orphanRemoval)
- 레시피 기반 공구: 레시피 이미지 자동 첨부 (displayOrder = 0)

**개선 이유:**
- ❌ 기존: targetId + targetType (FK 관계 없음, 참조 무결성 보장 불가)
- ✅ 개선: GroupBuy FK 직접 참조 (참조 무결성 보장, orphanRemoval 가능)

---

### 3.9. PostImage (게시글 이미지)
`Post`에 첨부되는 이미지 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 이미지 고유 ID |
| `post` | `Post` | FK, NOT NULL | 게시글 (Post 엔티티) |
| `imageUrl` | `String(500)` | NOT NULL | 이미지 저장 URL |
| `displayOrder` | `Integer` | NOT NULL, DEFAULT 0 | 표시 순서 (0부터 시작) |

**인덱스:**
- `idx_post_id_order` (post_id, display_order): 게시글별 이미지 순서 조회

**제약조건:**
- `UNIQUE (post_id, display_order)`: 같은 게시글 내 순서 중복 방지

**관계:**
- `Post` (N:1): 게시글 (orphanRemoval = true)

**비즈니스 규칙:**
- 최대 5장 제한 (Application Layer)
- 게시글 삭제 시 이미지 자동 삭제 (orphanRemoval)

---

### 3.10. Notification (알림)
사용자에게 보내는 알림 정보를 관리합니다.

| 필드명 | 데이터 타입 | 제약조건 | 설명 |
| --- | --- | --- | --- |
| `id` | `Long` | PK, Auto Increment | 알림 고유 ID |
| `user` | `User` | FK, NOT NULL | 알림 수신자 (User 엔티티) |
| `actor` | `User` | FK, Nullable | 알림 발생자 (User 엔티티) |
| `content` | `String(500)` | NOT NULL | 알림 내용 |
| `url` | `String(500)` | Nullable | 클릭 시 이동할 URL |
| `isRead` | `Boolean` | NOT NULL, DEFAULT false | 읽음 여부 |
| `type` | `NotificationType` | NOT NULL | 알림 종류 Enum |
| `relatedEntityId` | `Long` | Nullable | 관련 엔티티 ID |
| `relatedEntityType` | `EntityType` | Nullable | 관련 엔티티 타입 Enum |

**인덱스:**
- `idx_user_id_is_read_created_at` (user_id, is_read, created_at): 사용자별 읽지 않은 알림 조회
- `idx_user_id_created_at` (user_id, created_at): 사용자별 알림 목록 (최신순)

**관계:**
- `User` (N:1): 알림 수신자
- `User` (N:1): 알림 발생자 (actor)

**비즈니스 규칙:**
- 알림 종류별 생성 시점:
  - `JOIN_GROUP_BUY`: 누군가 내 공구에 참여
  - `CANCEL_PARTICIPATION`: 누군가 참여 취소
  - `COMMENT_GROUP_BUY`: 내 공구에 댓글
  - `COMMENT_POST`: 내 게시글에 댓글
  - `REPLY_COMMENT`: 내 댓글에 대댓글
  - `REVIEW_GROUP_BUY`: 내 공구에 후기
  - `GROUP_BUY_DEADLINE`: 찜한 공구 마감 D-1
  - `GROUP_BUY_COMPLETED`: 참여한 공구 목표 달성
- 본인 행동은 알림 생성 안 함
- 읽은 알림은 30일 후 자동 삭제 (배치)

**개선 이유:**
- ✅ `actor`: 누가 행동했는지 명확히 표시 ("OOO님이...")
- ✅ `relatedEntityId`, `relatedEntityType`: 관련 엔티티 추적 (디버깅, 확장)

---

## 4. 개발 단계별 엔티티 구현 우선순위

### 🔴 1단계 (Week 1-2): 핵심 엔티티
필수 기능 구현을 위한 최소 엔티티
- ✅ `User`: 회원 가입/로그인
- ✅ `GroupBuy`: 공구 CRUD
- ✅ `Participation`: 참여/취소
- ✅ `GroupBuyImage`: 공구 이미지 (별도 테이블)

### 🟡 2단계 (Week 3-4): 레시피 연동 + 커뮤니티
차별화 기능 구현
- ✅ `Post`: 커뮤니티 게시판
- ✅ `PostImage`: 게시글 이미지
- ✅ `Comment`: 댓글/대댓글
- ✅ `Wishlist`: 찜 기능
- ⚠️ GroupBuy 레시피 필드 활용 (이미 포함)

### 🟢 3단계 (Week 5): 후기 + 알림
신뢰 시스템 구현
- ✅ `Review`: 후기 + 매너온도
- ✅ `Notification`: 알림 시스템

### ⚪ 4단계 (Week 6): 최적화 + 확장
선택적 기능
- 🔧 인덱스 추가 최적화
- 🔧 배치 작업 (만료 알림, 상태 자동 변경)
- 🔧 포인트 시스템 (User.points 필드 추가)
- 🔧 뱃지 시스템 (Badge 엔티티 추가)

---

## 5. 주요 개선 사항 요약

### ✅ 추가된 필드
1. **Participation.selectedDeliveryMethod** ⭐ Critical
   - 기획서 필수: 참여 시 수령 방법 선택
2. **Participation.participatedAt**
   - 참여 일시 추적
3. **GroupBuy.version**
   - 동시성 제어 (낙관적 락)
4. **Comment.type**
   - Q&A vs 일반 댓글 구분
5. **Wishlist.wishedAt**
   - 찜 목록 정렬용
6. **Notification.actor, relatedEntityId, relatedEntityType**
   - 알림 발생자 및 관련 엔티티 추적
7. **GroupBuyImage.displayOrder, PostImage.displayOrder**
   - 이미지 순서 관리

### ✅ 구조 개선
1. **Image 엔티티 분리** ⭐ Critical
   - `Image` → `GroupBuyImage` + `PostImage`
   - FK 관계 명시, 참조 무결성 보장
   - orphanRemoval로 자동 삭제
2. **Enum 추가**
   - `CommentType`: Q&A/GENERAL 구분
   - `EntityType`: 알림 관련 엔티티 타입

### ✅ 제약조건 강화
1. **유니크 제약**
   - Participation (user_id, group_buy_id)
   - Review (reviewer_id, group_buy_id)
   - Wishlist (user_id, group_buy_id)
2. **Check 제약**
   - GroupBuy: currentHeadcount <= targetHeadcount
   - Review: rating BETWEEN 1 AND 5
   - Comment: 정확히 하나의 타겟만 연결
3. **범위 제약**
   - Bean Validation (@Min, @Max) 추가

### ✅ 성능 최적화
1. **인덱스 상세화**
   - 복합 인덱스: (status, deadline), (user_id, is_read, created_at)
   - 조회 패턴 기반 인덱스 설계
2. **비정규화 정당화**
   - recipeName, recipeImageUrl 캐싱 이유 명시
3. **동시성 제어**
   - @Version 필드 추가

### ⏸️ 제거된 필드
1. **User.points** (일시 제외)
   - FEATURE.md 4순위 기능
   - 초기 구현 시 제외, 향후 확장

---

## 6. 참조 무결성 및 CASCADE 전략

### CASCADE 설정 권장사항

| 부모 → 자식 | CASCADE | orphanRemoval | 이유 |
|------------|---------|---------------|------|
| GroupBuy → Participation | ALL | true | 공구 삭제 시 참여 정보도 삭제 |
| GroupBuy → Review | ALL | false | 공구 삭제 시 후기 보존 (논의 필요) |
| GroupBuy → Comment | ALL | false | 공구 삭제 시 댓글 보존 (논의 필요) |
| GroupBuy → GroupBuyImage | ALL | true | 공구 삭제 시 이미지 자동 삭제 |
| GroupBuy → Wishlist | ALL | true | 공구 삭제 시 찜 정보 삭제 |
| Post → Comment | ALL | true | 게시글 삭제 시 댓글도 삭제 |
| Post → PostImage | ALL | true | 게시글 삭제 시 이미지 자동 삭제 |
| Comment → Comment | PERSIST, MERGE | false | 부모 댓글 삭제 시 대댓글 보존/소프트삭제 |

### 소프트 삭제 적용 대상
- ✅ User: 탈퇴 시 deletedAt 설정
- ✅ GroupBuy: 삭제 시 deletedAt 설정 (이력 보존)
- ✅ Post: 삭제 시 deletedAt 설정 (이력 보존)
- ✅ Comment: 삭제 시 "삭제된 댓글입니다" + deletedAt 설정

---

## 7. 쿼리 성능 최적화 가이드

### N+1 문제 해결
- `@ManyToOne`: 기본 LAZY 로딩
- 필요 시 Fetch Join 사용
  ```java
  @Query("SELECT g FROM GroupBuy g JOIN FETCH g.host WHERE g.id = :id")
  ```

### 페이징 최적화
- `Pageable` 사용
- 커버링 인덱스 활용
- Count 쿼리 최적화

### 캐싱 전략
- Redis 활용: 조회수, 인기 공구, 레시피 API 응답
- `@Cacheable` 어노테이션 활용

---

## 8. 마이그레이션 전략

### 초기 개발 (Week 1-2)
- H2 인메모리 DB 사용
- Spring Data JPA 자동 DDL (`spring.jpa.hibernate.ddl-auto=create`)

### 중기 개발 (Week 3-5)
- MySQL 전환
- Flyway/Liquibase 도입 (선택)
- DDL → `validate` 모드 전환

### 배포 준비 (Week 6)
- 운영 DB 스키마 고정
- 인덱스 최종 점검
- 제약조건 테스트

---

## 부록: JPA 어노테이션 예시

### User 엔티티 예시
```java
@Entity
@Table(name = "users", 
    indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_nickname", columnList = "nickname")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    @Email
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password; // BCrypt 암호화
    
    @Column(nullable = false, unique = true, length = 50)
    @Size(min = 2, max = 50)
    private String nickname;
    
    @Column(nullable = false, length = 13)
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
    private String phoneNumber;
    
    @Column(length = 500)
    private String profileImageUrl;
    
    @Column(nullable = false)
    private Double mannerTemperature = 36.5;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;
    
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    private List<GroupBuy> hostedGroupBuys = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Participation> participations = new ArrayList<>();
}
```

### GroupBuy 엔티티 예시
```java
@Entity
@Table(name = "group_buys",
    indexes = {
        @Index(name = "idx_status_deadline", columnList = "status, deadline"),
        @Index(name = "idx_recipe_api_id", columnList = "recipe_api_id"),
        @Index(name = "idx_category", columnList = "category")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_recipe_host", columnNames = {"recipe_api_id", "host_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
public class GroupBuy extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false, foreignKey = @ForeignKey(name = "fk_group_buy_host"))
    private User host;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(nullable = false)
    @Min(0)
    private Integer totalPrice;
    
    @Column(nullable = false)
    @Min(2) @Max(100)
    private Integer targetHeadcount;
    
    @Column(nullable = false)
    @Min(0)
    private Integer currentHeadcount = 0;
    
    @Column(nullable = false)
    @Future
    private LocalDateTime deadline;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryMethod deliveryMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupBuyStatus status = GroupBuyStatus.RECRUITING;
    
    @Column(length = 100)
    private String recipeApiId;
    
    @Column(length = 200)
    private String recipeName;
    
    @Column(length = 500)
    private String recipeImageUrl;
    
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations = new ArrayList<>();
    
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<GroupBuyImage> images = new ArrayList<>();
}
```

---

**문서 버전**: v2.0  
**최종 수정일**: 2025-01-XX  
**작성자**: RecipeMate 개발팀
