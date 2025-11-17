package com.recipemate.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 신고 처리 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportProcessRequest {

    /**
     * 처리 액션 (PROCESS: 처리 완료, DISMISS: 기각)
     */
    @NotBlank(message = "처리 액션은 필수입니다.")
    private String action;

    /**
     * 관리자 메모 (선택사항)
     */
    private String adminNotes;
    
    // Service에서 사용하는 getNotes() 메서드 추가
    public String getNotes() {
        return adminNotes;
    }
}
