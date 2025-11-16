package com.recipemate.domain.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 제안 처리 요청
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionProcessRequest {

    @NotBlank(message = "처리 사유를 입력해주세요.")
    private String reason;
}
