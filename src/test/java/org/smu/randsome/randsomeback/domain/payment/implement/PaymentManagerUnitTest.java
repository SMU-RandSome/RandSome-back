package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.entity.CandidatePayment;
import org.smu.randsome.randsomeback.domain.payment.repository.CandidatePaymentJpaRepository;

class PaymentManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    PaymentManager paymentManager;

    @Mock
    CandidatePaymentJpaRepository candidatePaymentJpaRepository;

    @Test
    void 결제를_등록한다() {
        // given
        var member = mock(Member.class);
        var registration = mock(CandidateRegistration.class);
        given(registration.getMember()).willReturn(member);
        given(candidatePaymentJpaRepository.save(any(CandidatePayment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        paymentManager.register(registration);

        // then
        verify(candidatePaymentJpaRepository).save(any(CandidatePayment.class));
    }

}