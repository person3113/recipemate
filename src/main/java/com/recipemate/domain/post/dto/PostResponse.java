package com.recipemate.domain.post.dto;

import com.recipemate.domain.post.entity.Post;
import com.recipemate.global.common.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private PostCategory category;
    private Integer viewCount;
    private Long authorId;
    private String authorNickname;
    private String authorEmail;  // 작성자 권한 체크용
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int likeCount;
    private int commentCount;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .authorEmail(post.getAuthor().getEmail())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(post.getLikes() != null ? post.getLikes().size() : 0)
                .commentCount(post.getComments() != null ? post.getComments().size() : 0)
                .build();
    }
}

