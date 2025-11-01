package com.recipemate.domain.comment.dto;

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
public class UpdateCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다")
    private String content;

    // 리다이렉트를 위한 필드
    @NotNull(message = "대상 타입은 필수입니다")
    private EntityType targetType;

    @NotNull(message = "대상 ID는 필수입니다")
    private Long targetId;
}
