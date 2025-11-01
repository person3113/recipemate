# URL 설계 (Thymeleaf + htmx 기반)

> **설계 원칙**  
> - Thymeleaf 서버 사이드 렌더링을 기본으로 사용
> - 폼 제출은 POST 방식 (RESTful PUT/DELETE 대신 POST 사용)
> - 삭제/취소 등 멱등성이 필요한 작업도 POST로 처리 (Thymeleaf form 제약)
> - htmx를 사용한 부분 페이지 갱신은 별도 `/fragments` 엔드포인트 제공
> - 읽기 작업: GET (페이지 렌더링 또는 데이터 조회)
> - 쓰기 작업: POST (생성/수정/삭제 모두)

---

## 1. 인증 (Authentication)

### 1.1. 페이지 렌더링
- `GET /auth/login`: 로그인 페이지
- `GET /auth/signup`: 회원가입 페이지
- `GET /auth/password/find`: 비밀번호 찾기 페이지
- `GET /auth/password/reset`: 비밀번호 재설정 페이지

### 1.2. 폼 처리
- `POST /auth/login`: 로그인 처리
- `POST /auth/signup`: 회원가입 처리
- `POST /auth/logout`: 로그아웃 처리
- `POST /auth/password/find`: 비밀번호 재설정 이메일 발송
- `POST /auth/password/reset`: 비밀번호 재설정 처리

---

## 2. 사용자 (Users)

### 2.1. 페이지 렌더링
- `GET /users/me`: 마이페이지 (프로필 정보)
- `GET /users/me/group-purchases`: 내가 만든 공구 목록 페이지
- `GET /users/me/participations`: 내가 참여한 공구 목록 페이지
- `GET /users/me/bookmarks`: 내가 찜한 공구 목록 페이지
- `GET /users/me/notifications`: 내 알림 목록 페이지
- `GET /users/{userId}/profile`: 특정 사용자 프로필 조회 페이지

### 2.2. 폼 처리
- `POST /users/me`: 프로필 정보 수정
- `POST /users/me/password`: 비밀번호 변경
- `POST /users/me/delete`: 회원 탈퇴

### 2.3. 알림 처리
- `POST /users/me/notifications/{notificationId}/read`: 특정 알림 읽음 처리
- `POST /users/me/notifications/delete-all`: 모든 알림 삭제

### 2.4. htmx Fragments (부분 갱신용)
- `GET /users/me/fragments/notifications`: 알림 목록 HTML 조각 (htmx용)
- `GET /users/me/fragments/profile`: 프로필 정보 HTML 조각
- `GET /users/me/fragments/group-purchases`: 내 공구 목록 HTML 조각

---

## 3. 레시피 (Recipes)

### 3.1. 페이지 렌더링
- `GET /recipes`: 레시피 목록 페이지 (검색, 필터링)
  - Query params: `keyword`, `category`, `page`, `size`
- `GET /recipes/random`: 랜덤 레시피 페이지
- `GET /recipes/categories`: 레시피 카테고리 목록 페이지
- `GET /recipes/{recipeId}`: 레시피 상세 페이지

### 3.2. 레시피 기반 공구 연결
- `GET /recipes/{recipeId}/group-purchases`: 특정 레시피와 연결된 공구 목록 조회

---

## 4. 공동구매 (Group Purchases)

### 4.1. 페이지 렌더링
- `GET /group-purchases/list`: 공구 목록 페이지
  - Query params: `category`, `status`, `recipeOnly`, `keyword`, `page`, `size`
- `GET /group-purchases/new`: 공구 작성 페이지
- `GET /group-purchases/{purchaseId}`: 공구 상세 페이지
- `GET /group-purchases/{purchaseId}/edit`: 공구 수정 페이지
- `GET /group-purchases/{purchaseId}/participants`: 참여자 목록 페이지

### 4.2. 공구 CRUD (폼 처리)
- `POST /group-purchases`: 공구 생성
- `POST /group-purchases/recipe-based`: 레시피 기반 공구 생성
- `POST /group-purchases/{purchaseId}`: 공구 수정
- `POST /group-purchases/{purchaseId}/delete`: 공구 삭제 (소프트 삭제)

### 4.3. 공구 참여 (폼 처리)
- `POST /group-purchases/{purchaseId}/participate`: 공구 참여
- `POST /group-purchases/{purchaseId}/participate/cancel`: 공구 참여 취소

### 4.4. 찜 기능 (htmx용 - 부분 갱신)
- `POST /group-purchases/{purchaseId}/bookmarks`: 공구 찜하기
- `POST /group-purchases/{purchaseId}/bookmarks/cancel`: 공구 찜 취소
  - **또는** DELETE 메서드로 구현 가능 (htmx 사용 시)

### 4.5. 후기 (Reviews)
- `GET /group-purchases/{purchaseId}/reviews`: 공구 후기 목록 페이지
- `POST /group-purchases/{purchaseId}/reviews`: 공구 후기 작성
- `POST /group-purchases/{purchaseId}/reviews/{reviewId}/edit`: 후기 수정
- `POST /group-purchases/{purchaseId}/reviews/{reviewId}/delete`: 후기 삭제

### 4.6. htmx Fragments (부분 갱신용)
- `GET /group-purchases/{purchaseId}/fragments/participants`: 참여자 목록 HTML 조각
- `GET /group-purchases/{purchaseId}/fragments/reviews`: 후기 목록 HTML 조각

