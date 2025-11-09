package com.recipemate.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyCommentDto {
    private Long postId;
    private String postTitle;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private LocalDateTime postDeletedAt;
    private boolean isReply;
}
