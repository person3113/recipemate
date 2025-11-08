package com.recipemate.domain.user.entity;

import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.PointType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "point_histories", indexes = {
        @Index(name = "idx_point_history_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_point_history_user_desc_created", columnList = "user_id,description,created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;

    public static PointHistory create(User user, int amount, String description, PointType type) {
        return new PointHistory(
                null,
                user,
                amount,
                description,
                type
        );
    }
}
