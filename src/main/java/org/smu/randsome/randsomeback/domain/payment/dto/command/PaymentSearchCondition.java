package org.smu.randsome.randsomeback.domain.payment.dto.command;

import java.util.List;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;

public record PaymentSearchCondition(
        List<PaymentStatus> paymentStatuses,
        String query
) {

}