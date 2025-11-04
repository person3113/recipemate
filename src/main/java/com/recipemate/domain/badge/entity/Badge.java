package com.recipemate.domain.badge.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BadgeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "badges",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_badge_type", columnNames = {"user_id", "badge_type"})
        },
        indexes = {
                @Index(name = "idx_user_id_acquired_at", columnList = "user_id, acquired_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_badge_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 50)
    private BadgeType badgeType;

    @Column(nullable = false)
    private LocalDateTime acquiredAt;

    public static Badge create(User user, BadgeType badgeType) {
        return new Badge(
                null,
                user,
                badgeType,
                LocalDateTime.now()
        );
    }
}
