package org.smu.randsome.randsomeback.domain.candidate.enums;

public enum RegistrationStatus {

    CANCELED, // 후보자 등록 취소 (관리자 승인 전)
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN, // 후보자 철회
    ;
}