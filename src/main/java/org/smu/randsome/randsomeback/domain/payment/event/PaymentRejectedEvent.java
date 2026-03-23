package org.smu.randsome.randsomeback.domain.payment.event;

import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;

/**
 * 결제 거절 이벤트.
 * 관리자가 결제를 거절하면 발행된다.
 */
public record PaymentRejectedEvent(PaymentType paymentType, Long memberId, Long paymentId) {

}