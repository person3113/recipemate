package com.recipemate.domain.user.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyPostDto {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private Integer viewCount;
    private Long commentCount;
    private Long likeCount;
    private LocalDateTime deletedAt;

    // Constructor for queries that include deletedAt (for "My Activity" page)
    public MyPostDto(Long id, String title, LocalDateTime createdAt, Integer viewCount,
                     Long commentCount, Long likeCount, LocalDateTime deletedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.deletedAt = deletedAt;
    }

    // Constructor for queries that exclude deletedAt (for backward compatibility)
    public MyPostDto(Long id, String title, LocalDateTime createdAt, Integer viewCount,
                     Long commentCount, Long likeCount) {
        this(id, title, createdAt, viewCount, commentCount, likeCount, null);
    }
}
