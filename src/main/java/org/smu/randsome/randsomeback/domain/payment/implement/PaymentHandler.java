package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.Set;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;

/**
 * 결제 유형별 승인/거절 후속 처리를 담당하는 핸들러 계약.
 */
public interface PaymentHandler {

    /**
     * 현재 핸들러가 처리할 수 있는 결제 유형 목록.
     */
    Set<PaymentType> supports();

    /**
     * 결제 승인 후속 처리를 수행한다.
     *
     * @param referenceId 결제와 연결된 도메인 엔티티 식별자
     * @param now 승인 시각
     */
    void approve(Long referenceId, LocalDateTime now);

    /**
     * 결제 거절 후속 처리를 수행한다.
     *
     * @param referenceId 결제와 연결된 도메인 엔티티 식별자
     * @param rejectedReason 거절 사유
     * @param now 거절 시각
     */
    void reject(Long referenceId, String rejectedReason, LocalDateTime now);

}