package com.recipemate.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UpdateReviewRequest {

    @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다")
    private Integer rating;

    @Size(max = 500, message = "후기 내용은 500자 이내여야 합니다")
    private String content;
}
