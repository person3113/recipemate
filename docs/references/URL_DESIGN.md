# API URL 설계

## 1. 인증 (Authentication)

- `POST /auth/signup`: 회원가입
- `POST /auth/login`: 로그인
- `POST /auth/logout`: 로그아웃
- `POST /auth/password/find`: 비밀번호 찾기 (재설정 요청)
- `PUT /auth/password/reset`: 비밀번호 재설정

## 2. 사용자 (Users)

- `GET /users/me`: 내 프로필 정보 조회
- `PUT /users/me`: 내 프로필 정보 수정
- `DELETE /users/me`: 회원 탈퇴
- `GET /users/{userId}/profile`: 특정 사용자 프로필 조회

### 2.1. 내 활동 내역
- `GET /users/me/group-purchases`: 내가 만든 공동구매 목록 조회
- `GET /users/me/participations`: 내가 참여한 공동구매 목록 조회
- `GET /users/me/bookmarks`: 내가 찜한 공동구매 목록 조회

### 2.2. 알림
- `GET /users/me/notifications`: 내 알림 목록 조회
- `PUT /users/me/notifications/{notificationId}`: 특정 알림 읽음 처리
- `DELETE /users/me/notifications`: 모든 알림 삭제

## 3. 레시피 (Recipes)

- `GET /recipes`: 레시피 목록 조회 (검색, 필터링 포함)
- `GET /recipes/random`: 랜덤 레시피 조회
- `GET /recipes/categories`: 레시피 카테고리 목록 조회
- `GET /recipes/{recipeId}`: 특정 레시피 상세 조회

## 4. 공동구매 (Group Purchases)

- `GET /group-purchases`: 공동구매 목록 조회 (검색, 필터링 포함)
- `POST /group-purchases`: 공동구매 생성
- `GET /group-purchases/{purchaseId}`: 특정 공동구매 상세 조회
- `PUT /group-purchases/{purchaseId}`: 공동구매 수정
- `DELETE /group-purchases/{purchaseId}`: 공동구매 삭제

### 4.1. 참여 (Participation)

- `POST /group-purchases/{purchaseId}/participate`: 공동구매 참여
- `DELETE /group-purchases/{purchaseId}/participate`: 공동구매 참여 취소
- `GET /group-purchases/{purchaseId}/participants`: 공동구매 참여자 목록 조회

### 4.2. 찜 (Bookmarks)

- `POST /group-purchases/{purchaseId}/bookmarks`: 공동구매 찜하기
- `DELETE /group-purchases/{purchaseId}/bookmarks`: 공동구매 찜 취소
- `GET /users/me/bookmarks`: 내가 찜한 공동구매 목록 조회

### 4.3. 후기 (Reviews)

- `GET /group-purchases/{purchaseId}/reviews`: 특정 공동구매의 후기 목록 조회
- `POST /group-purchases/{purchaseId}/reviews`: 특정 공동구매에 후기 작성

## 5. 커뮤니티 (Community)

- `GET /community-posts`: 커뮤니티 게시글 목록 조회
- `POST /community-posts`: 커뮤니티 게시글 작성
- `GET /community-posts/{postId}`: 특정 게시글 상세 조회
- `PUT /community-posts/{postId}`: 게시글 수정
- `DELETE /community-posts/{postId}`: 게시글 삭제

### 5.1. 좋아요 (Likes)

- `POST /community-posts/{postId}/likes`: 게시글 좋아요
- `DELETE /community-posts/{postId}/likes`: 게시글 좋아요 취소

### 5.2. 댓글 (Comments)

- `GET /community-posts/{postId}/comments`: 게시글의 댓글 목록 조회
- `POST /community-posts/{postId}/comments`: 게시글에 댓글 작성
- `PUT /community-posts/{postId}/comments/{commentId}`: 댓글 수정
- `DELETE /community-posts/{postId}/comments/{commentId}`: 댓글 삭제

## 6. 통합 검색 (Global Search)

- `GET /search?query={keyword}`: 전체 리소스(레시피, 공구, 커뮤니티)에서 검색