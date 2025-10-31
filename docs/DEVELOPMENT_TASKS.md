# RecipeMate 개발 작업 리스트

> **작성일**: 2025-01-XX  
> **기반 문서**: PROJECT_PLAN.md, ENTITY.md, FEATURE.md, URL_DESIGN.md  
> **개발 방식**: TDD + DDD + 애자일 (단계적 개발)

---

## 📋 작업 분할 원칙

1. **기능 단위 분할**: 각 도메인별로 테스트 → 엔티티 → 서비스 → 컨트롤러 순서
2. **우선순위**: 1순위(핵심) → 2순위(차별화) → 3순위(부가) → 4순위(확장)
3. **TDD 적용**: 각 기능마다 테스트 먼저 작성 → 구현 → 리팩터링
4. **단계적 완성**: 간단한 버전 먼저 동작 → 점진적 개선

---

## 🔴 Phase 1: 프로젝트 초기 설정 및 핵심 기능 (Week 1-2)

### 1.1. 프로젝트 초기 설정

#### [x] Task 1-1-1: Spring Boot 프로젝트 생성 및 기본 설정
- [x] Spring Initializr로 프로젝트 생성
  - Dependencies: Spring Web, Spring Data JPA, Spring Security, H2, PostgreSQL, Lombok, Validation
- [x] `application.yml` 설정 (프로파일별: dev, prod)
  - H2 인메모리 DB 설정 (dev)
  - PostgreSQL 설정 (prod)
  - JPA 설정 (ddl-auto: create → validate)
- [x] 패키지 구조 생성
  ```
  com.recipemate
  ├── domain (도메인별 패키지)
  │   ├── user
  │   ├── groupbuy
  │   ├── recipe
  │   ├── community
  │   └── notification
  ├── global (공통 설정, 유틸)
  │   ├── config
  │   ├── exception
  │   ├── util
  │   └── common
  └── RecipeMateApplication.java
  ```

#### [x] Task 1-1-2: 공통 설정 및 Base 클래스 작성
- [x] `BaseEntity` 작성 (createdAt, updatedAt, deletedAt)
  - `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`
- [x] JPA Auditing 설정 (`@EnableJpaAuditing`)
- [x] Global Exception Handler 작성
  - `@RestControllerAdvice` 또는 `@ControllerAdvice`
  - 공통 에러 응답 DTO 정의
- [x] API Response 공통 포맷 정의
  ```java
    ApiResponse<T> { code, message, data }
  ```

#### [x] Task 1-1-3: Enum 클래스 작성
- [x] `UserRole` (USER, ADMIN)
- [x] `GroupBuyStatus` (RECRUITING, IMMINENT, CLOSED)
- [x] `DeliveryMethod` (DIRECT, PARCEL, BOTH)
- [x] `PostCategory` (TIPS, FREE, REVIEW)
- [x] `CommentType` (Q_AND_A, GENERAL)
- [x] `NotificationType` (JOIN_GROUP_BUY, CANCEL_PARTICIPATION, ...)
- [x] `EntityType` (GROUP_BUY, POST, COMMENT, REVIEW)

---

### 1.2. 회원 관리 (User Domain)

#### [x] Task 1-2-1: User 엔티티 작성
- [x] 테스트 작성 (UserTest.java)
  - 매너온도 기본값 36.5 확인
  - 이메일/닉네임 유니크 제약 확인
- [x] User 엔티티 구현
  - 필드: id, email, password, nickname, phoneNumber, profileImageUrl, mannerTemperature, role
  - 인덱스: email, nickname
  - 제약조건: @Email, @Pattern(phoneNumber)
- [x] UserRepository 인터페이스 작성
  - `findByEmail(String email)`
  - `findByNickname(String nickname)`
  - `existsByEmail(String email)`
  - `existsByNickname(String nickname)`

#### [x] Task 1-2-2: Spring Security 설정
- [x] SecurityConfig 작성
  - 세션 기반 인증 설정
  - BCryptPasswordEncoder Bean 등록
  - permitAll / authenticated 경로 설정
- [x] CustomUserDetails, CustomUserDetailsService 구현
- [x] 로그인 성공/실패 핸들러 작성

#### [x] Task 1-2-3: 회원가입 기능
- [x] 테스트 작성 (UserServiceTest.java)
  - 회원가입 성공
  - 이메일 중복 실패
  - 닉네임 중복 실패
  - 비밀번호 암호화 확인
- [x] UserService 구현
  - `signup(SignupRequest dto)` 메서드
  - 이메일/닉네임 중복 검사
  - 비밀번호 암호화 (BCrypt)
  - 매너온도 기본값 36.5 설정
- [x] AuthController 구현
  - `POST /auth/signup`
  - 요청 DTO: SignupRequest (email, password, nickname, phoneNumber)
  - 응답 DTO: UserResponse (id, email, nickname, ...)
