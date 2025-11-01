package com.recipemate.domain.comment.dto;

import com.recipemate.global.common.CommentType;
import com.recipemate.global.common.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "대상 타입은 필수입니다 (GROUP_BUY 또는 POST)")
    private EntityType targetType;

    @NotNull(message = "대상 ID는 필수입니다")
    private Long targetId;

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다")
    private String content;

    @NotNull(message = "댓글 타입은 필수입니다")
    private CommentType type;

    // 대댓글인 경우에만 사용
    private Long parentId;
}
