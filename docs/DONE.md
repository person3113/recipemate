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
    - `GET /users/me` (마이페이지 렌더링)
    - `POST /users/me` (프로필 수정 폼 제출)
    - `POST /users/me/password` (비밀번호 변경 폼 제출)

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
    - `GET /group-purchases/list`
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
    - `POST /group-purchases/{purchaseId}` (수정)
    - `POST /group-purchases/{purchaseId}/delete` (삭제)

---

### 1.4. 공구 참여/취소 기능 (Participation Domain)

#### [x] Task 1-4-1: Participation 엔티티 작성
- [x] 테스트 작성
    - 중복 참여 방지 (user + groupBuy 유니크)
    - selectedDeliveryMethod는 DIRECT 또는 PARCEL만 가능
- [x] Participation 엔티티 구현
    - 필드: id, user(FK), groupBuy(FK), quantity, selectedDeliveryMethod, participatedAt
    - 제약조건: UNIQUE(userId, groupBuyId)
    - 인덱스: userId, groupBuyId
- [x] ParticipationRepository 작성
    - `findByUserIdAndGroupBuyId(Long userId, Long groupBuyId)`
    - `findByGroupBuyId(Long groupBuyId)`
    - `countByGroupBuyId(Long groupBuyId)`

#### [x] Task 1-4-2: 공구 참여 기능
- [x] 테스트 작성 (ParticipationServiceTest.java)
    - 참여 성공 시 currentHeadcount 증가
    - 중복 참여 방지
    - 목표 인원 달성 시 참여 불가
    - 마감된 공구 참여 불가
    - 주최자 본인 참여 불가
    - 목표 달성 시 status → CLOSED
- [x] ParticipationService 구현
    - `participate(Long userId, Long purchaseId, ParticipateRequest dto)`
    - GroupBuy의 currentHeadcount++ (트랜잭션)
    - 목표 달성 시 status → CLOSED
    - 낙관적 락 예외 처리 (@Retryable)
- [x] GroupBuyController 구현
    - `POST /group-purchases/{purchaseId}/participate`
    - 요청 DTO: ParticipateRequest (selectedDeliveryMethod, quantity)

#### [x] Task 1-4-3: 공구 참여 취소 기능
- [x] 테스트 작성
    - 취소 성공 시 currentHeadcount 감소
    - 마감 1일 전 취소 제한 (비즈니스 로직)
    - 참여하지 않은 공구 취소 불가
- [x] ParticipationService 구현
    - `cancelParticipation(Long userId, Long purchaseId)`
    - currentHeadcount-- (트랜잭션)
    - 마감일 확인 로직
- [x] GroupBuyController 구현
    - `POST /group-purchases/{purchaseId}/participate/cancel`

#### [x] Task 1-4-4: 참여자 목록 조회 기능
- [x] 테스트 작성
    - 공개 설정 시 모든 사용자 조회 가능
    - 비공개 설정 시 주최자만 조회 가능
- [x] ParticipationService 구현
    - `getParticipants(Long purchaseId, Long currentUserId)`
    - 권한 검증 (isParticipantListPublic 확인)
- [x] GroupBuyController 구현
    - `GET /group-purchases/{purchaseId}/participants`
    - 응답: ParticipantResponse 리스트 (닉네임, 매너온도, 참여일)

---

### 1.5. 검색 및 필터링

#### [x] Task 1-5-1: 공구 검색 기능
- [x] 테스트 작성
    - 제목으로 검색
    - 내용으로 검색
    - 카테고리 필터링
    - 상태 필터링
    - 복합 조건 검색
- [x] GroupBuyRepositoryCustom 인터페이스 작성
    - QueryDSL 기반 동적 쿼리
- [x] GroupBuyRepositoryImpl 구현
    - `searchGroupBuys(GroupBuySearchCondition condition, Pageable pageable)`
- [ ] 통합 검색 API
    - `GET /search?query={keyword}` (나중에 구현)

---

### 1.6. 마이페이지 기본 기능

#### [x] Task 1-6-1: 내가 만든 공구 목록
- [x] 테스트 작성
    - 진행중/완료/취소 필터링
- [x] UserService 구현
    - `getMyGroupBuys(Long userId, GroupBuyStatus status, Pageable pageable)`
