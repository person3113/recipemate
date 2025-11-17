package com.recipemate.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("대기", "처리 대기 중"),
    PROCESSED("처리완료", "신고가 처리되었습니다"),
    DISMISSED("기각", "신고가 기각되었습니다");

    private final String displayName;
    private final String description;
}
