package com.recipemate.domain.user.entity;

import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_nickname", columnList = "nickname")
})
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$")
    @Column(nullable = false, length = 13)
    private String phoneNumber;

    @Column(length = 500)
    private String profileImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Double mannerTemperature = 36.5;

    @Builder.Default
    @Column(nullable = false)
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean commentNotification = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean groupPurchaseNotification = true;

    public static User create(String email, String encodedPassword, String nickname, String phoneNumber) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .mannerTemperature(36.5)
                .points(0)
                .role(UserRole.USER)
                .commentNotification(true)
                .groupPurchaseNotification(true)
                .build();
    }

    public static User createAdmin(String email, String encodedPassword, String nickname, String phoneNumber) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .mannerTemperature(36.5)
                .points(0)
                .role(UserRole.ADMIN)
                .commentNotification(true)
                .groupPurchaseNotification(true)
                .build();
    }

    public void updateMannerTemperature(double delta) {
        this.mannerTemperature += delta;
    }

    public void updateProfile(String nickname, String phoneNumber, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (profileImageUrl != null) {
          this.profileImageUrl = profileImageUrl.isBlank() ? null : profileImageUrl;
        }
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void earnPoints(int amount) {
        if (amount > 0) {
            this.points += amount;
        }
    }

    public boolean usePoints(int amount) {
        if (amount <= 0 || this.points < amount) {
            return false;
        }
        this.points -= amount;
        return true;
    }

    public void updateNotificationSettings(Boolean commentNotification, Boolean groupPurchaseNotification) {
        this.commentNotification = commentNotification;
        this.groupPurchaseNotification = groupPurchaseNotification;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
