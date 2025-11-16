package com.recipemate.domain.user.entity;

import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "manner_temp_histories",
    indexes = {
        @Index(name = "idx_manner_temp_history_user_id", columnList = "user_id"),
        @Index(name = "idx_manner_temp_history_created_at", columnList = "created_at")
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MannerTempHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_manner_temp_history_user"))
    private User user;

    @Column(nullable = false)
    private Double changeValue;

    @Column(nullable = false)
    private Double previousTemperature;

    @Column(nullable = false)
    private Double newTemperature;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column
    private Long relatedReviewId;

    public static MannerTempHistory create(
            User user, 
            Double changeValue, 
            Double previousTemperature,
            Double newTemperature,
            String reason, 
            Long relatedReviewId
    ) {
        return MannerTempHistory.builder()
                .user(user)
                .changeValue(changeValue)
                .previousTemperature(previousTemperature)
                .newTemperature(newTemperature)
                .reason(reason)
                .relatedReviewId(relatedReviewId)
                .build();
    }
}
