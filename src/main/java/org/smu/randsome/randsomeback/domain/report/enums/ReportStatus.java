package org.smu.randsome.randsomeback.domain.report.enums;

public enum ReportStatus {

    PENDING,    // 신고 접수 (대기 중)
    IN_REVIEW,  // 검토 중
    RESOLVED,   // 처리 완료
    REJECTED;   // 신고 거절
}
