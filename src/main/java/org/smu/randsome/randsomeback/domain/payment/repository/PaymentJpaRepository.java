package org.smu.randsome.randsomeback.domain.payment.repository;

import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

}