- [x] UserController 구현
    - `GET /users/me/group-purchases` (내가 만든 공구 목록 페이지)

#### [x] Task 1-6-2: 내가 참여한 공구 목록
- [x] 테스트 작성
    - 참여중/완료/취소 필터링
- [x] UserService 구현
    - `getParticipatedGroupBuys(Long userId, String status, Pageable pageable)`
- [x] UserController 구현
    - `GET /users/me/participations` (내가 참여한 공구 목록 페이지)

---

## 🟡 Phase 2: 레시피 연동 및 차별화 기능 (Week 3-4)

### 2.1. 외부 API 연동 (Recipe Domain)

#### [x] Task 2-1-1: TheMealDB API 클라이언트 작성
- [x] 테스트 작성 (API Mock 사용)
    - 레시피 검색 (이름)
    - 레시피 상세 조회 (ID)
    - 랜덤 레시피 조회
    - 카테고리 목록 조회
- [x] RestTemplate 설정
    - API Base URL 설정
    - 타임아웃 설정
- [x] TheMealDBClient 구현
    - `searchRecipes(String name)`
    - `getRecipeDetail(String id)`
    - `getRandomRecipes(int count)`
    - `getCategories()`
- [x] DTO 작성
    - MealResponse (strMeal, strMealThumb, strIngredient1~20, strMeasure1~20, ...)

#### [x] Task 2-1-2: 식품안전나라 API 클라이언트 작성
- [x] 테스트 작성
    - 한식 레시피 조회
    - 레시피 이름으로 검색
    - 재료로 레시피 검색
    - 요리 종류로 필터링
    - 잘못된 범위/1000건 초과 요청 검증
    - API 응답 파싱 검증
- [x] FoodSafetyClient 구현
    - `getKoreanRecipes(int start, int end)`
    - `searchRecipesByName(String keyword, int start, int end)`
    - `searchRecipesByIngredient(String ingredient, int start, int end)`
    - `searchRecipesByCategory(String category, int start, int end)`
    - 페이징 처리 (최대 1,000건 제한)
    - 에러 처리 (RESULT.CODE 검증)
- [x] DTO 작성
    - FoodSafetyApiResult (CODE, MSG)
    - CookRecipeResponse (RCP_NM, RCP_PARTS_DTLS, MANUAL01~20, 동적 필드 처리)
    - CookRecipeListResponse (COOKRCP01 래퍼)
- [x] application.yml 설정
    - food.safety.api.key (환경변수)
    - food.safety.api.base-url

#### [x] Task 2-1-3: Recipe 서비스 레이어 작성
- [x] RecipeService 구현
    - `searchRecipes(String keyword)`
    - `getRecipeDetail(String apiId)`
    - `getRandomRecipes()`
    - `getCategories()`
    - API 응답 캐싱 (Redis - 선택)
- [x] 재료 파싱 로직
    - strIngredient1~20 추출
    - null/빈 문자열 필터링
    - 재료 + 분량 조합

#### [x] Task 2-1-4: Recipe 컨트롤러 작성
- [x] 테스트 작성 (RecipeControllerTest.java)
    - 레시피 검색 페이지 렌더링
    - 레시피 상세 페이지 렌더링
    - 랜덤 레시피 조회
    - 카테고리 목록 조회
- [x] RecipeController 구현
    - `GET /recipes` (검색, 필터링)
    - `GET /recipes/{recipeId}` (상세 조회)
    - `GET /recipes/random` (랜덤 추천)
    - `GET /recipes/categories`
- [x] Thymeleaf 템플릿 작성
    - templates/recipes/list.html
    - templates/recipes/detail.html
    - templates/recipes/random.html
    - templates/recipes/categories.html

---

### 2.2. 레시피 기반 공구 생성

#### [x] Task 2-2-1: 레시피 기반 공구 생성 기능
- [x] 테스트 작성
    - 레시피 ID 포함하여 공구 생성
    - recipeName, recipeImageUrl 자동 채워짐
    - 재료 목록 파싱 후 content에 자동 입력
- [x] GroupBuyService 확장
    - `createRecipeBasedGroupBuy(Long userId, CreateRecipeGroupBuyRequest dto)`
    - 레시피 API 호출하여 재료 가져오기
    - recipeName, recipeImageUrl 캐싱
