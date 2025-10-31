package com.recipemate.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER-003", "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER-004", "비밀번호가 일치하지 않습니다."),
    DELETED_USER(HttpStatus.FORBIDDEN, "USER-005", "탈퇴한 회원입니다."),
    
    GROUP_BUY_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP-001", "공동구매를 찾을 수 없습니다."),
    GROUP_BUY_CLOSED(HttpStatus.BAD_REQUEST, "GROUP-002", "마감된 공동구매입니다."),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "GROUP-003", "이미 참여 중인 공동구매입니다."),
    MAX_PARTICIPANTS_EXCEEDED(HttpStatus.BAD_REQUEST, "GROUP-004", "최대 참여 인원을 초과했습니다."),
    UNAUTHORIZED_GROUP_BUY_ACCESS(HttpStatus.FORBIDDEN, "GROUP-005", "공동구매 접근 권한이 없습니다."),
    HAS_PARTICIPANTS(HttpStatus.BAD_REQUEST, "GROUP-006", "참여자가 있는 공동구매는 삭제할 수 없습니다."),
    
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECIPE-001", "레시피를 찾을 수 없습니다."),
    
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-001", "게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "POST-002", "게시글 접근 권한이 없습니다."),
    
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT-001", "댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "COMMENT-002", "댓글 접근 권한이 없습니다."),
    
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
