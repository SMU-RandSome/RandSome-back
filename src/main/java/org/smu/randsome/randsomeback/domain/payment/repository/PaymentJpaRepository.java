package org.smu.randsome.randsomeback.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndStatus(Long id, EntityStatus status);

    @Query("""
            SELECT p.paymentStatus, COUNT(p)
            FROM Payment p
            WHERE p.status = :status
            GROUP BY p.paymentStatus
            """)
    List<PaymentStatusCountItem> countByPaymentStatusAndStatus(@Param("status") EntityStatus status);
}