- [x] GroupBuyController 확장
    - `POST /group-purchases/recipe-based`
    - 요청 DTO: CreateRecipeGroupBuyRequest (recipeApiId, selectedIngredients, targetHeadcount, ...)

#### [x] Task 2-2-2: 레시피 상세 페이지에서 공구 연결
- [x] 레시피 기반 진행 중인 공구 조회
    - `GET /recipes/{recipeId}/group-purchases`
- [x] RecipeService 확장
    - `getRelatedGroupBuys(String recipeApiId)`

---

### 2.3. 찜 기능 (Wishlist Domain)

#### [x] Task 2-3-1: Wishlist 엔티티 작성
- [x] 테스트 작성
    - 중복 찜 방지 (user + groupBuy 유니크)
- [x] Wishlist 엔티티 구현
    - 필드: id, user(FK), groupBuy(FK), wishedAt
    - 제약조건: UNIQUE(userId, groupBuyId)
    - 인덱스: userId+wishedAt, groupBuyId
- [x] WishlistRepository 작성
    - `findByUserIdAndGroupBuyId(Long userId, Long groupBuyId)`
    - `findByUserIdOrderByWishedAtDesc(Long userId, Pageable pageable)`

#### [x] Task 2-3-2: 찜하기/취소 기능
- [x] 테스트 작성
    - 찜 추가 성공
    - 중복 찜 방지
    - 찜 취소 성공
- [x] WishlistService 구현
    - `addWishlist(Long userId, Long purchaseId)`
    - `removeWishlist(Long userId, Long purchaseId)`
- [x] GroupBuyController 구현
    - `POST /group-purchases/{purchaseId}/bookmarks` (찜하기)
    - `POST /group-purchases/{purchaseId}/bookmarks/cancel` (찜 취소)
    - htmx 사용 시 DELETE도 가능

#### [x] Task 2-3-3: 찜 목록 조회
- [x] UserController 구현
    - `GET /users/me/bookmarks` (찜 목록 페이지)

---

## 🟢 Phase 3: 커뮤니티 및 후기 시스템 (Week 5)

### 3.1. 커뮤니티 게시판 (Post Domain)

#### [x] Task 3-1-1: Post 엔티티 작성
- [x] 테스트 작성
    - 카테고리별 게시글 조회
    - 조회수 증가 (중복 방지)
- [x] Post 엔티티 구현
    - 필드: id, author(User FK), title, content, category, viewCount
    - 인덱스: category+createdAt, authorId
- [x] PostRepository 작성
    - `findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable)`
    - `findByAuthorId(Long authorId)`

#### [x] Task 3-1-2: PostImage 엔티티 작성
- [x] PostImage 엔티티 구현
    - 필드: id, post(FK), imageUrl, displayOrder
    - 제약조건: UNIQUE(postId, displayOrder)
    - 최대 5장 제한 (Application Layer)
- [x] PostImageRepository 작성

#### [x] Task 3-1-3: 게시글 CRUD 기능
- [x] 테스트 작성
    - 게시글 작성/조회/수정/삭제
    - 작성자만 수정/삭제 가능
- [x] PostService 구현
    - `createPost(Long userId, CreatePostRequest dto)`
    - `getPostDetail(Long postId)`
    - `updatePost(Long userId, Long postId, UpdatePostRequest dto)`
    - `deletePost(Long userId, Long postId)`
- [x] PostController 구현
    - `GET /community-posts/new` (작성 페이지)
    - `POST /community-posts` (작성 폼 제출)
    - `GET /community-posts/{postId}` (상세 페이지)
    - `GET /community-posts/{postId}/edit` (수정 페이지)
    - `POST /community-posts/{postId}` (수정 폼 제출)
    - `POST /community-posts/{postId}/delete` (삭제 폼 제출)

#### [x] Task 3-1-4: 게시글 목록 조회 및 검색
- [x] PostService 구현
    - `getPostList(PostCategory category, String keyword, Pageable pageable)`
- [x] PostController 구현
    - `GET /community-posts/list` (목록 페이지, 검색 포함)

---

### 3.2. 댓글 시스템 (Comment Domain)

