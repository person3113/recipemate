package com.recipemate.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyLikedPostDto {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private Integer viewCount;
    private Long commentCount;
    private Long likeCount;
}
