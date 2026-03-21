package org.smu.randsome.randsomeback.domain.payment.repository;

import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentQueryRepository {

    Page<PaymentWithReason> findPaymentsWithRejectedReason(
            PaymentSearch paymentSearch,
            Pageable pageable
    );

}