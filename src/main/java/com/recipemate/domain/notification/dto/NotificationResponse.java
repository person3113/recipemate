package com.recipemate.domain.notification.dto;

import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationResponse {

    private Long id;
    private String content;
    private String url;
    private Boolean isRead;
    private NotificationType type;
    private Long relatedEntityId;
    private EntityType relatedEntityType;
    private String actorNickname;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .url(notification.getUrl())
                .isRead(notification.getIsRead())
                .type(notification.getType())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .actorNickname(notification.getActor() != null ? notification.getActor().getNickname() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