- [x] Validation 추가
  - @NotBlank, @Email, @Pattern, @Size

#### [x] Task 1-2-4: 로그인/로그아웃 기능
- [x] 테스트 작성 (AuthControllerTest.java)
  - 로그인 성공 시 세션 생성 확인
  - 잘못된 비밀번호 로그인 실패
  - 로그아웃 시 세션 무효화 확인
- [x] AuthService 구현
  - `login(LoginRequest dto)` 메서드
- [x] AuthController 구현
  - `POST /auth/login`
  - `POST /auth/logout`
  - 요청 DTO: LoginRequest (email, password)
- [x] 세션 관리 설정 (rememberMe 제외, 기본 세션 방식 사용)

#### [x] Task 1-2-5: 프로필 조회/수정 기능
- [x] 테스트 작성 (UserControllerTest.java)
  - 내 프로필 조회
  - 프로필 수정 (닉네임, 전화번호, 프로필 이미지)
  - 비밀번호 변경
- [x] UserService 구현
  - `getMyProfile(Long userId)`
  - `updateProfile(Long userId, UpdateProfileRequest dto)`
  - `changePassword(Long userId, ChangePasswordRequest dto)`
- [x] UserController 구현
  - `GET /users/me`
  - `PUT /users/me`
  - `PUT /users/me/password`

---

### 1.3. 공동구매 핵심 기능 (GroupBuy Domain)

#### [x] Task 1-3-1: GroupBuy 엔티티 작성
- [x] 테스트 작성 (GroupBuyTest.java)
  - 현재 인원이 목표 인원 초과 불가
  - 마감일이 생성일보다 이후인지 확인
  - 낙관적 락 버전 관리 확인
- [x] GroupBuy 엔티티 구현
  - 필드: id, host(User FK), version, title, content, category, totalPrice, targetHeadcount, currentHeadcount, deadline, deliveryMethod, meetupLocation, parcelFee, isParticipantListPublic, status, recipeApiId, recipeName, recipeImageUrl
  - 인덱스: status+deadline, recipeApiId, category, hostId
  - 제약조건: CHECK (currentHeadcount <= targetHeadcount)
  - `@Version` 필드 (낙관적 락)
- [x] GroupBuyRepository 인터페이스 작성
  - `findByStatusOrderByDeadlineAsc(GroupBuyStatus status)`
  - `findByHostId(Long hostId)`
  - `findByRecipeApiId(String recipeApiId)`
  - 페이징 쿼리 메서드

#### [x] Task 1-3-2: GroupBuyImage 엔티티 작성
- [x] 테스트 작성
  - 같은 공구 내 displayOrder 중복 불가
  - 최대 3장 제한 (Application Layer)
- [x] GroupBuyImage 엔티티 구현
  - 필드: id, groupBuy(FK), imageUrl, displayOrder
  - 제약조건: UNIQUE(groupBuyId, displayOrder)
  - orphanRemoval = true
- [x] GroupBuyImageRepository 작성
  - `findByGroupBuyIdOrderByDisplayOrderAsc(Long groupBuyId)`

#### [x] Task 1-3-3: 공구 생성 기능 (일반 공구)
- [x] 테스트 작성 (GroupBuyServiceTest.java)
  - 일반 공구 생성 성공
  - 필수 필드 누락 시 실패
  - 마감일이 과거일 때 실패
  - 이미지 3장 초과 시 실패
- [x] GroupBuyService 구현
  - `createGroupBuy(Long userId, CreateGroupBuyRequest dto)`
  - 유효성 검사 (마감일, 인원, 가격)
  - 이미지 업로드 처리 (파일 저장 로직)
  - 상태 기본값: RECRUITING
- [x] GroupBuyController 구현
  - `POST /group-purchases`
  - 요청 DTO: CreateGroupBuyRequest
  - 응답 DTO: GroupBuyResponse
- [x] 이미지 업로드 유틸 작성
  - 파일 저장 경로 설정 (로컬/S3)
  - 파일명 UUID 생성
  - 파일 크기/확장자 검증

#### [x] Task 1-3-4: 공구 목록 조회 기능
- [x] 테스트 작성 (GroupBuyServiceTest.java)
  - 전체 목록 조회 (페이징)
  - 카테고리별 필터링
  - 상태별 필터링 (모집중/마감임박/종료)
  - 레시피 기반 공구만 필터링
  - 검색 (제목, 내용)
- [x] GroupBuyService 구현
  - `getGroupBuyList(GroupBuySearchCondition condition, Pageable pageable)`
  - JPA Specification 사용
- [x] GroupBuyController 구현
  - `GET /group-purchases`
  - 쿼리 파라미터: category, status, recipeOnly, keyword, page, size, sort

