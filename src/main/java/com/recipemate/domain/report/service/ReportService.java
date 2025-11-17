package com.recipemate.domain.report.service;

import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.comment.service.CommentService;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.post.service.PostService;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.domain.report.dto.ReportCreateRequest;
import com.recipemate.domain.report.dto.ReportResponse;
import com.recipemate.domain.report.entity.Report;
import com.recipemate.domain.report.entity.ReportStatus;
import com.recipemate.domain.report.entity.ReportedEntityType;
import com.recipemate.domain.report.repository.ReportRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final NotificationService notificationService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    /**
     * 신고 생성
     */
    @Transactional
    public ReportResponse createReport(Long reporterId, ReportCreateRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateReportedEntity(request.getReportedEntityType(), request.getReportedEntityId());

        Report report = Report.create(
                reporter,
                request.getReportedEntityType(),
                request.getReportedEntityId(),
                request.getReportType(),
                request.getContent()
        );

        Report savedReport = reportRepository.save(report);
        log.info("신고 생성 완료 - 신고자: {}, 대상: {} ID: {}",
                reporter.getNickname(),
                request.getReportedEntityType().getDisplayName(),
                request.getReportedEntityId());

        return ReportResponse.from(savedReport);
    }

    /**
     * 신고 대상 엔티티 존재 여부 검증
     */
    private void validateReportedEntity(ReportedEntityType entityType, Long entityId) {
        boolean exists = switch (entityType) {
            case RECIPE -> recipeRepository.existsById(entityId);
            case POST -> postRepository.existsById(entityId);
            case COMMENT -> commentRepository.existsById(entityId);
            case GROUP_PURCHASE -> groupBuyRepository.existsById(entityId);
            case USER -> userRepository.existsById(entityId);
        };

        if (!exists) {
            ErrorCode errorCode = switch (entityType) {
                case RECIPE -> ErrorCode.RECIPE_NOT_FOUND;
                case POST -> ErrorCode.POST_NOT_FOUND;
                case COMMENT -> ErrorCode.COMMENT_NOT_FOUND;
                case GROUP_PURCHASE -> ErrorCode.GROUP_BUY_NOT_FOUND;
                case USER -> ErrorCode.USER_NOT_FOUND;
            };
            throw new CustomException(errorCode);
        }
    }

    /**
     * 처리 대기 중인 신고 목록 조회
     */
    public List<ReportResponse> getPendingReports() {
        List<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 상태별 신고 목록 조회
     */
    public List<ReportResponse> getReportsByStatus(ReportStatus status) {
        List<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(status);
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 신고 상세 조회
     */
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        return convertToResponse(report);
    }

    /**
     * Report 엔티티를 ReportResponse로 변환
     * 사용자 신고인 경우 피신고자 닉네임 조회
     */
    private ReportResponse convertToResponse(Report report) {
        ReportResponse response = ReportResponse.from(report);
        
        // 사용자 신고인 경우 피신고자 닉네임 조회
        if (report.getReportedEntityType() == ReportedEntityType.USER) {
            String reportedUserNickname = userRepository.findById(report.getReportedEntityId())
                    .map(User::getNickname)
                    .orElse("삭제된 사용자");
            
            return ReportResponse.builder()
                    .id(response.getId())
                    .reporterNickname(response.getReporterNickname())
                    .reporterId(response.getReporterId())
                    .reportedEntityType(response.getReportedEntityType())
                    .reportedEntityId(response.getReportedEntityId())
                    .reportedUserNickname(reportedUserNickname)
                    .reportType(response.getReportType())
                    .content(response.getContent())
                    .status(response.getStatus())
                    .adminNotes(response.getAdminNotes())
                    .resolverNickname(response.getResolverNickname())
                    .createdAt(response.getCreatedAt())
                    .updatedAt(response.getUpdatedAt())
                    .build();
        }
        
        return response;
    }

    /**
     * 신고 처리 (처리 완료)
     */
    @Transactional
    public void processReport(Long adminId, Long reportId, String notes) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Report report = reportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (!report.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REPORT);
        }

        report.process(admin, notes);
        reportRepository.save(report);

        log.info("신고 처리 완료 - 신고 ID: {}, 관리자: {}", reportId, admin.getNickname());
    }

    /**
     * 신고 기각
     */
    @Transactional
    public void dismissReport(Long adminId, Long reportId, String notes) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Report report = reportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (!report.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REPORT);
        }

        report.dismiss(admin, notes);
        reportRepository.save(report);

        log.info("신고 기각 - 신고 ID: {}, 관리자: {}", reportId, admin.getNickname());
    }

    /**
     * 콘텐츠 삭제 (관리자 권한)
     */
    @Transactional
    public void deleteReportedContent(Long adminId, ReportedEntityType entityType, Long entityId) {
        log.info("오버로드된 deleteReportedContent 호출 - adminId: {}, entityType: {}, entityId: {}", 
                adminId, entityType, entityId);
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.error("오버로드된 메서드에서 관리자를 찾을 수 없음 - adminId: {}", adminId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        log.info("관리자 조회 성공 - admin: {}, isAdmin: {}", admin.getNickname(), admin.isAdmin());

        try {
            switch (entityType) {
                case POST -> {
                    log.info("PostService.deletePost 호출 전 - adminId: {}, postId: {}", adminId, entityId);
                    postService.deletePost(adminId, entityId);
                    log.info("PostService.deletePost 호출 완료");
                }
                case COMMENT -> {
                    log.info("CommentService.deleteComment 호출 전 - adminId: {}, commentId: {}", adminId, entityId);
                    commentService.deleteComment(adminId, entityId);
                    log.info("CommentService.deleteComment 호출 완료");
                }
                case RECIPE, GROUP_PURCHASE, USER -> 
                    throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, 
                        entityType.getDisplayName() + " 삭제는 별도 처리가 필요합니다.");
            }
        } catch (CustomException e) {
            log.error("콘텐츠 삭제 중 오류 발생 - entityType: {}, entityId: {}, errorCode: {}, message: {}", 
                    entityType, entityId, e.getErrorCode(), e.getMessage());
            throw e;
        }

        log.info("신고된 콘텐츠 삭제 - 타입: {}, ID: {}, 관리자: {}",
                entityType.getDisplayName(), entityId, admin.getNickname());
    }

    /**
     * 신고된 콘텐츠 삭제 및 신고 처리 (신고 ID로 처리)
     */
    @Transactional
    public void deleteReportedContent(Long adminId, Long reportId, String notes) {
        log.info("deleteReportedContent 호출됨 - adminId: {}, reportId: {}", adminId, reportId);
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.error("관리자를 찾을 수 없음 - adminId: {}", adminId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        Report report = reportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (!report.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REPORT);
        }

        // 콘텐츠 삭제
        deleteReportedContent(adminId, report.getReportedEntityType(), report.getReportedEntityId());

        // 신고 처리 완료로 변경
        report.process(admin, notes != null ? notes : "콘텐츠 삭제 처리");
        reportRepository.save(report);

        log.info("신고된 콘텐츠 삭제 및 신고 처리 완료 - 신고 ID: {}, 관리자: {}", reportId, admin.getNickname());
    }

    /**
     * 특정 엔티티의 신고 개수 조회
     */
    public long getReportCount(ReportedEntityType entityType, Long entityId) {
        return reportRepository.countByReportedEntityTypeAndReportedEntityId(entityType, entityId);
    }
}
