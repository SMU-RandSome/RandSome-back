package org.smu.randsome.randsomeback.domain.payment.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndStatus(Long id, EntityStatus status);

}