#### [x] Task 1-3-5: 공구 상세 조회 기능
- [x] 테스트 작성
  - 공구 상세 조회 성공
  - 존재하지 않는 공구 조회 시 404
  - 주최자 정보 포함 (Fetch Join)
  - 이미지 목록 포함 (순서대로)
- [x] GroupBuyService 구현
  - `getGroupBuyDetail(Long purchaseId)`
  - Fetch Join으로 host 조회 (N+1 방지)
  - 이미지 별도 쿼리로 조회 (displayOrder 정렬)
- [x] GroupBuyController 구현
  - `GET /group-purchases/{purchaseId}`
  - 응답 DTO: GroupBuyResponse (주최자, 이미지 포함)

#### [x] Task 1-3-6: 공구 수정/삭제 기능
- [x] 테스트 작성
  - 주최자만 수정 가능
  - 참여자 있으면 삭제 불가
  - 레시피 연동 여부는 변경 불가
- [x] GroupBuyService 구현
  - `updateGroupBuy(Long userId, Long purchaseId, UpdateGroupBuyRequest dto)`
  - `deleteGroupBuy(Long userId, Long purchaseId)`
  - 권한 검증 (주최자 확인)
  - 소프트 삭제 (deletedAt 설정)
- [x] GroupBuyController 구현
  - `PUT /group-purchases/{purchaseId}`
  - `DELETE /group-purchases/{purchaseId}`

---

### 1.4. 공구 참여/취소 기능 (Participation Domain)

#### [ ] Task 1-4-1: Participation 엔티티 작성
- [ ] 테스트 작성
  - 중복 참여 방지 (user + groupBuy 유니크)
  - selectedDeliveryMethod는 DIRECT 또는 PARCEL만 가능
- [ ] Participation 엔티티 구현
  - 필드: id, user(FK), groupBuy(FK), quantity, selectedDeliveryMethod, participatedAt
  - 제약조건: UNIQUE(userId, groupBuyId)
  - 인덱스: userId, groupBuyId
- [ ] ParticipationRepository 작성
  - `findByUserIdAndGroupBuyId(Long userId, Long groupBuyId)`
  - `findByGroupBuyId(Long groupBuyId)`
  - `countByGroupBuyId(Long groupBuyId)`

#### [ ] Task 1-4-2: 공구 참여 기능
- [ ] 테스트 작성 (ParticipationServiceTest.java)
  - 참여 성공 시 currentHeadcount 증가
  - 중복 참여 방지
  - 목표 인원 달성 시 참여 불가
  - 마감된 공구 참여 불가
  - 주최자 본인 참여 불가
  - 낙관적 락 충돌 시 재시도
- [ ] ParticipationService 구현
  - `participate(Long userId, Long purchaseId, ParticipateRequest dto)`
  - GroupBuy의 currentHeadcount++ (트랜잭션)
  - 목표 달성 시 status → CLOSED
  - 낙관적 락 예외 처리 (@Retryable)
- [ ] GroupBuyController 구현
  - `POST /group-purchases/{purchaseId}/participate`
  - 요청 DTO: ParticipateRequest (selectedDeliveryMethod, quantity)

#### [ ] Task 1-4-3: 공구 참여 취소 기능
- [ ] 테스트 작성
  - 취소 성공 시 currentHeadcount 감소
  - 마감 1일 전 취소 제한 (비즈니스 로직)
  - 참여하지 않은 공구 취소 불가
- [ ] ParticipationService 구현
  - `cancelParticipation(Long userId, Long purchaseId)`
  - currentHeadcount-- (트랜잭션)
  - 마감일 확인 로직
- [ ] GroupBuyController 구현
  - `DELETE /group-purchases/{purchaseId}/participate`

#### [ ] Task 1-4-4: 참여자 목록 조회 기능
- [ ] 테스트 작성
  - 공개 설정 시 모든 사용자 조회 가능
  - 비공개 설정 시 주최자만 조회 가능
- [ ] ParticipationService 구현
  - `getParticipants(Long purchaseId, Long currentUserId)`
  - 권한 검증 (isParticipantListPublic 확인)
- [ ] GroupBuyController 구현
  - `GET /group-purchases/{purchaseId}/participants`
  - 응답: ParticipantResponse 리스트 (닉네임, 매너온도, 참여일)

---

### 1.5. 검색 및 필터링

#### [ ] Task 1-5-1: 공구 검색 기능
- [ ] 테스트 작성
  - 제목으로 검색
  - 내용으로 검색
  - 카테고리 필터링
  - 상태 필터링
  - 복합 조건 검색
- [ ] GroupBuyRepositoryCustom 인터페이스 작성
  - QueryDSL 기반 동적 쿼리
