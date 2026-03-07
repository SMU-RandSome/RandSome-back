package org.smu.randsome.randsomeback.domain.candidate.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CandidateService {

    private final CandidateValidator candidateValidator;
    private final CandidateManager candidateManager;
    private final PaymentManager paymentManager;

    @Transactional
    public void apply(Long memberId) {
        candidateValidator.validateApply(memberId);

        CandidateRegistration candidateRegistration = candidateManager.apply(memberId);

        paymentManager.register(candidateRegistration);
    }

}