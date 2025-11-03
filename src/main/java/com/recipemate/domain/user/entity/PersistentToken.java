package com.recipemate.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Remember-Me 토큰 저장용 엔티티
 * Spring Security의 PersistentRememberMeToken을 JPA로 관리
 */
@Entity
@Table(name = "persistent_logins", indexes = {
        @Index(name = "idx_persistent_logins_username", columnList = "username"),
        @Index(name = "idx_persistent_logins_series", columnList = "series", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersistentToken {

    @Id
    @Column(length = 64)
    private String series;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(nullable = false)
    private LocalDateTime lastUsed;

    public PersistentToken(String series, String username, String token, LocalDateTime lastUsed) {
        this.series = series;
        this.username = username;
        this.token = token;
        this.lastUsed = lastUsed;
    }

    public void updateToken(String token, LocalDateTime lastUsed) {
        this.token = token;
        this.lastUsed = lastUsed;
    }
}