- [ ] GroupBuyRepositoryImpl 구현
  - `searchGroupBuys(GroupBuySearchCondition condition, Pageable pageable)`
- [ ] 통합 검색 API
  - `GET /search?query={keyword}` (나중에 구현)

---

### 1.6. 마이페이지 기본 기능

#### [ ] Task 1-6-1: 내가 만든 공구 목록
- [ ] 테스트 작성
  - 진행중/완료/취소 필터링
- [ ] UserService 구현
  - `getMyGroupBuys(Long userId, GroupBuyStatus status, Pageable pageable)`
- [ ] UserController 구현
  - `GET /users/me/group-purchases`

#### [ ] Task 1-6-2: 내가 참여한 공구 목록
- [ ] 테스트 작성
  - 참여중/완료/취소 필터링
- [ ] UserService 구현
  - `getParticipatedGroupBuys(Long userId, String status, Pageable pageable)`
- [ ] UserController 구현
  - `GET /users/me/participations`

---

## 🟡 Phase 2: 레시피 연동 및 차별화 기능 (Week 3-4)

### 2.1. 외부 API 연동 (Recipe Domain)

#### [ ] Task 2-1-1: TheMealDB API 클라이언트 작성
- [ ] 테스트 작성 (API Mock 사용)
  - 레시피 검색 (이름)
  - 레시피 상세 조회 (ID)
  - 랜덤 레시피 조회
  - 카테고리 목록 조회
- [ ] RestTemplate 설정
  - API Base URL 설정
  - 타임아웃 설정
- [ ] TheMealDBClient 구현
  - `searchRecipes(String name)`
  - `getRecipeDetail(String id)`
  - `getRandomRecipes(int count)`
  - `getCategories()`
- [ ] DTO 작성
  - MealResponse (strMeal, strMealThumb, strIngredient1~20, strMeasure1~20, ...)

#### [ ] Task 2-1-2: 식품안전나라 API 클라이언트 작성
- [ ] 테스트 작성
  - 한식 레시피 조회
- [ ] FoodSafetyClient 구현
  - `getKoreanRecipes(int start, int end)`
- [ ] DTO 작성
  - CookRecipeResponse (RCP_NM, RCP_PARTS_DTLS, MANUAL01~20, ...)

#### [ ] Task 2-1-3: Recipe 서비스 레이어 작성
- [ ] RecipeService 구현
  - `searchRecipes(String keyword)`
  - `getRecipeDetail(String apiId)`
  - `getRandomRecipes()`
  - `getCategories()`
  - API 응답 캐싱 (Redis - 선택)
- [ ] 재료 파싱 로직
  - strIngredient1~20 추출
  - null/빈 문자열 필터링
  - 재료 + 분량 조합

#### [ ] Task 2-1-4: Recipe 컨트롤러 작성
- [ ] RecipeController 구현
  - `GET /recipes` (검색, 필터링)
  - `GET /recipes/{recipeId}` (상세 조회)
  - `GET /recipes/random` (랜덤 추천)
  - `GET /recipes/categories`

---

### 2.2. 레시피 기반 공구 생성

#### [ ] Task 2-2-1: 레시피 기반 공구 생성 기능
- [ ] 테스트 작성
  - 레시피 ID 포함하여 공구 생성
  - recipeName, recipeImageUrl 자동 채워짐
  - 재료 목록 파싱 후 content에 자동 입력
- [ ] GroupBuyService 확장
  - `createRecipeBasedGroupBuy(Long userId, CreateRecipeGroupBuyRequest dto)`
  - 레시피 API 호출하여 재료 가져오기
  - recipeName, recipeImageUrl 캐싱
- [ ] GroupBuyController 확장
  - `POST /group-purchases/recipe-based`
  - 요청 DTO: CreateRecipeGroupBuyRequest (recipeApiId, selectedIngredients, targetHeadcount, ...)

#### [ ] Task 2-2-2: 레시피 상세 페이지에서 공구 연결
- [ ] 레시피 기반 진행 중인 공구 조회
  - `GET /recipes/{recipeId}/group-purchases`
- [ ] RecipeService 확장
  - `getRelatedGroupBuys(String recipeApiId)`

---

### 2.3. 찜 기능 (Wishlist Domain)

#### [ ] Task 2-3-1: Wishlist 엔티티 작성
- [ ] 테스트 작성
  - 중복 찜 방지 (user + groupBuy 유니크)
- [ ] Wishlist 엔티티 구현
  - 필드: id, user(FK), groupBuy(FK), wishedAt
  - 제약조건: UNIQUE(userId, groupBuyId)
  - 인덱스: userId+wishedAt, groupBuyId
- [ ] WishlistRepository 작성
  - `findByUserIdAndGroupBuyId(Long userId, Long groupBuyId)`
  - `findByUserIdOrderByWishedAtDesc(Long userId, Pageable pageable)`

