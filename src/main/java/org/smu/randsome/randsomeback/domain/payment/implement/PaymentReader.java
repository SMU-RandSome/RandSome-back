package org.smu.randsome.randsomeback.domain.payment.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentReader {

    private final PaymentRepository paymentRepository;

    public Payment find(Long id) {
        return paymentRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_PAYMENT));
    }

    @Transactional(readOnly = true)
    public Page<PaymentWithReason> findPayments(List<PaymentStatus> paymentStatuses, Pageable pageable) {
        return paymentRepository.findPaymentsWithRejectedReason(paymentStatuses, pageable);
    }

}