package com.recipemate.domain.groupbuy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 레시피 기반 공구 생성 시 사용자가 선택한 재료 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SelectedIngredient {

    @NotBlank(message = "재료명은 필수입니다")
    @Size(max = 100, message = "재료명은 100자 이내여야 합니다")
    private String name;

    @Size(max = 100, message = "분량은 100자 이내여야 합니다")
    private String measure;

    @Builder.Default
    private Boolean selected = true;
}
