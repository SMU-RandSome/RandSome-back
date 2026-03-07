package org.smu.randsome.randsomeback.domain.payment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;

class CandidatePaymentTest extends UnitTestSupport {

    @Test
    void 등록하면_후보자_등록_타입으로_PENDING_상태로_생성된다() {
        // given
        var member = mock(Member.class);
        var registration = mock(CandidateRegistration.class);

        // when
        CandidatePayment payment = CandidatePayment.register(member, registration);

        // then
        assertThat(payment.getMember()).isEqualTo(member);
        assertThat(payment.getCandidateRegistration()).isEqualTo(registration);
        assertThat(payment.getPaymentType()).isEqualTo(PaymentType.CANDIDATE_REGISTRATION);
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getRejectedReason()).isNull();
    }

    @Test
    void member가_null이면_예외가_발생한다() {
        // given
        var registration = mock(CandidateRegistration.class);

        // when & then
        assertThatThrownBy(() -> CandidatePayment.register(null, registration))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void candidateRegistration이_null이면_예외가_발생한다() {
        // given
        var member = mock(Member.class);

        // when & then
        assertThatThrownBy(() -> CandidatePayment.register(member, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 승인하면_APPROVED_상태로_변경된다() {
        // given
        var member = mock(Member.class);
        var registration = mock(CandidateRegistration.class);
        var payment = CandidatePayment.register(member, registration);

        // when
        payment.approve();

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void 거절하면_REJECTED_상태로_변경되고_사유가_기록된다() {
        // given
        var member = mock(Member.class);
        var registration = mock(CandidateRegistration.class);
        var payment = CandidatePayment.register(member, registration);
        var reason = "결제 정보 불일치";

        // when
        payment.reject(reason);

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getRejectedReason()).isEqualTo(reason);
    }

    @Test
    void 거절_사유가_null이면_예외가_발생한다() {
        // given
        var member = mock(Member.class);
        var registration = mock(CandidateRegistration.class);
        var payment = CandidatePayment.register(member, registration);

        // when & then
        assertThatThrownBy(() -> payment.reject(null))
                .isInstanceOf(NullPointerException.class);
    }

}