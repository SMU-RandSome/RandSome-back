package org.smu.randsome.randsomeback.domain.candidate.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;

class CandidateRegistrationTest extends UnitTestSupport {

    @Test
    void 지원하면_PENDING_상태로_생성된다() {
        // given
        Member member = mock(Member.class);

        // when
        CandidateRegistration registration = CandidateRegistration.apply(member);

        // then
        assertThat(registration.getMember()).isEqualTo(member);
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(registration.getApprovedAt()).isNull();
        assertThat(registration.getRejectedReason()).isNull();
    }

    @Test
    void member가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> CandidateRegistration.apply(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 승인하면_APPROVED_상태로_변경되고_승인시간이_기록된다() {
        // given
        Member member = mock(Member.class);
        CandidateRegistration registration = CandidateRegistration.apply(member);
        LocalDateTime now = LocalDateTime.of(2026, 3, 8, 12, 0);

        // when
        registration.approve(now);

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(registration.getApprovedAt()).isEqualTo(now);
    }

    @Test
    void 거절하면_REJECTED_상태로_변경되고_사유가_기록된다() {
        // given
        Member member = mock(Member.class);
        CandidateRegistration registration = CandidateRegistration.apply(member);
        String reason = "조건 미달";

        // when
        registration.reject(reason, LocalDate.now());

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(registration.getRejectedReason()).isEqualTo(reason);
    }

    @Test
    void 거절_사유가_null이면_예외가_발생한다() {
        // given
        Member member = mock(Member.class);
        CandidateRegistration registration = CandidateRegistration.apply(member);

        // when & then
        assertThatThrownBy(() -> registration.reject(null, LocalDate.now()))
                .isInstanceOf(NullPointerException.class);
    }

}
