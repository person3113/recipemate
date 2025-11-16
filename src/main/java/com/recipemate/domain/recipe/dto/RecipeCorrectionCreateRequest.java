package com.recipemate.domain.recipe.dto;

import com.recipemate.domain.recipe.entity.CorrectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 레시피 개선 제안 생성 요청
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCorrectionCreateRequest {

    @NotNull(message = "제안 종류를 선택해주세요.")
    private CorrectionType correctionType;

    @NotBlank(message = "제안 내용을 입력해주세요.")
    private String proposedChange;
}
