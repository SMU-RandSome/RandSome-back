package org.smu.randsome.randsomeback.domain.payment.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentQueryRepository {

    Page<PaymentWithReason> findPaymentsWithRejectedReason(
            List<PaymentStatus> paymentStatuses,
            Pageable pageable
    );

}