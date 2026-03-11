package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.springframework.stereotype.Component;

/**
 * 매칭 결제 승인/거절 후속 처리를 담당한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingPaymentHandler implements PaymentHandler {
    private final MatchingManager matchingManager;

    @Override
    public Set<PaymentType> supports() {
        return Set.of(PaymentType.RANDOM_MATCHING, PaymentType.IDEAL_TYPE_MATCHING);
    }

    @Override
    public void approve(Long referenceId, LocalDateTime now) {
        log.info("[Payment] 매칭 결제 승인 후속 처리 대기 - matchingRequestId={}, handler={}",
                referenceId, matchingManager.getClass().getSimpleName());
        // TODO: matchingManager.approve(referenceId) — MatchingManager 구현 후 채울 것
    }

    @Override
    public void reject(Long referenceId, String rejectedReason, LocalDateTime now) {
        log.info("[Payment] 매칭 결제 거절 후속 처리 대기 - matchingRequestId={}, reason={}, handler={}",
                referenceId, rejectedReason, matchingManager.getClass().getSimpleName());
        // TODO: matchingManager.reject(referenceId, rejectedReason) — MatchingManager 구현 후 채울 것
    }

}