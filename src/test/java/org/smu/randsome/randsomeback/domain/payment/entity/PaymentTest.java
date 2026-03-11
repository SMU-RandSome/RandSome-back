package org.smu.randsome.randsomeback.domain.payment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class PaymentTest extends UnitTestSupport {

    @Test
    void 결제를_등록하면_PENDING_상태로_생성된다() {
        Member member = mock(Member.class);

        Payment payment = Payment.register(member, PaymentType.RANDOM_MATCHING, 1L, 3);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getConfirmedAt()).isNull();
        assertThat(payment.getRejectedAt()).isNull();
    }

    @Test
    void 결제를_등록하면_금액이_인원수에_따라_계산된다() {
        Member member = mock(Member.class);

        Payment payment = Payment.register(member, PaymentType.RANDOM_MATCHING, 1L, 3);

        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void 결제를_등록하면_paymentType과_referenceId가_저장된다() {
        Member member = mock(Member.class);

        Payment payment = Payment.register(member, PaymentType.IDEAL_TYPE_MATCHING, 42L, 2);

        assertThat(payment.getPaymentType()).isEqualTo(PaymentType.IDEAL_TYPE_MATCHING);
        assertThat(payment.getReferenceId()).isEqualTo(42L);
    }

    @Test
    void 결제를_확정하면_COMPLETED_상태가_되고_확정시각이_기록된다() {
        Member member = mock(Member.class);
        Payment payment = Payment.register(member, PaymentType.RANDOM_MATCHING, 1L, 1);
        LocalDateTime confirmedAt = LocalDateTime.of(2026, 3, 11, 12, 0);

        payment.confirm(confirmedAt);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getConfirmedAt()).isEqualTo(confirmedAt);
    }

    @Test
    void 이미_확정된_결제는_confirm_호출시_상태가_변경되지_않는다() {
        Member member = mock(Member.class);
        Payment payment = Payment.register(member, PaymentType.RANDOM_MATCHING, 1L, 1);
        LocalDateTime firstConfirm = LocalDateTime.of(2026, 3, 11, 12, 0);
        LocalDateTime secondConfirm = LocalDateTime.of(2026, 3, 11, 13, 0);
        payment.confirm(firstConfirm);

        payment.confirm(secondConfirm);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getConfirmedAt()).isEqualTo(firstConfirm);
    }

    @Test
    void 결제를_거절하면_REJECTED_상태가_되고_거절시각이_기록된다() {
        Member member = mock(Member.class);
        Payment payment = Payment.register(member, PaymentType.RANDOM_MATCHING, 1L, 1);
        LocalDateTime rejectedAt = LocalDateTime.of(2026, 3, 11, 12, 0);

        payment.reject(rejectedAt);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getRejectedAt()).isEqualTo(rejectedAt);
    }

    @Test
    void 이미_확정된_결제는_거절시_예외가_발생한다() {
        Member member = mock(Member.class);
        Payment payment = Payment.register(member, PaymentType.RANDOM_MATCHING, 1L, 1);
        payment.confirm(LocalDateTime.of(2026, 3, 11, 12, 0));

        assertThatThrownBy(() -> payment.reject(LocalDateTime.of(2026, 3, 11, 13, 0)))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_ALREADY_CONFIRMED_PAYMENT.getMessage());
    }

    @Test
    void member가_null이면_등록시_예외가_발생한다() {
        assertThatThrownBy(() -> Payment.register(null, PaymentType.RANDOM_MATCHING, 1L, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void paymentType이_null이면_등록시_예외가_발생한다() {
        Member member = mock(Member.class);

        assertThatThrownBy(() -> Payment.register(member, null, 1L, 1))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void referenceId가_null이면_등록시_예외가_발생한다() {
        Member member = mock(Member.class);

        assertThatThrownBy(() -> Payment.register(member, PaymentType.RANDOM_MATCHING, null, 1))
                .isInstanceOf(NullPointerException.class);
    }

}