#### [x] Task 3-2-1: Comment 엔티티 작성
- [x] 테스트 작성
    - 공구 또는 게시글 중 하나에만 연결
    - 대댓글 (parent) 관계 확인
    - Q&A vs 일반 댓글 구분
- [x] Comment 엔티티 구현
    - 필드: id, author(User FK), groupBuy(FK, Nullable), post(FK, Nullable), parent(FK, Nullable), content, type
    - 제약조건: CHECK (groupBuy XOR post)
    - 인덱스: groupBuyId, postId, parentId
- [x] CommentRepository 작성
    - `findByGroupBuyIdAndParentIsNullOrderByCreatedAtAsc(Long groupBuyId)`
    - `findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId)`
    - `findByParentIdOrderByCreatedAtAsc(Long parentId)`

#### [x] Task 3-2-2: 댓글 작성/수정/삭제 기능
- [x] 테스트 작성 (13개 테스트, 100% 통과)
    - 공구에 댓글 작성
    - 게시글에 댓글 작성
    - 대댓글 작성
    - 본인 댓글만 수정/삭제
    - 소프트 삭제 ("삭제된 댓글입니다")
    - Q&A 타입 댓글 (공구 전용)
    - 권한 검증 (작성자만 수정/삭제)
- [x] CommentService 구현
    - `createComment(Long userId, CreateCommentRequest dto)`
    - `updateComment(Long userId, Long commentId, UpdateCommentRequest dto)`
    - `deleteComment(Long userId, Long commentId)`
    - `getComments(EntityType targetType, Long targetId)`
- [x] CommentController 구현 (통합 엔드포인트 설계)
    - **통합 댓글 엔드포인트**: `POST /comments` (targetType, targetId로 공구/게시글 구분)
    - **수정**: `POST /comments/{commentId}/edit`
    - **삭제**: `POST /comments/{commentId}/delete`
    - **Fragment**: `GET /comments/fragments` (htmx용)
    - **장점**: RESTful, 확장 가능(Review 추가 가능), 코드 중복 없음
- [x] ValidationConfig 추가
    - 수동 DTO 검증을 위한 Validator 빈 설정

---

### 3.3. 후기 및 매너온도 시스템 (Review Domain)

#### [x] Task 3-3-1: Review 엔티티 작성
- [x] 테스트 작성
    - 중복 후기 방지 (reviewer + groupBuy 유니크)
    - 별점 1~5 범위 제한
    - 참여자만 후기 작성 가능
    - 완료된 공구만 후기 작성 가능
- [x] Review 엔티티 구현
    - 필드: id, reviewer(User FK), groupBuy(FK), rating, content
    - 제약조건: UNIQUE(reviewerId, groupBuyId), CHECK (rating BETWEEN 1 AND 5)
    - 인덱스: groupBuyId, reviewerId
- [x] ReviewRepository 작성
    - `findByGroupBuyIdOrderByCreatedAtDesc(Long groupBuyId)`
    - `existsByReviewerIdAndGroupBuyId(Long reviewerId, Long groupBuyId)`

#### [x] Task 3-3-2: 후기 작성 및 매너온도 반영
- [x] 테스트 작성 (ReviewServiceTest.java - 26개 테스트)
    - 후기 작성 시 주최자 매너온도 변동
        - 5점: +0.5, 4점: +0.3, 3점: 0, 2점: -1.0, 1점: -2.0
    - 중복 후기 방지
    - 참여 여부 확인
    - 실패 케이스: 사용자 없음, 공구 없음, 참여하지 않음, 중복 후기, CLOSED 상태 아님
- [x] ReviewService 구현
    - `createReview(Long userId, Long purchaseId, CreateReviewRequest dto)`
    - `updateReview(Long userId, Long reviewId, UpdateReviewRequest dto)`
    - `deleteReview(Long userId, Long reviewId)`
    - `getReviewsByGroupBuy(Long purchaseId)`
    - 참여 여부 확인 (Participation 조회)
    - 공구 상태 확인 (CLOSED)
    - 매너온도 업데이트 로직
- [x] DTOs 작성
    - CreateReviewRequest.java (rating, content)
    - UpdateReviewRequest.java (rating, content)
    - ReviewResponse.java (후기 정보 + 작성자 + 공구 + 타임스탬프)
