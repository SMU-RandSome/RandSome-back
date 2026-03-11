package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;

class PaymentManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    PaymentManager paymentManager;

    @Mock
    PaymentJpaRepository paymentJpaRepository;

    @Test
    void 결제를_등록한다() {
        // given
        var member = mock(Member.class);

        // when
        paymentManager.register(member, PaymentType.CANDIDATE_REGISTRATION, 1L, 1);

        // then
        verify(paymentJpaRepository).save(any(Payment.class));
    }

}