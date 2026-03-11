package org.smu.randsome.randsomeback.domain.payment.repository;

import org.smu.randsome.randsomeback.domain.payment.entity.CandidatePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatePaymentJpaRepository extends JpaRepository<CandidatePayment, Long> {

}