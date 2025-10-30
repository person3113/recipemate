# RecipeMate Backlog

> **목적**: 현재 구현하지 않은 기능, 기술 부채, 개선 사항을 기록  
> **관리 방식**: 우선순위별로 분류 후 적절한 시점에 처리  

---

## 🔴 HIGH Priority (다음 Phase 전에 처리 권장)

### 1. Controller 아키텍처 변경 (Thymeleaf 통합)

**현재 상황**:
- 모든 Controller가 `@RestController`로 구현되어 JSON만 반환
- Thymeleaf 템플릿이 존재하지 않아 브라우저에서 직접 접근 불가
- 기술 스택에 명시된 Thymeleaf + htmx가 실제로 사용되지 않음

**문제점**:
```java
@RestController  // ← JSON만 반환
public class AuthController {
    @PostMapping("/auth/login")
    public ApiResponse<UserResponse> login(...) {
        return ApiResponse.success(userResponse);  // JSON
    }
}
```
- `/auth/login`을 브라우저에서 호출하면 JSON만 보임
- 실제 로그인 페이지가 없음

**해결 방안**:

#### 옵션 B: 이원화 (페이지용 Controller + API용 RestController)
```java
// 페이지 렌더링
@Controller
@RequestMapping("/pages")
public class PageController {
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
}

// API (htmx/AJAX용)
@RestController
@RequestMapping("/api")
public class AuthApiController {
    @PostMapping("/auth/login")
    public ApiResponse<UserResponse> login(...) {
        return ApiResponse.success(userResponse);
    }
}
```

**작업 범위**:
- [ ] User 도메인 Controller 수정 (AuthController, UserController)
- [ ] Thymeleaf 템플릿 작성 (resources/templates/auth/, resources/templates/user/)
- [ ] htmx 통합 (CDN 추가, 기본 사용 패턴 정립)
- [ ] 테스트 수정 (MockMvc 테스트 - HTML 응답 검증)

**예상 소요 시간**: 4-6시간

**처리 시점**: 
- GroupBuy 백엔드 완성 후 → 전체 화면 한번에 통합 (권장)
- 또는 User 도메인만 먼저 통합 후 → 다른 도메인 개발 시 참고

---

## 🟡 MEDIUM Priority (복잡도 증가 시 처리)

### 2. Validator 패턴 도입

**현재 상황**:
- 검증 로직이 Service 클래스에 직접 구현되어 있음
- 간단한 중복 체크 수준이라 문제없음

```java
// UserService.java
public UserResponse signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }
    if (userRepository.existsByNickname(request.getNickname())) {
        throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }
    // ...
}
```

**개선 방안**:
```java
// UserValidator.java (새로 생성)
@Component
public class UserValidator {
    private final UserRepository userRepository;
    
    public void validateSignup(SignupRequest request) {
        validateEmailUniqueness(request.getEmail());
        validateNicknameUniqueness(request.getNickname());
    }
    
    public void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
    
    public void validateNicknameUniqueness(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }
    
    public void validateProfileUpdate(Long userId, UpdateProfileRequest request) {
        // 닉네임 변경 시 중복 체크 (자기 자신 제외)
        if (request.getNickname() != null) {
            userRepository.findByNickname(request.getNickname())
                .ifPresent(user -> {
                    if (!user.getId().equals(userId)) {
                        throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
                    }
                });
        }
    }
}

// UserService.java (간결해짐)
public UserResponse signup(SignupRequest request) {
    userValidator.validateSignup(request);  // 위임
    // ... 회원가입 로직
}
```

**장점**:
- Service 클래스가 간결해짐 (비즈니스 로직에 집중)
- 검증 로직 재사용 가능
- 테스트 작성 용이 (Validator 단독 테스트 가능)

**처리 시점**:
- GroupBuy 도메인 개발 중 검증 로직이 복잡해질 때
- 특히 권한 체크(PermissionChecker)가 필요한 시점

---

### 3. PermissionChecker 패턴 도입

**예상 필요성**:
- GroupBuy 도메인에서 권한 체크가 많이 필요할 것으로 예상
  - 공동구매 수정/삭제: 주최자만 가능
  - 참가 취소: 참가자 본인만 가능
  - 리뷰 작성: 참가 완료자만 가능

**구현 예시**:
```java
@Component
public class GroupBuyPermissionChecker {
    
    public void checkHostPermission(User user, GroupBuy groupBuy) {
        if (!groupBuy.isHost(user)) {
            throw new CustomException(ErrorCode.FORBIDDEN_NOT_HOST);
        }
    }
    
    public void checkParticipantPermission(User user, GroupBuy groupBuy) {
        if (!groupBuy.isParticipant(user)) {
            throw new CustomException(ErrorCode.FORBIDDEN_NOT_PARTICIPANT);
        }
    }
    
    public void checkCanParticipate(User user, GroupBuy groupBuy) {
        if (groupBuy.isFull()) {
            throw new CustomException(ErrorCode.GROUP_BUY_FULL);
        }
        if (groupBuy.isClosed()) {
            throw new CustomException(ErrorCode.GROUP_BUY_CLOSED);
        }
        if (groupBuy.isParticipant(user)) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPATING);
        }
    }
}
```

