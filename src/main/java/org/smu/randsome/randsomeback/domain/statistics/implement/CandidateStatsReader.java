package org.smu.randsome.randsomeback.domain.statistics.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CandidateStatsReader {

    private final CandidateJpaRepository candidateJpaRepository;

    public long countApproved() {
        return candidateJpaRepository.countByRegistrationStatusAndStatus(RegistrationStatus.APPROVED, EntityStatus.ACTIVE);
    }

}