# 1:1 쪽지 기능 구현 완료 보고서

## 📅 구현 일자
2025년 11월 15일

## ✅ 구현 완료 항목

### 1. 백엔드 구현 ✅

#### 1-1. Enum 확장
- ✅ `NotificationType.java`: `DIRECT_MESSAGE` 추가
- ✅ `EntityType.java`: `DIRECT_MESSAGE` 추가
- ✅ `ErrorCode.java`: 쪽지 관련 에러 코드 5개 추가
  - `DIRECT_MESSAGE_NOT_FOUND`
  - `CANNOT_SEND_MESSAGE_TO_SELF`
  - `EMPTY_CONTENT`
  - `CONTENT_TOO_LONG`
  - `UNAUTHORIZED_MESSAGE_ACCESS`

#### 1-2. 엔티티 (Entity)
- ✅ `DirectMessage.java` 생성
  - BaseEntity 상속 (createdAt, updatedAt, deletedAt 자동 관리)
  - sender, receiver와 User 엔티티 @ManyToOne 관계 설정
  - 성능 최적화를 위한 3개 인덱스 추가
  - 정적 팩토리 메서드 `create()` 구현 (유효성 검증 포함)
  - 비즈니스 로직 메서드: `markAsRead()`, `canAccess()`

#### 1-3. Repository
- ✅ `DirectMessageRepository.java` 생성
  - `findConversationBetween()`: 두 사용자 간 대화 조회 (JPQL)
  - `countUnreadMessages()`: 안 읽은 메시지 개수 조회
  - `markAllAsReadBetween()`: 일괄 읽음 처리
  - `findRecentContacts()`: 최근 대화 상대 목록 조회

#### 1-4. DTO
- ✅ `DirectMessageResponse.java`: 쪽지 응답 DTO
- ✅ `SendMessageRequest.java`: 쪽지 전송 요청 DTO (Validation 포함)
- ✅ `ContactResponse.java`: 대화 상대 정보 DTO

#### 1-5. Service
- ✅ `DirectMessageService.java` 생성
  - `sendMessage()`: 쪽지 전송 및 알림 생성
  - `getConversation()`: 대화 내용 조회
  - `markAsRead()`: 읽음 처리
  - `getUnreadCount()`: 안 읽은 메시지 개수 조회
  - `getRecentContacts()`: 최근 대화 상대 조회

#### 1-6. Controller
- ✅ `DirectMessageController.java` 생성
  - `GET /direct-messages/conversation/{userId}`: 대화 페이지
  - `POST /direct-messages/send/{recipientId}`: 쪽지 전송
  - `GET /direct-messages/contacts`: 대화 상대 목록 페이지
  - `GET /direct-messages/unread-count`: 안 읽은 메시지 개수 (AJAX)

#### 1-7. NotificationService 수정
- ✅ `generateNotificationContent()`: DIRECT_MESSAGE 케이스 추가
- ✅ `generateNotificationUrl()`: DIRECT_MESSAGE 케이스 추가
  - 알림 클릭 시 발신자와의 대화방으로 이동

---

### 2. 프론트엔드 구현 ✅

#### 2-1. 대화 페이지
- ✅ `templates/direct-messages/conversation.html` 생성
  - 카카오톡 스타일 메시지 UI
  - 발신/수신 메시지 구분 (색상, 정렬)
  - 자동 스크롤 (맨 아래로)
  - 메시지 전송 폼
  - 성공/에러 메시지 표시

#### 2-2. 대화 상대 목록 페이지
- ✅ `templates/direct-messages/contacts.html` 생성
  - 최근 대화 상대 목록
  - 프로필 이미지 표시
  - 매너온도 배지
  - 안 읽은 메시지 개수 표시

#### 2-3. 참여자 관리 페이지 수정
- ✅ `templates/group-purchases/participants.html` 수정
  - 1:1 쪽지 버튼 활성화
  - 비활성화된 버튼 → 활성 링크로 변경
  - 안내 메시지 수정

---

## 📁 파일 구조

```
src/main/java/com/recipemate/domain/directmessage/
├── controller/
│   └── DirectMessageController.java
├── dto/
│   ├── ContactResponse.java
│   ├── DirectMessageResponse.java
│   └── SendMessageRequest.java
├── entity/
│   └── DirectMessage.java
├── repository/
│   └── DirectMessageRepository.java
└── service/
    └── DirectMessageService.java

src/main/resources/templates/direct-messages/
├── contacts.html
└── conversation.html
```

---

## 🔑 주요 기능

### 1. 쪽지 전송
- 참여자 관리 페이지에서 각 참여자에게 쪽지 전송 가능
- 최대 1000자까지 입력 가능
- 자신에게는 쪽지 전송 불가
- 빈 메시지 전송 차단