**처리 시점**: GroupBuy Service 구현 중 권한 체크가 반복될 때

---

## 🟢 LOW Priority (선택적 기능)

### 4. Remember-Me 기능 완전 구현

**현재 상황**:
- 불완전한 Remember-Me 설정이 제거됨
- 기본 세션 인증만 동작 중

**제거된 코드**:
```java
// SecurityConfig (제거됨)
.rememberMe(remember -> remember
    .rememberMeParameter("rememberMe")
    .tokenValiditySeconds(604800)  // 7일
)

// LoginRequest (제거됨)
private Boolean rememberMe;
```

**완전 구현 시 필요한 작업**:
```java
// 1. PersistentTokenRepository 설정 (DB 기반)
@Bean
public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(dataSource);
    return tokenRepository;
}

// 2. SecurityConfig 수정
.rememberMe(remember -> remember
    .key("uniqueAndSecretKey")
    .tokenValiditySeconds(604800)  // 7일
    .tokenRepository(persistentTokenRepository(dataSource))
    .userDetailsService(customUserDetailsService)
)

// 3. DB 테이블 생성 (persistent_logins)
CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

// 4. LoginRequest에 rememberMe 필드 추가
private Boolean rememberMe = false;
```

**장점**: 사용자가 로그인 상태 유지 가능 (7일)  
**단점**: 보안 설정 복잡, 토큰 관리 필요  
**처리 시점**: 프로덕션 배포 전 또는 사용자 요청 시

---

### 5. DTO Validation 강화

**현재 상황**:
- 기본 Validation 어노테이션만 사용 중 (@NotBlank, @Email, @Pattern)

**개선 가능 사항**:
```java
// 커스텀 Validation 어노테이션
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "이미 존재하는 이메일입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 사용
public class SignupRequest {
    @UniqueEmail  // ← DTO 레벨에서 검증
    private String email;
}
```

**장점**: 검증 로직이 DTO에 응집  
**단점**: 구현 복잡도 증가  
**처리 시점**: 검증 로직이 복잡해지고 재사용이 많을 때

---

### 6. QueryDSL 활용

**현재 상황**:
- QueryDSL 의존성은 추가되어 있으나 사용하지 않음
- 단순 쿼리는 JpaRepository 메서드로 충분

**활용 시점**:
- 복잡한 동적 쿼리 필요 시 (예: 공동구매 검색 - 카테고리, 상태, 키워드 등)
- Pagination + 정렬 조합이 복잡할 때

**예시**:
```java
// GroupBuyRepositoryCustom.java
public interface GroupBuyRepositoryCustom {
    Page<GroupBuy> searchGroupBuys(GroupBuySearchCondition condition, Pageable pageable);
}

// GroupBuyRepositoryImpl.java
public Page<GroupBuy> searchGroupBuys(GroupBuySearchCondition condition, Pageable pageable) {
    QGroupBuy groupBuy = QGroupBuy.groupBuy;
    
    BooleanBuilder builder = new BooleanBuilder();
    if (condition.getCategory() != null) {
        builder.and(groupBuy.category.eq(condition.getCategory()));
    }
    if (condition.getStatus() != null) {
        builder.and(groupBuy.status.eq(condition.getStatus()));
    }
    // ... 동적 조건 추가
    
    return queryFactory
        .selectFrom(groupBuy)
        .where(builder)
        .orderBy(groupBuy.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
}
```

**처리 시점**: 검색 기능 구현 시 (Phase 2)

---

## 📊 우선순위 요약

| 항목 | 우선순위 | 처리 시점 | 예상 시간 |
|------|----------|-----------|-----------|
| Controller Thymeleaf 통합 | 🔴 HIGH | GroupBuy 백엔드 완성 후 | 4-6시간 |
| Validator 패턴 도입 | 🟡 MEDIUM | GroupBuy 개발 중 복잡도 증가 시 | 2-3시간 |
| PermissionChecker 도입 | 🟡 MEDIUM | GroupBuy 권한 체크 반복 시 | 1-2시간 |
| Remember-Me 구현 | 🟢 LOW | 프로덕션 배포 전 | 2-3시간 |
| Custom Validation | 🟢 LOW | 검증 로직 복잡 시 | 3-4시간 |
| QueryDSL 활용 | 🟢 LOW | 검색 기능 구현 시 | 2-3시간 |

---

## 🎯 권장 처리 순서

1. **현재**: GroupBuy 도메인 백엔드 개발 (Entity, Service, RestController)
2. **Phase 1 완료 후**: Controller → Thymeleaf 통합 (모든 도메인 화면 구현)
3. **Phase 2**: 필요 시 Validator/PermissionChecker 리팩터링
4. **Phase 3**: QueryDSL 검색 기능, Remember-Me 등 부가 기능

---

## 📝 메모

- **TDD 원칙 유지**: 백로그 항목도 테스트 먼저 작성
- **YAGNI 원칙**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새가 느껴질 때 백로그 항목 처리
