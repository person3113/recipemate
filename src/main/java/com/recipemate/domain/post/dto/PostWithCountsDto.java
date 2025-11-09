package com.recipemate.domain.post.dto;

import com.recipemate.domain.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostWithCountsDto {
    private Post post;
    private long likeCount;
    private long commentCount;
}
