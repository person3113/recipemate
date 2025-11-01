package com.recipemate.domain.comment.dto;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.global.common.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long authorId;
    private String authorNickname;
    private String content;
    private CommentType type;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    
    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorId(comment.getAuthor().getId())
                .authorNickname(comment.getAuthor().getNickname())
                .content(comment.getContent())
                .type(comment.getType())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getDeletedAt() != null)
                .replies(new ArrayList<>())
                .build();
    }

    public static CommentResponse fromWithReplies(Comment comment, List<Comment> replies) {
        CommentResponse response = from(comment);
        response.replies = replies.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
        return response;
    }
}
