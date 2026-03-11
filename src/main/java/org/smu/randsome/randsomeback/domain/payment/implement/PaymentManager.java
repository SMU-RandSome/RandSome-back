package org.smu.randsome.randsomeback.domain.payment.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentManager {

    private final PaymentJpaRepository paymentJpaRepository;

    public void register(Member member, PaymentType paymentType, Long referenceId, int amount) {
        paymentJpaRepository.save(Payment.register(
                member,
                paymentType,
                referenceId,
                amount
        ));
    }

}