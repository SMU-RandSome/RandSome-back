package org.smu.randsome.randsomeback.domain.candidate.implement;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CandidateReader {

    private final CandidateJpaRepository candidateJpaRepository;

    public Optional<RegistrationStatus> findLatestRegistrationStatus(Long memberId) {
        return candidateJpaRepository.findLatestRegistrationStatusByMemberId(memberId, EntityStatus.ACTIVE);
    }

}