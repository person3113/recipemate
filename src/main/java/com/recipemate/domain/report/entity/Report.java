package com.recipemate.domain.report.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 악성 콘텐츠/사용자 신고 엔티티
 */
@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_entity_type", columnList = "reported_entity_type"),
        @Index(name = "idx_report_reporter_id", columnList = "reporter_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 신고자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * 신고 대상 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reported_entity_type", nullable = false, length = 30)
    private ReportedEntityType reportedEntityType;

    /**
     * 신고 대상 ID
     */
    @Column(name = "reported_entity_id", nullable = false)
    private Long reportedEntityId;

    /**
     * 신고 사유 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    /**
     * 신고 상세 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    /**
     * 관리자 처리 메모
     */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    /**
     * 처리한 관리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private User resolver;

    /**
     * 신고 생성
     */
    public static Report create(
            User reporter,
            ReportedEntityType reportedEntityType,
            Long reportedEntityId,
            ReportType reportType,
            String content
    ) {
        return Report.builder()
                .reporter(reporter)
                .reportedEntityType(reportedEntityType)
                .reportedEntityId(reportedEntityId)
                .reportType(reportType)
                .content(content)
                .status(ReportStatus.PENDING)
                .build();
    }

    /**
     * 신고 처리 완료
     */
    public void process(User admin, String notes) {
        this.status = ReportStatus.PROCESSED;
        this.adminNotes = notes;
        this.resolver = admin;
    }

    /**
     * 신고 기각
     */
    public void dismiss(User admin, String notes) {
        this.status = ReportStatus.DISMISSED;
        this.adminNotes = notes;
        this.resolver = admin;
    }

    /**
     * 처리 대기 중인지 확인
     */
    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }

    /**
     * 처리 완료되었는지 확인
     */
    public boolean isProcessed() {
        return this.status == ReportStatus.PROCESSED;
    }

    /**
     * 기각되었는지 확인
     */
    public boolean isDismissed() {
        return this.status == ReportStatus.DISMISSED;
    }
}
