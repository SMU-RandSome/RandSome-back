package org.smu.randsome.randsomeback.domain.candidate.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CandidateValidator {

    private final CandidateJpaRepository candidateJpaRepository;

    public void validateApply(Long memberId) {
        if (candidateJpaRepository.existsByMemberIdAndStatus(memberId, EntityStatus.ACTIVE)) {
            throw new CoreException(ErrorType.DUPLICATE_CANDIDATE);
        }
    }

}