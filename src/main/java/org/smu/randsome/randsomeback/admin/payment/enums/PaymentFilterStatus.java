package org.smu.randsome.randsomeback.admin.payment.enums;

import java.util.List;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;

public enum PaymentFilterStatus {
    PENDING,    // 승인 대기 → PaymentStatus.PENDING
    PROCESSED;  // 처리 완료 → PaymentStatus.COMPLETED + REJECTED

    public List<PaymentStatus> toPaymentStatuses() {
        return switch (this) {
            case PENDING ->   List.of(PaymentStatus.PENDING);
            case PROCESSED -> List.of(PaymentStatus.COMPLETED, PaymentStatus.REJECTED);
        };
    }

}