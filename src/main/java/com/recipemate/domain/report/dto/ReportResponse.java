package com.recipemate.domain.report.dto;

import com.recipemate.domain.report.entity.Report;
import com.recipemate.domain.report.entity.ReportStatus;
import com.recipemate.domain.report.entity.ReportType;
import com.recipemate.domain.report.entity.ReportedEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 신고 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class ReportResponse {

    private Long id;
    private String reporterNickname;
    private Long reporterId;
    private ReportedEntityType reportedEntityType;
    private Long reportedEntityId;
    private String reportedUserNickname; // 사용자 신고 시 피신고자 닉네임
    private ReportType reportType;
    private String content;
    private ReportStatus status;
    private String adminNotes;
    private String resolverNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterNickname(report.getReporter() != null ? report.getReporter().getNickname() : "삭제된 사용자")
                .reporterId(report.getReporter() != null ? report.getReporter().getId() : null)
                .reportedEntityType(report.getReportedEntityType())
                .reportedEntityId(report.getReportedEntityId())
                .reportedUserNickname(null) // ReportService에서 설정
                .reportType(report.getReportType())
                .content(report.getContent())
                .status(report.getStatus())
                .adminNotes(report.getAdminNotes())
                .resolverNickname(report.getResolver() != null ? report.getResolver().getNickname() : null)
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
