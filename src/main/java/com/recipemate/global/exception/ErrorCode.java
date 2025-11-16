package com.recipemate.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER-003", "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER-004", "비밀번호가 일치하지 않습니다."),
    DELETED_USER(HttpStatus.FORBIDDEN, "USER-005", "탈퇴한 회원입니다."),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "USER-006", "포인트가 부족합니다."),
    ALREADY_CHECKED_IN_TODAY(HttpStatus.CONFLICT, "USER-007", "오늘은 이미 출석 체크를 완료했습니다."),
    CANNOT_DELETE_USER_WITH_ACTIVE_GROUP_BUYS(HttpStatus.BAD_REQUEST, "USER-008", "진행 중인 공동구매가 있어 탈퇴할 수 없습니다."),
    EMAIL_USED_BY_DELETED_ACCOUNT(HttpStatus.CONFLICT, "USER-009", "이미 사용된 이메일입니다. 관리자에게 문의하세요."),
    NICKNAME_USED_BY_DELETED_ACCOUNT(HttpStatus.CONFLICT, "USER-010", "이미 사용된 닉네임입니다. 관리자에게 문의하세요."),
    
    GROUP_BUY_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP-001", "공동구매를 찾을 수 없습니다."),
    GROUP_BUY_CLOSED(HttpStatus.BAD_REQUEST, "GROUP-002", "마감된 공동구매입니다."),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "GROUP-003", "이미 참여 중인 공동구매입니다."),
    MAX_PARTICIPANTS_EXCEEDED(HttpStatus.BAD_REQUEST, "GROUP-004", "최대 참여 인원을 초과했습니다."),
    UNAUTHORIZED_GROUP_BUY_ACCESS(HttpStatus.FORBIDDEN, "GROUP-005", "공동구매 접근 권한이 없습니다."),
    HAS_PARTICIPANTS(HttpStatus.BAD_REQUEST, "GROUP-006", "참여자가 있는 공동구매는 삭제할 수 없습니다."),
    INVALID_TITLE(HttpStatus.BAD_REQUEST, "GROUP-007", "제목은 필수입니다."),
    INVALID_CONTENT(HttpStatus.BAD_REQUEST, "GROUP-008", "내용은 필수입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "GROUP-009", "카테고리는 필수입니다."),
    INVALID_TOTAL_PRICE(HttpStatus.BAD_REQUEST, "GROUP-010", "총 금액은 0원 이상이어야 합니다."),
    INVALID_TARGET_HEADCOUNT(HttpStatus.BAD_REQUEST, "GROUP-011", "목표 인원은 2명 이상이어야 합니다."),
    INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "GROUP-012", "마감일은 현재보다 이후여야 합니다."),
    INVALID_DELIVERY_METHOD(HttpStatus.BAD_REQUEST, "GROUP-013", "수령 방법은 필수입니다."),
    INVALID_RECIPE_API_ID(HttpStatus.BAD_REQUEST, "GROUP-014", "레시피 API ID는 필수입니다."),
    TARGET_HEADCOUNT_BELOW_CURRENT(HttpStatus.BAD_REQUEST, "GROUP-015", "목표 인원은 현재 참여 인원보다 작을 수 없습니다."),
    HOST_CANNOT_PARTICIPATE(HttpStatus.BAD_REQUEST, "GROUP-016", "주최자는 자신의 공동구매에 참여할 수 없습니다."),
    PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP-017", "참여하지 않은 공동구매입니다."),
    CANCELLATION_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "GROUP-018", "마감 1일 전에는 참여를 취소할 수 없습니다."),
    UNAUTHORIZED_PARTICIPANT_LIST_ACCESS(HttpStatus.FORBIDDEN, "GROUP-019", "참여자 목록을 볼 수 있는 권한이 없습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "GROUP-020", "수량은 1 이상이어야 합니다."),
    INVALID_SELECTED_DELIVERY_METHOD(HttpStatus.BAD_REQUEST, "GROUP-021", "선택한 수령 방법은 DIRECT 또는 PARCEL이어야 합니다."),
    DELIVERY_METHOD_INCOMPATIBLE(HttpStatus.BAD_REQUEST, "GROUP-022", "선택한 수령 방법이 공구의 수령 방법과 호환되지 않습니다."),
    NO_PARTICIPANTS(HttpStatus.BAD_REQUEST, "GROUP-023", "참여 인원이 0명입니다."),
    IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "GROUP-024", "이미지는 최대 3장까지만 업로드할 수 있습니다."),
    CANNOT_MODIFY_GROUP_BUY(HttpStatus.BAD_REQUEST, "GROUP-025", "마감 또는 마감 임박 상태의 공동구매는 수정할 수 없습니다."),
    ADDRESS_REQUIRED_FOR_PARCEL(HttpStatus.BAD_REQUEST, "GROUP-026", "택배 수령 방법을 선택한 경우 배송지는 필수입니다."),
    
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS-001", "배송지를 찾을 수 없습니다."),
    UNAUTHORIZED_ADDRESS_ACCESS(HttpStatus.FORBIDDEN, "ADDRESS-002", "배송지 접근 권한이 없습니다."),
    
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECIPE-001", "레시피를 찾을 수 없습니다."),
    
    CORRECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CORRECTION-001", "제안을 찾을 수 없습니다."),
    DUPLICATE_CORRECTION_REQUEST(HttpStatus.CONFLICT, "CORRECTION-002", "동일한 레시피에 대한 제안이 이미 대기 중입니다."),
    ALREADY_PROCESSED_CORRECTION(HttpStatus.BAD_REQUEST, "CORRECTION-003", "이미 처리된 제안입니다."),
    
    WISHLIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "WISHLIST-001", "이미 찜한 공동구매입니다."),
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "WISHLIST-002", "찜 내역을 찾을 수 없습니다."),
    
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-001", "게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "POST-002", "게시글 접근 권한이 없습니다."),
    
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT-001", "댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "COMMENT-002", "댓글 접근 권한이 없습니다."),
    UNAUTHORIZED_COMMENT_UPDATE(HttpStatus.FORBIDDEN, "COMMENT-003", "댓글 수정 권한이 없습니다."),
    UNAUTHORIZED_COMMENT_DELETE(HttpStatus.FORBIDDEN, "COMMENT-004", "댓글 삭제 권한이 없습니다."),
    COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT-005", "대댓글의 대댓글은 작성할 수 없습니다."),
    
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-001", "후기를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW-002", "이미 후기를 작성했습니다."),
    UNAUTHORIZED_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "REVIEW-003", "후기 접근 권한이 없습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "REVIEW-004", "별점은 1~5 사이의 값이어야 합니다."),
    GROUP_BUY_NOT_CLOSED(HttpStatus.BAD_REQUEST, "REVIEW-005", "완료된 공구만 후기를 작성할 수 있습니다."),
    GROUP_BUY_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "REVIEW-007", "완료된 공구만 후기를 작성할 수 있습니다."),
    NOT_PARTICIPATED(HttpStatus.BAD_REQUEST, "REVIEW-006", "참여하지 않은 공구는 후기를 작성할 수 없습니다."),
    REVIEW_DELETED_CANNOT_REWRITE(HttpStatus.BAD_REQUEST, "REVIEW-008", "삭제된 후기는 다시 작성할 수 없습니다."),
    
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION-001", "알림을 찾을 수 없습니다."),
    
    DIRECT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "DM-001", "쪽지를 찾을 수 없습니다."),
    CANNOT_SEND_MESSAGE_TO_SELF(HttpStatus.BAD_REQUEST, "DM-002", "자신에게는 쪽지를 보낼 수 없습니다."),
    EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "DM-003", "메시지 내용은 필수입니다."),
    CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "DM-004", "메시지는 최대 1000자까지 입력 가능합니다."),
    UNAUTHORIZED_MESSAGE_ACCESS(HttpStatus.FORBIDDEN, "DM-005", "쪽지 접근 권한이 없습니다."),

    UNAUTHORIZED(HttpStatus.FORBIDDEN, "COMMON-004", "권한이 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "입력값이 올바르지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-003", "입력값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 오류가 발생했습니다."),
    CONCURRENCY_FAILURE(HttpStatus.CONFLICT, "COMMON-005", "동시 요청으로 인해 처리에 실패했습니다. 잠시 후 다시 시도해주세요.");

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
