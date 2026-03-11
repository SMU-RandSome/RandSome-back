package org.smu.randsome.randsomeback.domain.payment.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.payment.entity.CandidatePayment;
import org.smu.randsome.randsomeback.domain.payment.repository.CandidatePaymentJpaRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentManager {

    private final CandidatePaymentJpaRepository candidatePaymentJpaRepository;

    public void register(CandidateRegistration candidateRegistration) {
        candidatePaymentJpaRepository.save(CandidatePayment.register(candidateRegistration));
    }

}