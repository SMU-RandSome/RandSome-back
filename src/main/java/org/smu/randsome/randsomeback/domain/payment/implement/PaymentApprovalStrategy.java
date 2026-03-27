package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;

/**
 * 결제 유형별 승인/거절 후속 처리 전략 계약.
 */
public interface PaymentApprovalStrategy {

    /**
     * 이 전략이 처리할 수 있는 결제 유형 목록.
     */
    Set<PaymentType> getSupportedTypes();

    /**
     * 결제 승인 후속 처리를 수행한다.
     *
     * @param referenceId 결제와 연결된 도메인 엔티티 식별자
     * @param approvedAt  승인 시각
     */
    void approve(Long referenceId, LocalDateTime approvedAt);

    /**
     * 결제 거절 후속 처리를 수행한다.
     *
     * @param referenceId    결제와 연결된 도메인 엔티티 식별자
     * @param rejectedReason 거절 사유
     * @param rejectedAt     거절 시각
     */
    void reject(Long referenceId, String rejectedReason, LocalDateTime rejectedAt);

}