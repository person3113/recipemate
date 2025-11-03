package com.recipemate.domain.notification.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id_is_read_created_at", columnList = "user_id, is_read, created_at"),
        @Index(name = "idx_user_id_created_at", columnList = "user_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(length = 255)
    private String url;

    @Column(nullable = false)
    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column
    private Long relatedEntityId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EntityType relatedEntityType;

    public static Notification create(
            User user,
            User actor,
            String content,
            NotificationType type,
            String url,
            Long relatedEntityId,
            EntityType relatedEntityType
    ) {
        return Notification.builder()
                .user(user)
                .actor(actor)
                .content(content)
                .type(type)
                .url(url)
                .isRead(false)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
