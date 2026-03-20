package org.smu.randsome.randsomeback.domain.payment.dto;

import org.smu.randsome.randsomeback.domain.payment.entity.Payment;

public record PaymentWithReason(
        Payment payment,
        String rejectedReason
) {

}