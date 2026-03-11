package org.smu.randsome.randsomeback.domain.candidate.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class CandidateRegistrationTest extends UnitTestSupport {

    @Test
    void 지원하면_PENDING_상태로_생성된다() {
        // given
        var member = mock(Member.class);

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
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        var now = LocalDateTime.of(2026, 3, 8, 12, 0);

        // when
        registration.approve(now);

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(registration.getApprovedAt()).isEqualTo(now);
    }

    @Test
    void 이미_승인된_신청을_다시_승인할경우_아무_일도_일어나지_않는다() {
        // given
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        var now = LocalDateTime.of(2026, 3, 8, 12, 0);

        registration.approve(now);

        // when
        registration.approve(now.plusDays(1));

        // then
        // 승인시간이 변경되지 않아야 한다.
        assertThat(registration.getApprovedAt()).isEqualTo(now);
    }

    @Test
    void 이미_승인된_신청을_거절하면_예외가_발생한다() {
        // given
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        registration.approve(TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> registration.reject("사유", TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_ALLOW_ALREADY_APPROVED_REGISTRATION);
    }

    @Test
    void 거절하면_REJECTED_상태로_변경되고_사유가_기록된다() {
        // given
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        var reason = "조건 미달";

        // when
        registration.reject(reason, TestDateTimeUtils.now());

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(registration.getRejectedReason()).isEqualTo(reason);
    }

    @Test
    void 이미_거절된_신청을_다시_거절하면_사유가_업데이트된다() {
        // given
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        registration.reject("기존 사유", TestDateTimeUtils.now());

        var newReason = "변경된 사유";

        // when
        registration.reject(newReason, TestDateTimeUtils.now());

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(registration.getRejectedReason()).isEqualTo(newReason);
    }

    @Test
    void 거절_사유가_null이면_예외가_발생한다() {
        // given
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);

        // when & then
        assertThatThrownBy(() -> registration.reject(null, TestDateTimeUtils.now()))
                .isInstanceOf(NullPointerException.class);
    }
}
