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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
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
                        
                        // Public group purchase pages (read-only)
                        .requestMatchers(
                            "/group-purchases/list",              // List page (HTML)
                            "/group-purchases/{id:[0-9]+}"        // Detail page (HTML)
                        ).permitAll()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
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

