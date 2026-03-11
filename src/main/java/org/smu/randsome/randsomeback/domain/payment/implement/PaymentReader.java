package org.smu.randsome.randsomeback.domain.payment.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentReader {

    private final PaymentJpaRepository paymentJpaRepository;

    public Payment find(Long id) {
        return paymentJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_PAYMENT));
    }

}