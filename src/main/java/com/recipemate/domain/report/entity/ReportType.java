package com.recipemate.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    INAPPROPRIATE_CONTENT("부적절한 콘텐츠", "음란물, 폭력적 내용 등"),
    SPAM("스팸/광고", "무분별한 광고, 홍보글"),
    ABUSE("욕설/비방", "타인에 대한 모욕, 비방, 욕설"),
    FALSE_INFORMATION("허위 정보", "거짓 정보, 사기성 콘텐츠"),
    COPYRIGHT("저작권 침해", "무단 도용, 저작권 위반"),
    ETC("기타", "기타 사유");

    private final String displayName;
    private final String description;
}