#### [ ] Task 2-3-2: 찜하기/취소 기능
- [ ] 테스트 작성
  - 찜 추가 성공
  - 중복 찜 방지
  - 찜 취소 성공
- [ ] WishlistService 구현
  - `addWishlist(Long userId, Long purchaseId)`
  - `removeWishlist(Long userId, Long purchaseId)`
- [ ] GroupBuyController 구현
  - `POST /group-purchases/{purchaseId}/bookmarks`
  - `DELETE /group-purchases/{purchaseId}/bookmarks`

#### [ ] Task 2-3-3: 찜 목록 조회
- [ ] UserController 구현
  - `GET /users/me/bookmarks`

---

## 🟢 Phase 3: 커뮤니티 및 후기 시스템 (Week 5)

### 3.1. 커뮤니티 게시판 (Post Domain)

#### [ ] Task 3-1-1: Post 엔티티 작성
- [ ] 테스트 작성
  - 카테고리별 게시글 조회
  - 조회수 증가 (중복 방지)
- [ ] Post 엔티티 구현
  - 필드: id, author(User FK), title, content, category, viewCount
  - 인덱스: category+createdAt, authorId
- [ ] PostRepository 작성
  - `findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable)`
  - `findByAuthorId(Long authorId)`

#### [ ] Task 3-1-2: PostImage 엔티티 작성
- [ ] PostImage 엔티티 구현
  - 필드: id, post(FK), imageUrl, displayOrder
  - 제약조건: UNIQUE(postId, displayOrder)
  - 최대 5장 제한 (Application Layer)
- [ ] PostImageRepository 작성

#### [ ] Task 3-1-3: 게시글 CRUD 기능
- [ ] 테스트 작성
  - 게시글 작성/조회/수정/삭제
  - 작성자만 수정/삭제 가능
- [ ] PostService 구현
  - `createPost(Long userId, CreatePostRequest dto)`
  - `getPostDetail(Long postId)`
  - `updatePost(Long userId, Long postId, UpdatePostRequest dto)`
  - `deletePost(Long userId, Long postId)`
- [ ] PostController 구현
  - `POST /community-posts`
  - `GET /community-posts/{postId}`
  - `PUT /community-posts/{postId}`
  - `DELETE /community-posts/{postId}`

#### [ ] Task 3-1-4: 게시글 목록 조회 및 검색
- [ ] PostService 구현
  - `getPostList(PostCategory category, String keyword, Pageable pageable)`
- [ ] PostController 구현
  - `GET /community-posts`

---

### 3.2. 댓글 시스템 (Comment Domain)

#### [ ] Task 3-2-1: Comment 엔티티 작성
- [ ] 테스트 작성
  - 공구 또는 게시글 중 하나에만 연결
  - 대댓글 (parent) 관계 확인
  - Q&A vs 일반 댓글 구분
- [ ] Comment 엔티티 구현
  - 필드: id, author(User FK), groupBuy(FK, Nullable), post(FK, Nullable), parent(FK, Nullable), content, type
  - 제약조건: CHECK (groupBuy XOR post)
  - 인덱스: groupBuyId, postId, parentId
- [ ] CommentRepository 작성
  - `findByGroupBuyIdAndParentIsNullOrderByCreatedAtAsc(Long groupBuyId)`
  - `findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId)`
  - `findByParentIdOrderByCreatedAtAsc(Long parentId)`

#### [ ] Task 3-2-2: 댓글 작성/수정/삭제 기능
- [ ] 테스트 작성
  - 공구에 댓글 작성
  - 게시글에 댓글 작성
  - 대댓글 작성
  - 본인 댓글만 수정/삭제
  - 소프트 삭제 ("삭제된 댓글입니다")
- [ ] CommentService 구현
  - `createComment(Long userId, CreateCommentRequest dto)`
  - `updateComment(Long userId, Long commentId, UpdateCommentRequest dto)`
  - `deleteComment(Long userId, Long commentId)`
- [ ] Controller 구현
  - 공구 댓글: `/group-purchases/{purchaseId}/comments`
  - 게시글 댓글: `/community-posts/{postId}/comments`

---

### 3.3. 후기 및 매너온도 시스템 (Review Domain)

#### [ ] Task 3-3-1: Review 엔티티 작성
- [ ] 테스트 작성
  - 중복 후기 방지 (reviewer + groupBuy 유니크)
  - 별점 1~5 범위 제한
  - 참여자만 후기 작성 가능
  - 완료된 공구만 후기 작성 가능
- [ ] Review 엔티티 구현
  - 필드: id, reviewer(User FK), groupBuy(FK), rating, content
  - 제약조건: UNIQUE(reviewerId, groupBuyId), CHECK (rating BETWEEN 1 AND 5)
  - 인덱스: groupBuyId, reviewerId
