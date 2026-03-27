package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 후보자 등록 결제 승인/거절 후속 처리 전략.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CandidatePaymentApprovalStrategy implements PaymentApprovalStrategy {

    private final CandidateManager candidateManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Set<PaymentType> getSupportedTypes() {
        return Set.of(PaymentType.CANDIDATE_REGISTRATION);
    }

    @Override
    public void approve(Long referenceId, LocalDateTime approvedAt) {
        CandidateRegistration candidateRegistration = candidateManager.approve(referenceId, approvedAt);
        eventPublisher.publishEvent(new CandidateRegistrationApprovedEvent(
                candidateRegistration.getMember().getNickname()
        ));

        log.info("[CandidatePaymentApprovalStrategy] 후보자 승인 처리 완료 - candidateRegistrationId={}", referenceId);
    }

    @Override
    public void reject(Long referenceId, String rejectedReason, LocalDateTime rejectedAt) {
        candidateManager.reject(referenceId, rejectedReason, rejectedAt);

        log.info("[CandidatePaymentApprovalStrategy] 후보자 거절 처리 완료 - candidateRegistrationId={}", referenceId);
    }

}
