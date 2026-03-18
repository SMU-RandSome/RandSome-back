package org.smu.randsome.randsomeback.domain.member.controller.dto;

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
                """
)
public enum CandidateRegistrationStatusView {

    NOT_APPLIED,
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN;

    public static CandidateRegistrationStatusView from(Optional<RegistrationStatus> status) {
        return status
                .map(s -> valueOf(s.name()))
                .orElse(NOT_APPLIED);
    }

}
