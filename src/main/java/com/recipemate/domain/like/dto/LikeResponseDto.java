package com.recipemate.domain.like.dto;

public record LikeResponseDto(
    boolean isLiked,
    long likeCount
) {
}
