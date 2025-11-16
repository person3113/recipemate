package com.recipemate.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

/**
 * MVC 컨트롤러용 전역 예외 처리 핸들러
 * Thymeleaf 뷰 렌더링 및 POST-Redirect-GET 패턴 지원
 */
@ControllerAdvice(basePackages = "com.recipemate.domain")
public class MvcExceptionHandler {

    /**
     * CustomException 처리
     * - POST 요청: PRG 패턴으로 리다이렉트 + Flash 메시지
     * - GET 요청: 에러 페이지 렌더링
     */
    @ExceptionHandler(CustomException.class)
    public Object handleCustomException(
            CustomException e,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        ErrorCode errorCode = e.getErrorCode();

        // POST 요청인 경우: PRG 패턴으로 리다이렉트
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            redirectAttributes.addFlashAttribute("errorMessage", errorCode.getMessage());
            redirectAttributes.addFlashAttribute("errorCode", errorCode.getCode());

            // 요청 URI와 에러 타입에서 리다이렉트 경로 결정
            String refererPath = determineRedirectPath(request, errorCode);
            return "redirect:" + refererPath;
        }

        // GET 요청인 경우: 에러 페이지 렌더링
        return createErrorView(errorCode.getStatus(), errorCode.getMessage());
    }

    /**
     * Validation 예외 처리
     * - POST 요청: PRG 패턴으로 리다이렉트 + Flash 메시지
     * - GET 요청: 에러 페이지 렌더링
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        // 첫 번째 에러 메시지 추출 (필드명을 한글로 변환)
        String errorMessage = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        FieldError fieldError = (FieldError) error;
                        String fieldName = translateFieldName(fieldError.getField());
                        return fieldName + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        // POST 요청인 경우: PRG 패턴으로 리다이렉트
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("errorCode", "VALIDATION-001");

            String refererPath = determineRedirectPath(request, null);
            return "redirect:" + refererPath;
        }

        // GET 요청인 경우: 에러 페이지 렌더링
        return createErrorView(HttpStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * 필드명을 한글로 변환
     */
    private String translateFieldName(String fieldName) {
        switch (fieldName) {
            case "nickname": return "닉네임";
            case "phoneNumber": return "전화번호";
            case "currentPassword": return "현재 비밀번호";
            case "newPassword": return "새 비밀번호";
            case "confirmPassword": return "비밀번호 확인";
            case "email": return "이메일";
            case "password": return "비밀번호";
            case "title": return "제목";
            case "content": return "내용";
            default: return fieldName;
        }
    }

    /**
     * IllegalArgumentException 처리
     * 잘못된 요청 파라미터
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException e) {
        return createErrorView(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * MissingServletRequestParameterException 처리
     * 필수 요청 파라미터 누락
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ModelAndView handleMissingServletRequestParameterException(
            org.springframework.web.bind.MissingServletRequestParameterException e
    ) {
        return createErrorView(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다: " + e.getParameterName());
    }

    /**
     * 일반 예외 처리
     * 예상하지 못한 서버 오류
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e, HttpServletRequest request) {
        // 로그 출력 (프로덕션에서는 로깅 프레임워크 사용)
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();

        // POST 요청인 경우에도 에러 페이지 렌더링 (리다이렉트하면 스택 트레이스 손실)
        return createErrorView(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        );
    }

    /**
     * 요청 URI와 에러 타입을 기반으로 리다이렉트 경로 결정
     */
    private String determineRedirectPath(HttpServletRequest request, ErrorCode errorCode) {
        String path = request.getRequestURI();

        // 특정 에러 타입에 따른 리다이렉트 처리
        if (errorCode != null) {
            // POST_NOT_FOUND: 존재하지 않는 게시글 - 목록으로 리다이렉트
            if (errorCode == ErrorCode.POST_NOT_FOUND && path.contains("/community-posts")) {
                return "/community-posts/list";
            }
            
            // GROUP_BUY_NOT_FOUND: 존재하지 않는 공동구매 - 목록으로 리다이렉트
            if (errorCode == ErrorCode.GROUP_BUY_NOT_FOUND && path.contains("/group-purchases")) {
                return "/group-purchases/list";
            }
        }

        // /auth/signup
        if (path.contains("/auth/signup")) {
            return "/auth/signup";
        }

        // /auth/login
        if (path.contains("/auth/login")) {
            return "/auth/login";
        }

        // /users/me/notifications (알림 설정)
        if (path.contains("/users/me/notifications")) {
            return "/users/me/settings";
        }

        // /users/me/password (비밀번호 변경)
        if (path.contains("/users/me/password")) {
            return "/users/me/settings";
        }

        // /users/me/delete (계정 탈퇴)
        if (path.contains("/users/me/delete")) {
            return "/users/me/settings";
        }

        // /users/me (프로필 수정 - POST /users/me)
        if (path.contains("/users/me")) {
            return "/users/me/settings";
        }

        // /group-purchases/{purchaseId}/... 패턴
        if (path.matches(".*/group-purchases/\\d+/.*")) {
            String purchaseId = path.replaceAll(".*/group-purchases/(\\d+)/.*", "$1");
            return "/group-purchases/" + purchaseId;
        }

        // /group-purchases/{purchaseId}
        if (path.matches(".*/group-purchases/\\d+")) {
            return path;
        }

        // /group-purchases/new 또는 /group-purchases/recipe-based
        if (path.contains("/group-purchases/new") || path.contains("/group-purchases/recipe-based")) {
            return "/group-purchases/new";
        }

        // /community-posts/{postId}/... 패턴
        if (path.matches(".*/community-posts/\\d+/.*")) {
            String postId = path.replaceAll(".*/community-posts/(\\d+)/.*", "$1");
            return "/community-posts/" + postId;
        }

        // /community-posts/{postId}
        if (path.matches(".*/community-posts/\\d+")) {
            return path;
        }

        // /community-posts/new
        if (path.contains("/community-posts/new")) {
            return "/community-posts/new";
        }

        // /community-posts 관련 경로 (POST /community-posts - 게시글 생성)
        if (path.contains("/community-posts")) {
            return "/community-posts/new";
        }

        // /group-purchases 관련 경로
        if (path.contains("/group-purchases")) {
            return "/group-purchases/list";
        }

        // /comments 관련 경로
        // 댓글은 targetType과 targetId를 폼 데이터로 전달받으므로, 
        // 컨트롤러에서 직접 리다이렉트 처리해야 함
        // 여기서는 기본 홈으로 리다이렉트
        if (path.contains("/comments")) {
            return "/";
        }

        // 기본: 홈 페이지
        return "/";
    }

    /**
     * 에러 페이지 ModelAndView 생성
     */
    private ModelAndView createErrorView(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView();

        // HTTP 상태 코드에 따라 에러 페이지 선택
        if (status.is4xxClientError()) {
            mav.setViewName("error/4xx");
        } else {
            mav.setViewName("error/5xx");
        }

        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", message);

        mav.setStatus(status);

        return mav;
    }
}
