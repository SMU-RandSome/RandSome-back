package org.smu.randsome.randsomeback.domain.payment.repository;

import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithDetails;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentQueryRepository {

    Page<PaymentWithDetails> findAllPaymentsWithRejectedReason(
            PaymentSearchCondition paymentSearchCondition,
            Pageable pageable
    );

}