- [ ] ReviewRepository 작성
  - `findByGroupBuyIdOrderByCreatedAtDesc(Long groupBuyId)`
  - `existsByReviewerIdAndGroupBuyId(Long reviewerId, Long groupBuyId)`

#### [ ] Task 3-3-2: 후기 작성 및 매너온도 반영
- [ ] 테스트 작성
  - 후기 작성 시 주최자 매너온도 변동
    - 5점: +0.5, 4점: +0.3, 3점: 0, 2점: -1.0, 1점: -2.0
  - 중복 후기 방지
  - 참여 여부 확인
- [ ] ReviewService 구현
  - `createReview(Long userId, Long purchaseId, CreateReviewRequest dto)`
  - 참여 여부 확인 (Participation 조회)
  - 공구 상태 확인 (CLOSED)
  - 매너온도 업데이트 로직
- [ ] GroupBuyController 구현
  - `POST /group-purchases/{purchaseId}/reviews`

#### [ ] Task 3-3-3: 후기 목록 조회
- [ ] GroupBuyController 구현
  - `GET /group-purchases/{purchaseId}/reviews`

---

### 3.4. 알림 시스템 (Notification Domain)

#### [ ] Task 3-4-1: Notification 엔티티 작성
- [ ] 테스트 작성
  - 알림 생성 시 actor, relatedEntityId 저장
  - 읽지 않은 알림만 조회
- [ ] Notification 엔티티 구현
  - 필드: id, user(FK), actor(FK), content, url, isRead, type, relatedEntityId, relatedEntityType
  - 인덱스: userId+isRead+createdAt, userId+createdAt
- [ ] NotificationRepository 작성
  - `findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead)`
  - `countByUserIdAndIsReadFalse(Long userId)`

#### [ ] Task 3-4-2: 알림 생성 로직
- [ ] NotificationService 구현
  - `createNotification(Long userId, NotificationType type, Long actorId, Long relatedEntityId, EntityType entityType)`
  - 알림 템플릿 생성 ("OOO님이 참여했습니다")
  - 본인 행동은 알림 생성 안 함
- [ ] 각 도메인 서비스에 알림 생성 로직 추가
  - 참여 시: JOIN_GROUP_BUY
  - 참여 취소 시: CANCEL_PARTICIPATION
  - 댓글 작성 시: COMMENT_GROUP_BUY, COMMENT_POST
  - 대댓글 작성 시: REPLY_COMMENT
  - 후기 작성 시: REVIEW_GROUP_BUY

#### [ ] Task 3-4-3: 알림 조회/읽음 처리/삭제
- [ ] UserController 구현
  - `GET /users/me/notifications`
  - `PUT /users/me/notifications/{notificationId}` (읽음 처리)
  - `DELETE /users/me/notifications` (전체 삭제)

---

## ⚪ Phase 4: 최적화 및 배치 작업 (Week 6)

### 4.1. 배치 작업

#### [ ] Task 4-1-1: 공구 상태 자동 변경 배치
- [ ] @Scheduled 설정
- [ ] 매일 자동 실행
  - 마감일 지난 공구 → CLOSED
  - D-1, D-2 공구 → IMMINENT
- [ ] 마감 임박 알림 발송
  - 찜한 공구 마감 D-1 알림

#### [ ] Task 4-1-2: 읽은 알림 자동 삭제
- [ ] 30일 지난 읽은 알림 삭제 배치

---

### 4.2. 성능 최적화

#### [ ] Task 4-2-1: 인덱스 최적화
- [ ] 실제 쿼리 프로파일링
- [ ] Slow Query 분석
- [ ] 복합 인덱스 추가/조정

#### [ ] Task 4-2-2: N+1 문제 해결
- [ ] Fetch Join 적용
- [ ] @EntityGraph 사용
- [ ] Batch Size 설정

#### [ ] Task 4-2-3: 캐싱 전략 (선택)
- [ ] Redis 설정
- [ ] 레시피 API 응답 캐싱
- [ ] 인기 공구 목록 캐싱
- [ ] 조회수 캐싱

---

### 4.3. 프론트엔드 개발

#### [ ] Task 4-3-1: Thymeleaf 템플릿 작성
- [ ] 레이아웃 템플릿 (header, footer, nav)
- [ ] 홈 화면
- [ ] 레시피 목록/상세
- [ ] 공구 목록/상세/작성/수정
- [ ] 커뮤니티 목록/상세/작성
- [ ] 마이페이지 (모든 탭)
- [ ] 로그인/회원가입

