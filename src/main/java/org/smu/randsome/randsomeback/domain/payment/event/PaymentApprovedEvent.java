package org.smu.randsome.randsomeback.domain.payment.event;

import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;

/**
 * 결제 승인 이벤트.
 * 관리자가 결제를 승인하면 발행된다.
 */
public record PaymentApprovedEvent(PaymentType paymentType, Long memberId, Long paymentId) {

}