package org.smu.randsome.randsomeback.domain.candidate.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;

public record CandidateRegistrationItem(
        @Schema(description = "후보자 등록 ID")
        Long id,

        @Schema(description = "회원 ID")
        Long memberId,

        @Schema(description = "회원 닉네임")
        String memberNickname,

        @Schema(description = "회원 법정 이름")
        String memberLegalName,

        @Schema(description = "등록 상태")
        RegistrationStatus registrationStatus,

        @Schema(description = "신청 일시")
        LocalDateTime createdAt,

        @Schema(description = "승인 일시")
        LocalDateTime approvedAt,

        @Schema(description = "거절 일시")
        LocalDateTime rejectedAt,

        @Schema(description = "거절 사유")
        String rejectedReason,

        @Schema(description = "철회 일시")
        LocalDateTime withdrawnAt
) {

    public static CandidateRegistrationItem from(CandidateRegistration candidateRegistration) {
        var member = candidateRegistration.getMember();

        return new CandidateRegistrationItem(
                candidateRegistration.getId(),
                member.getId(),
                member.getNickname(),
                member.getLegalName(),
                candidateRegistration.getRegistrationStatus(),
                candidateRegistration.getCreatedAt(),
                candidateRegistration.getApprovedAt(),
                candidateRegistration.getRejectedAt(),
                candidateRegistration.getRejectedReason(),
                candidateRegistration.getWithdrawnAt()
        );
    }
}