#### [ ] Task 4-3-2: Bootstrap 스타일링
- [ ] CDN 추가
- [ ] 반응형 레이아웃
- [ ] 카드 컴포넌트 (공구, 레시피)
- [ ] 폼 스타일링
- [ ] 버튼/모달/알림 스타일

#### [ ] Task 4-3-3: htmx/Alpine.js 적용
- [ ] htmx로 부분 페이지 갱신
  - 댓글 작성/삭제
  - 좋아요/찜하기
  - 참여하기/취소
- [ ] Alpine.js로 클라이언트 상호작용
  - 드롭다운 메뉴
  - 모달 팝업
  - 이미지 갤러리

---

### 4.4. 배포 준비

#### [ ] Task 4-4-1: Docker 설정
- [ ] Dockerfile 작성
- [ ] docker-compose.yml 작성 (PostgreSQL + Spring Boot)
- [ ] Nginx 리버스 프록시 설정

#### [ ] Task 4-4-2: 프로덕션 설정
- [ ] application-prod.yml 작성
- [ ] PostgreSQL DDL 스크립트 작성
- [ ] 환경변수 설정 (.env)

#### [ ] Task 4-4-3: 테스트 및 버그 수정
- [ ] 전체 E2E 테스트
- [ ] 주요 시나리오 테스트
- [ ] 버그 수정 및 리팩터링

---

### 4.5. 확장 기능 (4순위 - 선택적, 시간 여유 시)

#### [ ] Task 4-5-1: 번역 API 연동
- [ ] 테스트 작성
  - TheMealDB 영문 레시피명 → 한글 번역
  - 재료명 번역 캐싱
- [ ] Google Translation API 설정
  - API Key 발급 및 환경변수 설정
  - 번역 클라이언트 작성
- [ ] TranslationService 구현
  - `translateRecipeName(String englishName)`
  - `translateIngredient(String ingredient)`
  - 번역 결과 캐싱 (Redis 또는 로컬 캐시)
- [ ] RecipeService에 번역 로직 통합
  - 레시피 조회 시 자동 번역 옵션
  - 사용자 언어 설정에 따라 번역 제공

#### [ ] Task 4-5-2: 지도 API 연동 (만남 장소 표시)
- [ ] 테스트 작성
  - 주소 → 위도/경도 변환 (Geocoding)
  - 지도 표시 컴포넌트 렌더링
- [ ] 카카오/네이버 지도 API 설정
  - API Key 발급
  - JavaScript SDK 추가
- [ ] GroupBuy 엔티티 확장
  - latitude, longitude 필드 추가 (Nullable)
- [ ] MapService 구현
  - `geocodeAddress(String address)` (주소 → 좌표)
  - `reverseGeocode(Double lat, Double lng)` (좌표 → 주소)
- [ ] 프론트엔드 통합
  - 공구 작성 시 지도에서 장소 선택
  - 공구 상세 페이지에 지도 표시

#### [ ] Task 4-5-3: 뱃지 시스템
- [ ] Badge 엔티티 작성
  - 테스트 작성
    - 뱃지 조건 달성 여부 확인
    - 중복 획득 방지 (user + badgeType 유니크)
  - Badge 엔티티 구현
    - 필드: id, user(FK), badgeType (Enum), acquiredAt
    - BadgeType Enum: FIRST_GROUP_BUY, TEN_PARTICIPATIONS, REVIEWER, POPULAR_HOST, ...
    - 제약조건: UNIQUE(userId, badgeType)
  - BadgeRepository 작성
    - `findByUserId(Long userId)`
    - `existsByUserIdAndBadgeType(Long userId, BadgeType badgeType)`
- [ ] BadgeService 구현
  - `checkAndAwardBadge(Long userId, BadgeType badgeType)`
  - 조건 체크 로직
    - FIRST_GROUP_BUY: 첫 공구 생성 시
    - TEN_PARTICIPATIONS: 참여 10회 달성 시
    - REVIEWER: 후기 5개 작성 시
    - POPULAR_HOST: 매너온도 40도 이상 달성 시
  - 각 도메인 서비스에 뱃지 획득 로직 추가
- [ ] 마이페이지에 뱃지 목록 표시
  - UserController에 엔드포인트 추가
  - `GET /users/me/badges`

#### [ ] Task 4-5-4: 포인트 시스템
- [ ] User 엔티티 확장
  - 테스트 작성
    - 포인트 적립/차감 로직 확인
    - 포인트 잔액 0 미만 불가
  - User 엔티티에 points 필드 추가 (기본값: 0)
- [ ] PointHistory 엔티티 작성
  - 필드: id, user(FK), amount, description, type (EARN, USE), createdAt
  - 인덱스: userId+createdAt
  - PointHistoryRepository 작성
