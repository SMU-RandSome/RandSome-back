package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationApprovedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 매칭 결제 승인/거절 후속 처리 전략.
 * RANDOM_MATCHING, IDEAL_TYPE_MATCHING 모두 처리하며, 타입별 매칭 로직은 MatchingManager 내부에서 위임된다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingPaymentApprovalStrategy implements PaymentApprovalStrategy {

    private final MatchingManager matchingManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Set<PaymentType> getSupportedTypes() {
        return Set.of(PaymentType.RANDOM_MATCHING, PaymentType.IDEAL_TYPE_MATCHING);
    }

    @Override
    public void approve(Long referenceId, LocalDateTime approvedAt) {
        MatchingApplication matchingApplication = matchingManager.approve(referenceId, approvedAt);
        eventPublisher.publishEvent(new MatchingApplicationApprovedEvent(
                matchingApplication.getMember().getNickname(),
                matchingApplication.getApplicationCount()
        ));

        log.info("[MatchingPaymentApprovalStrategy] 매칭 신청 승인 처리 완료 - matchingApplicationId={}", referenceId);
    }

    @Override
    public void reject(Long referenceId, String rejectedReason, LocalDateTime rejectedAt) {
        matchingManager.reject(referenceId, rejectedReason, rejectedAt);

        log.info("[MatchingPaymentApprovalStrategy] 매칭 신청 거절 처리 완료 - matchingApplicationId={}", referenceId);
    }

}