- [x] UserService 확장
    - `updateMannerTemperature(Long userId, Double delta)` 메서드 추가

#### [x] Task 3-3-3: 후기 목록 조회
- [x] ReviewController 구현
    - **Form Endpoints**: `POST /group-purchases/{purchaseId}/reviews` (후기 작성 폼 제출)
        - `POST /group-purchases/{purchaseId}/reviews/{reviewId}/edit` (후기 수정)
        - `POST /group-purchases/{purchaseId}/reviews/{reviewId}/delete` (후기 삭제)
    - ReviewService의 `getReviewsByGroupBuy()` 메서드 사용

---

### 3.4. 알림 시스템 (Notification Domain)

#### [x] Task 3-4-1: Notification 엔티티 작성
- [x] 테스트 작성
    - 알림 생성 시 actor, relatedEntityId 저장
    - 읽지 않은 알림만 조회
- [x] Notification 엔티티 구현
    - 필드: id, user(FK), actor(FK), content, url, isRead, type, relatedEntityId, relatedEntityType
    - 인덱스: userId+isRead+createdAt, userId+createdAt
- [x] NotificationRepository 작성
    - `findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead)`
    - `countByUserIdAndIsReadFalse(Long userId)`

#### [x] Task 3-4-2: 알림 생성 로직
- [x] NotificationService 구현
    - `createNotification(Long userId, NotificationType type, Long actorId, Long relatedEntityId, EntityType entityType)`
    - 알림 템플릿 생성 ("OOO님이 참여했습니다")
    - 본인 행동은 알림 생성 안 함
- [x] 각 도메인 서비스에 알림 생성 로직 추가
    - 참여 시: JOIN_GROUP_BUY
    - 참여 취소 시: CANCEL_PARTICIPATION
    - 댓글 작성 시: COMMENT_GROUP_BUY, COMMENT_POST
    - 대댓글 작성 시: REPLY_COMMENT
    - 후기 작성 시: REVIEW_GROUP_BUY

#### [x] Task 3-4-3: 알림 조회/읽음 처리/삭제
- [x] UserController 구현
    - `GET /users/me/notifications` (알림 목록 페이지)
    - `POST /users/me/notifications/{notificationId}/read` (읽음 처리)
    - `POST /users/me/notifications/delete-all` (전체 삭제)
    - `GET /users/me/fragments/notifications` (htmx용 알림 목록 조각)

---

## ⚪ Phase 4: 최적화 및 배치 작업 (Week 6)

### 4.1. 배치 작업

#### [x] Task 4-1-1: 공구 상태 자동 변경 배치
- [x] @Scheduled 설정
- [x] 매일 자동 실행
    - 마감일 지난 공구 → CLOSED
    - D-1, D-2 공구 → IMMINENT
- [x] 마감 임박 알림 발송
    - 찜한 공구 마감 D-1 알림

#### [x] Task 4-1-2: 읽은 알림 자동 삭제
- [x] 30일 지난 읽은 알림 삭제 배치

---

### 4.2. 성능 최적화

#### [x] Task 4-2-1: 인덱스 최적화
- [x] 실제 쿼리 프로파일링
- [x] Slow Query 분석
- [x] 복합 인덱스 추가/조정
    - **Post 엔티티**: `deletedAt+createdAt`, `category+deletedAt+createdAt` 추가
    - **Comment 엔티티**: `authorId`, `groupBuyId+deletedAt+createdAt`, `postId+deletedAt+createdAt`, `parentId+deletedAt+createdAt` 추가
    - **GroupBuy 엔티티**: `deletedAt+createdAt`, `status+deletedAt+createdAt`, `category+deletedAt+createdAt` 추가

#### [x] Task 4-2-2: N+1 문제 해결
- [x] Fetch Join 적용
    - NotificationRepository: actor 관계에 LEFT JOIN FETCH 추가
- [x] Batch 쿼리 최적화
    - GroupBuyImageRepository: `findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder()` 추가 (IN 쿼리 사용)
    - UserService: `getMyGroupBuys()`, `getParticipatedGroupBuys()` 메서드에서 이미지 일괄 조회 패턴 적용
    - GroupBuyService: `getGroupBuyList()` 메서드에서 이미지 일괄 조회 패턴 적용
