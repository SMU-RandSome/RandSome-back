package org.smu.randsome.randsomeback.domain.candidate.repository;

import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateJpaRepository extends JpaRepository<CandidateRegistration, Long> {

}