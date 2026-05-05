package org.smu.randsome.randsomeback.domain.member.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;

@Schema(
        description = """
                후보자 신청 상태입니다.
                - `NOT_APPLIED`: 신청 이력 없음
                - `PENDING`: 심사 중
                - `APPROVED`: 승인됨
                - `REJECTED`: 거절됨
                - `WITHDRAWN`: 철회됨
                - `SUSPENDED`: 정지됨
                """
)
public enum CandidateRegistrationStatusView {

    NOT_APPLIED,
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    SUSPENDED;

    public static CandidateRegistrationStatusView from(Optional<RegistrationStatus> status) {
        return status
                .map(CandidateRegistrationStatusView::from)
                .orElse(NOT_APPLIED);
    }

    private static CandidateRegistrationStatusView from(RegistrationStatus status) {
        return switch (status) {
            case CANCELED -> NOT_APPLIED; // 후보자 등록 취소는 신청 이력 없음으로 간주
            case APPROVED -> APPROVED;
            case REJECTED -> REJECTED;
            case PENDING -> PENDING;
            case WITHDRAWN -> WITHDRAWN;
            case SUSPENDED -> SUSPENDED;
        };
    }

}