- [ ] PointService 구현
  - `earnPoints(Long userId, Integer amount, String description)`
  - `usePoints(Long userId, Integer amount, String description)`
  - `getPointHistory(Long userId, Pageable pageable)`
  - 포인트 적립 규칙
    - 공구 생성: +50
    - 공구 참여: +10
    - 후기 작성: +20
    - 출석 체크: +5 (선택)
- [ ] 각 도메인 서비스에 포인트 적립 로직 추가
- [ ] 마이페이지에 포인트 내역 표시
  - `GET /users/me/points`
  - `GET /users/me/points/history`

#### [ ] Task 4-5-5: 이미지 최적화
- [ ] Thumbnailator 라이브러리 추가
  - build.gradle 의존성 추가
- [ ] 테스트 작성
  - 원본 이미지 → 썸네일 생성
  - JPEG/PNG → WebP 변환
  - 파일 크기 감소 확인
- [ ] ImageOptimizationService 구현
  - `createThumbnail(MultipartFile file, int width, int height)`
  - `convertToWebP(MultipartFile file)`
  - `optimizeImage(MultipartFile file, float quality)`
- [ ] 기존 이미지 업로드 로직에 통합
  - GroupBuyImage, PostImage 업로드 시 자동 최적화
  - 원본 + 썸네일 2종 저장 (선택)
  - CDN 연동 고려 (선택)

#### [ ] Task 4-5-6: 실시간 채팅 (선택 - 복잡도 높음)
- [ ] WebSocket 설정
  - Spring WebSocket 의존성 추가
  - WebSocketConfig 작성
  - STOMP 프로토콜 설정
- [ ] ChatMessage 엔티티 작성
  - 필드: id, sender(User FK), receiver(User FK), content, sentAt, isRead
  - 제약조건: 인덱스 (senderId, receiverId, sentAt)
- [ ] ChatService 구현
  - `sendMessage(Long senderId, Long receiverId, String content)`
  - `getMessageHistory(Long userId, Long otherUserId, Pageable pageable)`
  - `markAsRead(Long messageId)`
- [ ] WebSocket 컨트롤러 작성
  - `/topic/chat/{userId}` 구독 엔드포인트
  - `/app/chat.send` 메시지 전송 엔드포인트
- [ ] 프론트엔드 통합
  - SockJS + STOMP.js 클라이언트
  - 채팅방 UI 컴포넌트
  - 실시간 메시지 수신 처리
- [ ] 주의사항
  - 복잡도가 높아 시간 여유가 충분할 때만 구현
  - 대안: 기본 댓글 기능으로 대체 가능

---

## 📊 작업 추적

### Week 1-2: Phase 1 (프로젝트 초기 설정 + 회원 + 공구 핵심)
- [ ] 프로젝트 초기 설정 (1.1)
- [ ] 회원 관리 (1.2)
- [ ] 공동구매 핵심 기능 (1.3)
- [ ] 공구 참여/취소 (1.4)
- [ ] 검색 및 필터링 (1.5)
- [ ] 마이페이지 기본 (1.6)

### Week 3-4: Phase 2 (레시피 연동 + 차별화)
- [ ] 외부 API 연동 (2.1)
- [ ] 레시피 기반 공구 생성 (2.2)
- [ ] 찜 기능 (2.3)

### Week 5: Phase 3 (커뮤니티 + 후기 + 알림)
- [ ] 커뮤니티 게시판 (3.1)
- [ ] 댓글 시스템 (3.2)
- [ ] 후기 및 매너온도 (3.3)
- [ ] 알림 시스템 (3.4)

### Week 6: Phase 4 (최적화 + 배치 + 프론트 + 배포)
- [ ] 배치 작업 (4.1)
- [ ] 성능 최적화 (4.2)
- [ ] 프론트엔드 개발 (4.3)
- [ ] 배포 준비 (4.4)
- [ ] 확장 기능 (4.5) - 선택적

---

## 🎯 우선순위 라벨

- **P0 (Critical)**: 핵심 기능, 없으면 서비스 불가
- **P1 (High)**: 차별화 기능, 중요하지만 초기에는 간소화 가능
- **P2 (Medium)**: 부가 기능, 사용자 경험 향상
- **P3 (Low)**: 확장 기능, 시간 여유 시 추가

---

## 📝 개발 가이드라인 (재확인)

1. **TDD 방식**: 테스트 먼저 → 구현 → 리팩터링
2. **단계적 개발**: 간단한 버전 먼저 → 점진적 개선
3. **Best Practice**: SOLID 원칙, Clean Code
4. **코드 리뷰**: 주요 기능 완성 시 팀원 리뷰
5. **문서화**: README, API 문서, ERD 업데이트
6. **Git 전략**: Feature Branch → Develop → Main

---

**문서 버전**: v1.1  
**최종 수정일**: 2025-01-26  
**작성자**: RecipeMate 개발팀