### 2. 대화 조회
- 두 사용자 간 모든 메시지를 시간순으로 표시
- 발신/수신 메시지 구분 (카카오톡 스타일)
- 메시지 읽음 처리 자동화

### 3. 알림 연동
- 쪽지 수신 시 자동 알림 생성
- 알림 클릭 시 발신자와의 대화방으로 이동
- 알림 내용: "{발신자}님이 쪽지를 보냈습니다."

### 4. 안 읽은 메시지
- 안 읽은 메시지 개수 조회 API
- 대화방 진입 시 자동 읽음 처리
- 쪽지함에 안 읽은 메시지 배지 표시

### 5. 최근 대화 상대
- 쪽지를 주고받은 상대 목록
- 프로필 이미지 및 매너온도 표시
- 클릭 시 해당 상대와의 대화방으로 이동

---

## 🎨 UI/UX 특징

### 카카오톡 스타일 메시지 UI
- **발신 메시지**: 파란색, 오른쪽 정렬
- **수신 메시지**: 흰색 (테두리), 왼쪽 정렬
- **시간 표시**: MM-dd HH:mm 형식
- **자동 스크롤**: 페이지 로드 시 최신 메시지로 스크롤

### 반응형 디자인
- Bootstrap 5 기반
- 모바일/태블릿/데스크톱 대응
- 최대 너비 제한으로 가독성 확보

---

## 🔒 보안 기능

### 1. 권한 검증
- 로그인한 사용자만 쪽지 기능 사용 가능
- 비로그인 시 로그인 페이지로 리다이렉트

### 2. 입력 검증
- **서버 사이드**: `@Valid`, `@NotBlank`, `@Size` 어노테이션
- **클라이언트 사이드**: `required`, `maxlength` 속성
- **비즈니스 로직**: 자신에게 전송 방지, 빈 메시지 차단

### 3. 데이터 접근 제어
- 본인이 참여한 대화만 조회 가능
- 타인의 쪽지 접근 시도 시 에러 처리 (향후 강화 가능)

---

## 📊 데이터베이스

### 테이블: `direct_messages`
```sql
CREATE TABLE direct_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

### 인덱스 (성능 최적화)
1. `idx_dm_sender_created`: (sender_id, created_at)
2. `idx_dm_receiver_created`: (receiver_id, created_at)
3. `idx_dm_receiver_is_read`: (receiver_id, is_read, created_at)

---

## 🧪 테스트 시나리오

### 시나리오 1: 쪽지 전송
1. 공구 주최자가 참여자 관리 페이지 접속
2. 참여자 목록에서 "쪽지" 버튼 클릭
3. 대화 페이지로 이동
4. 메시지 입력 후 전송
5. 수신자에게 알림 생성 확인

### 시나리오 2: 대화 조회
1. 쪽지함 (/direct-messages/contacts) 접속
2. 대화 상대 목록 확인
3. 특정 상대 클릭
4. 해당 상대와의 모든 메시지 확인
5. 안 읽은 메시지 자동 읽음 처리 확인

### 시나리오 3: 알림 연동
1. 다른 사용자가 쪽지 전송
2. 알림탭에서 알림 확인
3. 알림 클릭
4. 발신자와의 대화방으로 자동 이동

---

## 🚀 향후 개선 사항

### 1. 실시간 기능 (선택)
- WebSocket 또는 Server-Sent Events 도입
- 새 메시지 실시간 수신
- 읽음 상태 실시간 업데이트

### 2. 추가 기능
- 메시지 검색 기능
- 메시지 삭제 기능
- 차단 기능
- 이미지/파일 전송

### 3. UI 개선
- 메시지 페이지네이션 (무한 스크롤)
- 답장 버튼 추가
- 이모지 지원

### 4. 성능 최적화
- 메시지 캐싱
- 대화 목록 페이지네이션
- 읽지 않은 메시지 수 캐싱

---

## ✨ 결론

1:1 쪽지 기능이 **완전히 구현되었습니다**.

### 구현된 기능:
- ✅ 쪽지 전송 및 수신
- ✅ 대화 내용 조회 (카카오톡 스타일)
- ✅ 알림 연동
- ✅ 안 읽은 메시지 관리
- ✅ 최근 대화 상대 목록
- ✅ 참여자 관리 페이지 통합

### 기술적 특징:
- 프로젝트 아키텍처와 완벽하게 일치
- 기존 코드 스타일 준수
- 성능 최적화 (인덱스, JPQL)
- 입력 검증 및 보안 고려
- 반응형 UI/UX

**빌드 상태**: ✅ BUILD SUCCESSFUL

쪽지 기능을 통해 공구 참여자 간 원활한 소통이 가능해졌습니다! 🎉