- [x] Hibernate Batch Size 설정 (application.yml)
    - `hibernate.default_batch_fetch_size: 100`
    - `hibernate.order_inserts: true`
    - `hibernate.order_updates: true`
    - `hibernate.batch_versioned_data: true`

#### [x] Task 4-2-3: 캐싱 전략 구현
- [x] CacheConfig 설정 및 캐시 정의 (RECIPES_CACHE, POPULAR_GROUP_BUYS_CACHE, VIEW_COUNTS_CACHE)
- [x] RecipeService 캐싱 구현 (검색, 상세, 랜덤, 카테고리)
- [x] GroupBuyService 인기 공구 목록 캐싱
- [x] PostService 게시글 목록 캐싱 및 캐시 무효화
- [x] Spring AOP 프록시 이슈 해결 (@CacheEvict 직접 적용)
- [x] 캐시 테스트 작성 (PostServiceCacheTest, GroupBuyServiceCacheTest, RecipeServiceCacheTest)
- [x] Redis 조건부 설정 (dev/prod: Redis, test: Simple cache)
- [x] 모든 테스트 통과 확인

---

### 4.3. 프론트엔드 개발

#### [x] Task 4-3-1: Thymeleaf 템플릿 작성
- [x] 레이아웃 템플릿 (header, footer, nav)
- [x] 홈 화면
- [x] 레시피 목록/상세
- [x] 공구 목록/상세/작성/수정
- [x] 커뮤니티 목록/상세/작성
- [x] 마이페이지 (모든 탭)
- [x] 로그인/회원가입

#### [x] Task 4-3-2: Bootstrap 스타일링
- [x] Bootstrap 5.3.2 + Bootstrap Icons 1.11.1 통합
- [x] 모든 템플릿 파일 Bootstrap 5 CDN 통일
- [x] 외부 CSS 파일 (/css/styles.css) 중앙 집중화
- [x] 인라인 스타일 100% 제거 (13개 파일)
- [x] 반응형 레이아웃 구현
- [x] 카드 컴포넌트 (공구, 레시피, 커뮤니티)
- [x] 폼 스타일링 (로그인, 회원가입, 게시글/공구 작성)
- [x] 버튼/모달/알림 스타일
- [x] 통일된 디자인 시스템 구축 (Color Scheme, Component Pattern)
- [x] 프로필 이미지, 플레이스홀더, 아이콘 스타일
- [x] Floating 버튼, Empty 상태 처리
- [x] 페이지네이션, Progress Bar 스타일
- [x] 검색 결과 하이라이팅, Tab 네비게이션

---

### 4.4. 배포 준비

#### [x] Task 4-4-1: Docker 설정
- [x] Dockerfile 작성 (Multi-stage build, Gradle 8.5 + JDK21 → JRE 21)
- [x] docker-compose.yml 작성 (PostgreSQL 16 + Redis 7 + Spring Boot + Nginx)
- [x] Nginx 리버스 프록시 설정 (nginx.conf + recipemate.conf)
- [x] .dockerignore 작성
- [x] .env.example 템플릿 작성
- [x] init-db.sql 초기화 스크립트 작성
- [x] DOCKER.md 문서 작성 (250+ lines)
- [x] .gitignore 업데이트 (.env, SSL certs 제외)

#### [x] Task 4-4-2: 프로덕션 설정
- [x] application-prod.yml 이미 설정됨 (application.yml 내 prod 프로파일)
- [x] PostgreSQL DDL 스크립트 작성 (schema.sql)
    - 13개 테이블 (users, persistent_logins, group_buys, group_buy_images, participations, posts, post_images, comments, reviews, wishlists, notifications, badges, point_histories)
    - 13개 PostgreSQL ENUM 타입
    - 60+ 인덱스 (단일/복합)
    - 외래키, CHECK, UNIQUE 제약조건
    - 6개 트리거 (updated_at 자동 갱신)
    - 스키마 버전 관리 테이블
    - 기본 관리자 계정 생성
- [x] 환경변수 설정 (.env.example 템플릿 제공)

---

### 4.5. 확장 기능 (4순위 - 선택적, 시간 여유 시)

