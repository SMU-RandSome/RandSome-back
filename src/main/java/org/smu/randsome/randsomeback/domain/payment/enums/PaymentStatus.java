package org.smu.randsome.randsomeback.domain.payment.enums;

public enum PaymentStatus {

    PENDING,
    COMPLETED,
    REJECTED;

    public boolean isPending() {
        return this == PENDING;
    }

}