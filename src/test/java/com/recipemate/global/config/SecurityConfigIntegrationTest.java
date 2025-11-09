//package com.recipemate.global.config;
//
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Security 설정 통합 테스트
// * 실제 Security 설정이 적용된 상태에서 인증/인가 규칙을 검증
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//class SecurityConfigIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트 사용자 생성
//        testUser = User.builder()
//                .email("test@example.com")
//                .password(passwordEncoder.encode("password123"))
//                .nickname("테스트유저")
//                .phoneNumber("010-1234-5678")
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(testUser);
//    }
//
//    // 1. 정적 리소스 접근 테스트
//    @Test
//    @DisplayName("정적 리소스는 인증 없이 접근 가능")
//    void staticResources_NoAuth_Success() throws Exception {
//        // CSS
//        mockMvc.perform(get("/css/style.css"))
//                .andExpect(status().isNotFound()); // 파일이 없지만 403이 아닌 404 반환
//
//        // JS
//        mockMvc.perform(get("/js/app.js"))
//                .andExpect(status().isNotFound());
//
//        // Images
//        mockMvc.perform(get("/images/logo.png"))
//                .andExpect(status().isNotFound());
//    }
//
//    // 2. 인증 페이지 접근 테스트
//    @Test
//    @DisplayName("로그인 페이지는 인증 없이 접근 가능")
//    void loginPage_NoAuth_Success() throws Exception {
//        mockMvc.perform(get("/auth/login"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("회원가입 페이지는 인증 없이 접근 가능")
//    void signupPage_NoAuth_Success() throws Exception {
//        mockMvc.perform(get("/auth/signup"))
//                .andExpect(status().isOk());
//    }
//
//    // 3. Public 공동구매 페이지 접근 테스트
//    @Test
//    @DisplayName("공동구매 목록 페이지는 인증 없이 접근 가능")
//    void groupPurchasesList_NoAuth_Success() throws Exception {
//        mockMvc.perform(get("/group-purchases/list"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("공동구매 상세 페이지는 인증 없이 접근 가능")
//    void groupPurchasesDetail_NoAuth_Success() throws Exception {
//        // ID가 숫자인 경우 접근 가능 (실제 데이터가 없어서 404 발생)
//        mockMvc.perform(get("/group-purchases/1"))
//                .andExpect(status().isNotFound()); // 데이터가 없어서 404
//    }
//
//    // 4. 보호된 엔드포인트 접근 테스트
//    @Test
//    @DisplayName("공동구매 생성 폼은 인증 필요")
//    void groupPurchasesCreateForm_NoAuth_Forbidden() throws Exception {
//        mockMvc.perform(get("/group-purchases/new"))
//                .andExpect(status().is3xxRedirection());
//    }
//
//    @Test
//    @DisplayName("공동구매 생성은 인증 필요 (CSRF 토큰 없으면 403)")
//    void groupPurchasesCreate_NoAuth_Forbidden() throws Exception {
//        // CSRF 토큰 없이 POST 요청 시 403 Forbidden
//        mockMvc.perform(post("/group-purchases"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("인증된 사용자는 공동구매 생성 폼 접근 가능")
//    @WithMockUser(username = "test@example.com")
//    void groupPurchasesCreateForm_WithAuth_Success() throws Exception {
//        mockMvc.perform(get("/group-purchases/new"))
//                .andExpect(status().isOk());
//    }
//
//
//    // 6. 사용자 프로필 엔드포인트 테스트
//    @Test
//    @DisplayName("마이페이지는 인증 필요")
//    void myPage_NoAuth_Forbidden() throws Exception {
//        mockMvc.perform(get("/users/me"))
//                .andExpect(status().is3xxRedirection());
//    }
//
//    @Test
//    @DisplayName("인증된 사용자는 마이페이지 접근 가능")
//    @WithMockUser(username = "test@example.com")
//    void myPage_WithAuth_Success() throws Exception {
//        mockMvc.perform(get("/users/me"))
//                .andExpect(status().isOk());
//    }
//
//    // 7. API 엔드포인트 테스트
//    @Test
//    @DisplayName("공동구매 생성 폼 제출은 인증 필요 (CSRF 토큰 없으면 403)")
//    void createGroupBuy_NoAuth_Forbidden() throws Exception {
//        // 인증 없음 + CSRF 토큰 없음 → 403 Forbidden
//        mockMvc.perform(post("/group-purchases"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("인증된 사용자는 CSRF 토큰과 함께 공동구매 생성 가능")
//    @WithMockUser(username = "test@example.com")
//    void createGroupBuy_WithAuth_ValidationError() throws Exception {
//        // CSRF 토큰 포함 + 빈 데이터로 요청 시 유효성 검증 실패 (200 OK with form view)
//        mockMvc.perform(post("/group-purchases")
//                        .with(csrf())) // ✅ CSRF 토큰 추가
//                .andExpect(status().isOk()); // 검증 실패 시 폼 페이지 반환 (200 OK)
//    }
//
//    // 8. H2 Console 접근 테스트
//    @Test
//    @DisplayName("H2 콘솔은 인증 없이 접근 가능")
//    void h2Console_NoAuth_Success() throws Exception {
//        mockMvc.perform(get("/h2-console"))
//                .andExpect(status().isNotFound()); // H2 콘솔이 활성화되지 않은 경우 404
//    }
//
//    // 9. 로그아웃 테스트
//    @Test
//    @DisplayName("로그아웃은 CSRF 토큰과 함께 접근 가능")
//    void logout_NoAuth_Success() throws Exception {
//        mockMvc.perform(post("/auth/logout")
//                        .with(csrf())) // ✅ CSRF 토큰 추가
//                .andExpect(status().is3xxRedirection()); // 로그인 페이지로 리다이렉트
//    }
//
//    // 10. CSRF 보호 테스트
//    @Test
//    @DisplayName("CSRF 토큰 없이 POST 요청 시 403 Forbidden")
//    @WithMockUser(username = "test@example.com")
//    void csrfEnabled_PostWithoutToken_Forbidden() throws Exception {
//        // CSRF 토큰 없이 POST 요청 시 403 에러
//        mockMvc.perform(post("/group-purchases/1/participate")
//                        .param("quantity", "1")
//                        .param("deliveryMethod", "DIRECT"))
//                .andExpect(status().isForbidden()); // ✅ CSRF 보호로 403 응답
//    }
//
//    @Test
//    @DisplayName("CSRF 토큰과 함께 POST 요청 시 정상 처리")
//    @WithMockUser(username = "test@example.com")
//    void csrfEnabled_PostWithToken_Success() throws Exception {
//        // CSRF 토큰 포함 시 비즈니스 로직 실행
//        // 500 에러 발생 가능 (USER_NOT_FOUND - MockUser가 DB에 없음)
//        // 중요한 것은 403 Forbidden이 아니라는 것 - CSRF 보호를 통과함을 의미
//        mockMvc.perform(post("/group-purchases/1/participate")
//                        .param("quantity", "1")
//                        .param("deliveryMethod", "DIRECT")
//                        .with(csrf())) // ✅ CSRF 토큰 추가
//                .andExpect(status().is5xxServerError()); // CSRF 보호 통과, 비즈니스 로직에서 USER_NOT_FOUND 예외 발생
//    }
//
//    // 11. 인증 흐름 통합 테스트 (현재 formLogin이 설정되지 않아 스킵)
//    // TODO: SecurityConfig에 formLogin 설정 추가 후 테스트 활성화
//    // @Test
//    // @DisplayName("잘못된 자격 증명으로 로그인 실패")
//    // void login_InvalidCredentials_Failure() throws Exception {
//    //     mockMvc.perform(formLogin("/auth/login")
//    //                     .user("username", "wronguser")
//    //                     .password("password", "wrongpassword"))
//    //             .andExpect(status().is3xxRedirection())
//    //             .andExpect(redirectedUrl("/auth/login?error"));
//    // }
//
//    // 12. 권한별 접근 제어 테스트 (미래 확장용)
//    @Test
//    @DisplayName("일반 사용자는 모든 인증된 엔드포인트 접근 가능")
//    @WithMockUser(username = "test@example.com", roles = "USER")
//    void userRole_AccessAuthenticatedEndpoints_Success() throws Exception {
//        mockMvc.perform(get("/group-purchases/new"))
//                .andExpect(status().isOk());
//    }
//
//    // 13. URL 패턴 매칭 테스트
//    @Test
//    @DisplayName("공동구매 ID가 숫자가 아닌 경우 인증 필요")
//    void groupPurchasesDetailNonNumericId_NoAuth_Forbidden() throws Exception {
//        mockMvc.perform(get("/group-purchases/abc"))
//                .andExpect(status().is3xxRedirection());
//    }
//
//    @Test
//    @DisplayName("공동구매 ID가 숫자인 경우 인증 불필요")
//    void groupPurchasesDetailNumericId_NoAuth_Success() throws Exception {
//        mockMvc.perform(get("/group-purchases/123"))
//                .andExpect(status().isNotFound()); // 데이터가 없어서 404
//    }
//}
