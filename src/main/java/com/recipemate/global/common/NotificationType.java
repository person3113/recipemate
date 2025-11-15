package com.recipemate.global.common;

public enum NotificationType {
    JOIN_GROUP_BUY,            // 공구 참여 알림
    CANCEL_PARTICIPATION,      // 참여 취소 알림
    COMMENT_GROUP_BUY,         // 공구 댓글 알림
    COMMENT_POST,              // 게시글 댓글 알림
    REPLY_COMMENT,             // 대댓글 알림
    REVIEW_GROUP_BUY,          // 공구 후기 알림
    GROUP_BUY_DEADLINE,        // 공구 마감 알림
    GROUP_BUY_COMPLETED,       // 공구 목표 달성 알림
    DIRECT_MESSAGE             // 쪽지 알림
}
