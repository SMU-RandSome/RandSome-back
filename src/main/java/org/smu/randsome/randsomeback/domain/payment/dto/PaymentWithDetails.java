package org.smu.randsome.randsomeback.domain.payment.dto;

import org.smu.randsome.randsomeback.domain.payment.entity.Payment;

public record PaymentWithDetails(
        Payment payment,
        Integer applicationCount,
        String rejectedReason
) {

}
