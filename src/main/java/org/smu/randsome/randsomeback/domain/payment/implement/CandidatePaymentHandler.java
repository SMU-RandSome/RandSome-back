package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.feed.FeedManager;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.springframework.stereotype.Component;

/**
 * 후보자 등록 결제 승인/거절 후속 처리를 담당한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CandidatePaymentHandler implements PaymentHandler {

    private final CandidateManager candidateManager;
    private final FeedManager feedManager;

    @Override
    public Set<PaymentType> supports() {
        return Set.of(PaymentType.CANDIDATE_REGISTRATION);
    }

    @Override
    public void approve(Long referenceId, LocalDateTime approvedAt) {
        CandidateRegistration candidateRegistration = candidateManager.approve(referenceId, approvedAt);

        log.info("[CandidatePaymentHandler] 후보자 등록 결제 승인 완료 - candidateRegistrationId={}, handler={}",
                referenceId, candidateManager.getClass().getSimpleName());

        feedManager.recordCandidateRegistration(candidateRegistration.getMember().getNickname());
    }

    @Override
    public void reject(Long referenceId, String rejectedReason, LocalDateTime rejectedAt) {
        candidateManager.reject(referenceId, rejectedReason, rejectedAt);

        log.info("[CandidatePaymentHandler] 후보자 등록 결제 거절 완료 - candidateRegistrationId={}, handler={}",
                referenceId, candidateManager.getClass().getSimpleName());
    }

}