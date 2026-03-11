package org.smu.randsome.randsomeback.domain.candidate.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidateJpaRepository extends JpaRepository<CandidateRegistration, Long> {

    boolean existsByMemberIdAndRegistrationStatusAndStatus(Long memberId, RegistrationStatus registrationStatus, EntityStatus status);
    boolean existsByMemberIdAndStatus(Long memberId, EntityStatus status);
    Optional<CandidateRegistration> findByIdAndStatus(Long id, EntityStatus status);
    Optional<CandidateRegistration> findByMemberIdAndRegistrationStatusAndStatus(Long memberId, RegistrationStatus registrationStatus, EntityStatus status);
    @Query("""
            SELECT cr FROM CandidateRegistration cr
            JOIN FETCH cr.member m
            WHERE cr.id = :id
              AND cr.status = :status
              AND m.status = :status
            """
    )
    Optional<CandidateRegistration> findByIdAndStatusWithMember(@Param("id") Long id, @Param("status") EntityStatus status);

}