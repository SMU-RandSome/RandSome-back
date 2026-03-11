package org.smu.randsome.randsomeback.domain.candidate.repository;

import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateJpaRepository extends JpaRepository<CandidateRegistration, Long> {

    boolean existsByMemberIdAndRegistrationStatusAndStatus(Long memberId, RegistrationStatus registrationStatus, EntityStatus status);

}