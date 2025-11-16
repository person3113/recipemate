package com.recipemate.domain.recipe.dto;

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

    private String reason;
}
