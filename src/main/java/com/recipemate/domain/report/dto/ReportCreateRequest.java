package com.recipemate.domain.report.dto;

import com.recipemate.domain.report.entity.ReportType;
import com.recipemate.domain.report.entity.ReportedEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 신고 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "신고 대상 타입은 필수입니다.")
    private ReportedEntityType reportedEntityType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long reportedEntityId;

    @NotNull(message = "신고 사유는 필수입니다.")
    private ReportType reportType;

    @NotBlank(message = "신고 내용은 필수입니다.")
    private String content;
}