#### [x] Task 4-5-1: 통합 검색 기능 구현
- [x] 테스트 작성
    - 공동구매, 커뮤니티 게시글, 레시피 동시 검색
    - 검색 결과 타입별 그룹화
    - SearchServiceTest.java (5개 테스트 - 모든 도메인 검색, 개별 도메인 검색, 빈 결과, 페이징)
    - SearchControllerTest.java (10개 테스트 - 통합 검색 페이지, htmx 프래그먼트, 유효성 검증)
- [x] SearchService 구현
    - 각 도메인 Repository에서 키워드 검색
    - 검색 결과를 통합 DTO로 조합
    - `unifiedSearch(String query, Pageable pageable)` 메서드
- [x] SearchController 구현
    - `GET /search?query={keyword}` (통합 검색 결과 페이지)
    - `GET /search/fragments?query={keyword}&type={type}` (htmx용 검색 결과 조각)
- [x] DTOs 작성
    - SearchResultResponse.java (id, title, content, entityType, createdAt)
    - UnifiedSearchResponse.java (query, groupBuys, posts, recipes, totalResults)
- [x] Thymeleaf 템플릿
    - templates/search/results.html (통합 검색 결과 페이지)
    - templates/search/fragments.html (groupBuyResults, postResults, recipeResults 프래그먼트)

#### [x] Task 4-5-2: 뱃지 시스템
- [x] Badge 엔티티 작성
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
- [x] BadgeService 구현
    - `checkAndAwardBadge(Long userId, BadgeType badgeType)`
    - 조건 체크 로직
        - FIRST_GROUP_BUY: 첫 공구 생성 시
        - TEN_PARTICIPATIONS: 참여 10회 달성 시
        - REVIEWER: 후기 5개 작성 시
        - POPULAR_HOST: 매너온도 40도 이상 달성 시
    - 각 도메인 서비스에 뱃지 획득 로직 추가
- [x] 마이페이지에 뱃지 목록 표시
    - UserController에 엔드포인트 추가
    - `GET /users/me/badges`

#### [x] Task 4-5-3: 포인트 시스템
- [x] User 엔티티 확장
    - 테스트 작성
        - 포인트 적립/차감 로직 확인
        - 포인트 잔액 0 미만 불가
    - User 엔티티에 points 필드 추가 (기본값: 0)
- [x] PointHistory 엔티티 작성
    - 필드: id, user(FK), amount, description, type (EARN, USE), createdAt
    - 인덱스: userId+createdAt
    - PointHistoryRepository 작성
- [x] PointService 구현
    - `earnPoints(Long userId, Integer amount, String description)`
    - `usePoints(Long userId, Integer amount, String description)`
    - `getPointHistory(Long userId, Pageable pageable)`
    - 포인트 적립 규칙
        - 공구 생성: +50
        - 공구 참여: +10
        - 후기 작성: +20
        - 출석 체크: +5 (선택)
- [x] 각 도메인 서비스에 포인트 적립 로직 추가
- [x] 마이페이지에 포인트 내역 표시
    - `GET /users/me/points`
    - `GET /users/me/points/history`

#### [x] Task 4-5-4: 이미지 최적화
- [x] Thumbnailator 라이브러리 추가
    - build.gradle 의존성 추가 (net.coobird:thumbnailator:0.4.19)
- [x] 테스트 작성
    - 원본 이미지 → 썸네일 생성 (createThumbnail)
    - 이미지 최적화 (리사이즈 + 압축)
    - JPEG/PNG 형식 지원
    - 파일 크기 감소 및 유효성 검증
    - ImageOptimizationServiceTest.java (10개 테스트)
    - ImageUploadIntegrationTest.java (8개 통합 테스트)
- [x] ImageOptimizationService 구현
    - `optimizeImage(byte[] imageData, String fileName)` - 이미지 최적화 (최대 1920x1920, 85% 품질)
    - `createThumbnail(byte[] imageData, int width, int height)` - 썸네일 생성 (비율 유지)
    - `convertToWebP(byte[] imageData)` - WebP 변환 (현재는 고품질 JPEG로 구현)
- [x] 기존 이미지 업로드 로직에 통합
    - ImageUploadUtil에 ImageOptimizationService 주입
    - 업로드 시 자동으로 이미지 최적화 적용
    - GroupBuyImage, PostImage 업로드 시 자동 최적화
    - 최적화 전후 크기 로깅
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
