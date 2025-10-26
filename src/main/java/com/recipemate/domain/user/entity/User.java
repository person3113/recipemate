package com.recipemate.domain.user.entity;

import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_nickname", columnList = "nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(nullable = false)
    private Double mannerTemperature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    public static User create(String email, String encodedPassword, String nickname, String phoneNumber) {
        return new User(
                null,
                email,
                encodedPassword,
                nickname,
                phoneNumber,
                null,
                36.5,
                UserRole.USER
        );
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
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
