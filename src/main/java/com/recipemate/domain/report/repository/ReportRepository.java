package com.recipemate.domain.report.repository;

import com.recipemate.domain.report.entity.Report;
import com.recipemate.domain.report.entity.ReportStatus;
import com.recipemate.domain.report.entity.ReportedEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 특정 상태의 신고 목록 조회
     */
    @Query("SELECT r FROM Report r " +
            "LEFT JOIN FETCH r.reporter " +
            "LEFT JOIN FETCH r.resolver " +
            "WHERE r.status = :status " +
            "ORDER BY r.createdAt DESC")
    List<Report> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status);

    /**
     * 처리 대기 중인 신고 목록 조회 (페이징)
     */
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    /**
     * 특정 엔티티에 대한 신고 개수 조회
     */
    long countByReportedEntityTypeAndReportedEntityId(
            ReportedEntityType reportedEntityType,
            Long reportedEntityId
    );

    /**
     * 특정 사용자가 신고한 개수
     */
    long countByReporter_Id(Long reporterId);

    /**
     * 신고 상세 조회 (reporter, resolver JOIN FETCH)
     */
    @Query("SELECT r FROM Report r " +
            "LEFT JOIN FETCH r.reporter " +
            "LEFT JOIN FETCH r.resolver " +
            "WHERE r.id = :id")
    java.util.Optional<Report> findByIdWithUsers(@Param("id") Long id);
}
