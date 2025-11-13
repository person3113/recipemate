package com.recipemate.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PersistentTokenRepository persistentTokenRepository;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화 (세션 기반 폼 로그인 보호)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/h2-console/**",           // H2 콘솔
                                "/recipes/admin/**"         // Admin API endpoints
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Static resources
                        .requestMatchers(
                            "/css/**",            // Static CSS
                            "/js/**",             // Static JS
                            "/images/**",         // Static images
                            "/h2-console/**"      // H2 console
                        ).permitAll()
                        
                        // Auth pages and API endpoints
                        .requestMatchers(
                            "/auth/login",        // Login page
                            "/auth/signup",       // Signup page
                            "/auth/login/**",     // Login API
                            "/auth/signup/**",    // Signup API
                            "/auth/logout"        // Logout API
                        ).permitAll()
                        
                        // Public pages (anyone can view)
                        .requestMatchers(
                            "/",                                  // Home page
                            "/recipes",                           // Recipe search page
                            "/recipes/list",                      // Recipe list page
                            "/recipes/random",                    // Random recipe page
                            "/recipes/{recipeId}",                // Recipe detail pages (e.g., /recipes/123, /recipes/meal-52772)
                            "/recipes/{recipeId}/group-purchases", // Recipe related group-buys
                            "/group-purchases/list",              // Group purchase list
                            "/group-purchases/{id:[0-9]+}",       // Group purchase detail
                            "/community-posts/list",              // Community post list
                            "/community-posts/{id:[0-9]+}",       // Community post detail
                            "/reviews",                           // Review list page
                            "/reviews/fragments",                 // Review list fragments (HTMX)
                            "/search/**",                         // Search pages
                            "/comments/fragments",                // Comment list fragments (HTMX)
                            "/recipes/*/bookmarks/status",        // Recipe bookmark status check (returns false for non-auth)
                            "/group-purchases/*/bookmarks/status" // Group-buy bookmark status check (returns false for non-auth)
                        ).permitAll()
                        
                        // API endpoints that require authentication
                        .requestMatchers(
                            "/recipes/*/bookmarks",               // Recipe bookmark add
                            "/recipes/*/bookmarks/cancel",        // Recipe bookmark cancel
                            "/group-purchases/*/bookmarks",       // Group-buy bookmark add
                            "/group-purchases/*/bookmarks/cancel", // Group-buy bookmark cancel
                            "/recipes/new",                       // Recipe creation form
                            "/recipes/*/edit",                    // Recipe edit form
                            "/recipes/my"                         // My recipes list
                        ).authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("recipemate-remember-me-key")
                        .tokenRepository(persistentTokenRepository)
                        .userDetailsService(userDetailsService)
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일
                        .rememberMeParameter("rememberMe")
                        .rememberMeCookieName("recipemate-remember-me")
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}

