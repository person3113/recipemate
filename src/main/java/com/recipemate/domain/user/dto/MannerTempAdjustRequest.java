package com.recipemate.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 매너온도 조정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MannerTempAdjustRequest {
    
    @NotNull(message = "변경값을 입력해주세요.")
    private Double changeValue;
    
    @NotBlank(message = "조정 사유를 입력해주세요.")
    private String reason;
}
