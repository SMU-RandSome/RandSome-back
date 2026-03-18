package org.smu.randsome.randsomeback.domain.member.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;

class CandidateRegistrationStatusViewTest {

    @Test
    void 후보자_신청_이력이_없으면_NOT_APPLIED를_반환한다() {
        CandidateRegistrationStatusView result = CandidateRegistrationStatusView.from(Optional.empty());

        assertThat(result).isEqualTo(CandidateRegistrationStatusView.NOT_APPLIED);
    }

    @Test
    void PENDING_상태는_PENDING으로_변환된다() {
        CandidateRegistrationStatusView result = CandidateRegistrationStatusView.from(Optional.of(RegistrationStatus.PENDING));

        assertThat(result).isEqualTo(CandidateRegistrationStatusView.PENDING);
    }

    @Test
    void APPROVED_상태는_APPROVED로_변환된다() {
        CandidateRegistrationStatusView result = CandidateRegistrationStatusView.from(Optional.of(RegistrationStatus.APPROVED));

        assertThat(result).isEqualTo(CandidateRegistrationStatusView.APPROVED);
    }

    @Test
    void REJECTED_상태는_REJECTED로_변환된다() {
        CandidateRegistrationStatusView result = CandidateRegistrationStatusView.from(Optional.of(RegistrationStatus.REJECTED));

        assertThat(result).isEqualTo(CandidateRegistrationStatusView.REJECTED);
    }

    @Test
    void WITHDRAWN_상태는_WITHDRAWN으로_변환된다() {
        CandidateRegistrationStatusView result = CandidateRegistrationStatusView.from(Optional.of(RegistrationStatus.WITHDRAWN));

        assertThat(result).isEqualTo(CandidateRegistrationStatusView.WITHDRAWN);
    }

}