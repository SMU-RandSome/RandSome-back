package org.smu.randsome.randsomeback.domain.payment.enums;

public enum PaymentStatus {

    CANCELLED,
    REFUNDED, // 환불 완료
    PENDING,
    COMPLETED,
    REJECTED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

}