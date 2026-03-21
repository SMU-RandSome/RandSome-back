package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.feed.FeedManager;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
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
    private final FeedManager feedManager;

    @Override
    public Set<PaymentType> supports() {
        return Set.of(PaymentType.RANDOM_MATCHING, PaymentType.IDEAL_TYPE_MATCHING);
    }

    @Override
    public void approve(Long referenceId, LocalDateTime approvedAt) {
        try {
            MatchingApplication matchingApplication = matchingManager.approve(referenceId, approvedAt);
            feedManager.recordMatchRequest(
                    matchingApplication.getMember().getNickname(),
                    matchingApplication.getApplicationCount()
            );

            log.info("[MatchingPaymentHandler] 매칭 신청 승인 처리 완료 - matchingApplicationId={}, handler={}",
                    referenceId,
                    matchingManager.getClass().getSimpleName());
        } catch (RuntimeException e) {
            log.error("[MatchingPaymentHandler] 매칭 신청 승인 후속 처리 실패 - matchingApplicationId={}",
                    referenceId,
                    e);
            throw e;
        }
    }

    @Override
    public void reject(Long referenceId, String rejectedReason, LocalDateTime rejectedAt) {
        try {
            matchingManager.reject(referenceId, rejectedReason, rejectedAt);

            log.info("[MatchingPaymentHandler] 매칭 신청 거절 처리 완료 - matchingApplicationId={}, handler={}",
                    referenceId,
                    matchingManager.getClass().getSimpleName());
        } catch (RuntimeException e) {
            log.error("[MatchingPaymentHandler] 매칭 신청 거절 처리 실패 - matchingApplicationId={}",
                    referenceId,
                    e);
            throw e;
        }
    }

}