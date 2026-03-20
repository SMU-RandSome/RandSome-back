package org.smu.randsome.randsomeback.domain.payment.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentStatisticsReader {

    private final PaymentJpaRepository paymentJpaRepository;

    public List<PaymentStatusCountItem> findAllStatusCount() {
        return paymentJpaRepository.countByPaymentStatusAndStatus(EntityStatus.ACTIVE);
    }

}