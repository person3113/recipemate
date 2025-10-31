package com.recipemate.global.exception;

import com.recipemate.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API용 전역 예외 처리 핸들러
 * JSON 응답만 반환
 * 
 * Note: MVC 컨트롤러의 예외는 MvcExceptionHandler에서 처리
 */
@RestControllerAdvice(basePackages = "com.recipemate.domain.recipe")
public class ApiExceptionHandler {

    /**
     * CustomException 처리
     * JSON 응답 반환
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * MethodArgumentNotValidException 처리
     * JSON 응답 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION-001", "입력값 검증에 실패했습니다.", errors));
    }

    /**
     * 일반 예외 처리
     * JSON 응답 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 로그 출력 (프로덕션에서는 로깅 프레임워크 사용)
        System.err.println("Unexpected API error: " + e.getMessage());
        e.printStackTrace();
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("COMMON-002", "서버 오류가 발생했습니다."));
    }
}
