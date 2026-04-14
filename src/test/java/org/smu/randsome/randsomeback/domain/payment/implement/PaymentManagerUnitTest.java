package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;

class PaymentManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    PaymentManager paymentManager;

    @Mock
    PaymentJpaRepository paymentJpaRepository;

    @Mock
    PaymentReader paymentReader;

    @Mock
    List<PaymentApprovalStrategy> strategies;

    @Mock
    ApplicationEventPublisher eventPublisher;


    @Test
    void 결제_확인_시_올바른_전략을_호출한다() {
        // given
        var paymentId = 1L;
        var referenceId = 10L;
        var payment = mock(Payment.class);
        given(payment.getPaymentType()).willReturn(PaymentType.CANDIDATE_REGISTRATION);
        given(payment.getReferenceId()).willReturn(referenceId);
        given(payment.getMember()).willReturn(mock(Member.class));
        given(paymentReader.find(paymentId)).willReturn(payment);

        var strategy = mock(PaymentApprovalStrategy.class);
        given(strategy.getSupportedTypes()).willReturn(Set.of(PaymentType.CANDIDATE_REGISTRATION));
        given(strategies.stream()).willReturn(Stream.of(strategy));

        // when
        paymentManager.approve(paymentId);

        // then
        verify(strategy).approve(eq(referenceId), any(LocalDateTime.class));
    }

    @Test
    void 결제_거절_시_올바른_전략을_호출한다() {
        // given
        var paymentId = 1L;
        var referenceId = 10L;
        var reason = "사유";
        var payment = mock(Payment.class);
        given(payment.getPaymentType()).willReturn(PaymentType.CANDIDATE_REGISTRATION);
        given(payment.getReferenceId()).willReturn(referenceId);
        given(payment.getMember()).willReturn(mock(Member.class));
        given(paymentReader.find(paymentId)).willReturn(payment);

        var strategy = mock(PaymentApprovalStrategy.class);
        given(strategy.getSupportedTypes()).willReturn(Set.of(PaymentType.CANDIDATE_REGISTRATION));
        given(strategies.stream()).willReturn(Stream.of(strategy));

        // when
        paymentManager.reject(paymentId, reason);

        // then
        verify(strategy).reject(eq(referenceId), eq(reason), any(LocalDateTime.class));
    }

    @Test
    void 지원하는_전략이_없으면_DEFAULT_ERROR를_던진다() {
        // given
        var paymentId = 1L;
        var payment = mock(Payment.class);
        given(payment.getPaymentType()).willReturn(PaymentType.CANDIDATE_REGISTRATION);
        given(paymentReader.find(paymentId)).willReturn(payment);
        given(strategies.stream()).willReturn(Stream.of());

        // when & then
        assertThatThrownBy(() -> paymentManager.approve(paymentId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DEFAULT_ERROR.getMessage());
    }


}