---

## 5. 커뮤니티 (Community Posts)

### 5.1. 페이지 렌더링
- `GET /community-posts/list`: 커뮤니티 게시글 목록 페이지
  - Query params: `category`, `keyword`, `page`, `size`
- `GET /community-posts/new`: 게시글 작성 페이지
- `GET /community-posts/{postId}`: 게시글 상세 페이지
- `GET /community-posts/{postId}/edit`: 게시글 수정 페이지

### 5.2. 게시글 CRUD (폼 처리)
- `POST /community-posts`: 게시글 작성
- `POST /community-posts/{postId}`: 게시글 수정
- `POST /community-posts/{postId}/delete`: 게시글 삭제 (소프트 삭제)

### 5.3. 좋아요 (htmx용 - 부분 갱신)
- `POST /community-posts/{postId}/likes`: 게시글 좋아요
- `POST /community-posts/{postId}/likes/cancel`: 게시글 좋아요 취소

---

## 6. 댓글 (Comments)

> **설계 특징**: 통합 댓글 엔드포인트 사용  
> - 공구 댓글과 게시글 댓글을 `/comments` 단일 엔드포인트로 처리
> - `targetType` (GROUP_BUY, POST)과 `targetId`로 대상 구분
> - 확장 가능: 나중에 REVIEW 댓글 추가 시에도 동일 엔드포인트 사용

### 6.1. 댓글 CRUD (폼 처리)
- `POST /comments`: 댓글 작성
  - Form params: `targetType`, `targetId`, `content`, `type`, `parentId` (선택)
  - targetType: GROUP_BUY (공구 댓글) 또는 POST (게시글 댓글)
  - type: Q_AND_A (공구 Q&A) 또는 GENERAL (일반 댓글)
- `POST /comments/{commentId}/edit`: 댓글 수정
  - Form params: `targetType`, `targetId`, `content`
- `POST /comments/{commentId}/delete`: 댓글 삭제 (소프트 삭제)
  - Form params: `targetType`, `targetId`

### 6.2. htmx Fragments (부분 갱신용)
- `GET /comments/fragments?targetType={type}&targetId={id}`: 댓글 목록 HTML 조각
  - 공구 댓글: `?targetType=GROUP_BUY&targetId=1`
  - 게시글 댓글: `?targetType=POST&targetId=1`

---

## 7. 통합 검색 (Global Search)

### 7.1. 검색 페이지
- `GET /search`: 통합 검색 결과 페이지
  - Query params: `query={keyword}`, `type` (optional: recipe, group-purchase, post)
  - 전체 리소스(레시피, 공구, 커뮤니티)에서 검색

### 7.2. htmx Fragments (부분 갱신용)
- `GET /search/fragments?query={keyword}&type={type}`: 검색 결과 HTML 조각

---

## 8. 홈 및 기타

### 8.1. 메인 페이지
- `GET /`: 홈 페이지 (레시피 추천, 인기 공구 등)

### 8.2. 에러 페이지
- `GET /error/404`: 404 에러 페이지
- `GET /error/500`: 500 에러 페이지

---

## 📝 URL 설계 패턴 요약

### 페이지 렌더링 (GET)
- **목록**: `GET /리소스/list`
- **상세**: `GET /리소스/{id}`
- **작성 폼**: `GET /리소스/new`
- **수정 폼**: `GET /리소스/{id}/edit`

### 폼 처리 (POST)
- **생성**: `POST /리소스`
- **수정**: `POST /리소스/{id}` (또는 `/리소스/{id}/edit`)
- **삭제**: `POST /리소스/{id}/delete`

### 서브 리소스 (POST)
- **참여/좋아요**: `POST /리소스/{id}/액션`
- **취소**: `POST /리소스/{id}/액션/cancel`

### htmx Fragments (GET)
- **부분 조각**: `GET /리소스/fragments/이름`
- **동적 조각**: `GET /리소스/{id}/fragments/이름`

---

## 🔄 RESTful API vs Thymeleaf + htmx 차이점

| 작업 | RESTful API | Thymeleaf + htmx |
|------|-------------|------------------|
| 생성 | `POST /resources` | `POST /resources` |
| 조회 | `GET /resources/{id}` | `GET /resources/{id}` |
| 수정 | `PUT /resources/{id}` | `POST /resources/{id}` |
| 삭제 | `DELETE /resources/{id}` | `POST /resources/{id}/delete` |
| 응답 | JSON | HTML 페이지 또는 Fragment |
| 부분 갱신 | 클라이언트 렌더링 | htmx Fragment 엔드포인트 |

---

## 💡 Best Practices

1. **POST-Redirect-GET 패턴**: 폼 제출 후 항상 리다이렉트하여 새로고침 시 중복 제출 방지
2. **Flash Attributes**: 리다이렉트 후 성공/에러 메시지 전달
3. **htmx 부분 갱신**: 전체 페이지 새로고침 없이 특정 영역만 업데이트
4. **명확한 액션 URL**: 삭제는 `/delete`, 취소는 `/cancel` 명시
5. **통합 엔드포인트**: 댓글처럼 여러 리소스에 공통으로 사용되는 기능은 통합 엔드포인트 사용
