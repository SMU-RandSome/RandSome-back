package org.smu.randsome.randsomeback.domain.payment.repository;

import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentQueryRepository {

    Page<PaymentWithReason> findAllPaymentsWithRejectedReason(
            PaymentSearchCondition paymentSearchCondition,
            Pageable pageable
    );

}