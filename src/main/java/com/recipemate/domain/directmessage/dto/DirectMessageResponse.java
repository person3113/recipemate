package com.recipemate.domain.directmessage.dto;

import com.recipemate.domain.directmessage.entity.DirectMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DirectMessageResponse {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private Long receiverId;
    private String receiverNickname;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static DirectMessageResponse from(DirectMessage message) {
        return DirectMessageResponse.builder()
            .id(message.getId())
            .senderId(message.getSender().getId())
            .senderNickname(message.getSender().getNickname())
            .receiverId(message.getReceiver().getId())
            .receiverNickname(message.getReceiver().getNickname())
            .content(message.getContent())
            .isRead(message.getIsRead())
            .createdAt(message.getCreatedAt())
            .build();
    }
}

