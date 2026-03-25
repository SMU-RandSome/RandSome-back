package org.smu.randsome.randsomeback.domain.candidate.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CandidateValidator {

    private final CandidateJpaRepository candidateJpaRepository;
    private final MemberReader memberReader;

    public void validateApply(Long memberId) {
        // NOTE: SELECT FOR UPDATE로 member 행에 X락을 걸어서, 같은 멤버에 대한 apply() 트랜잭션들이
        //  순서대로 실행되도록 강제. check와 insert가 원자적으로 보장됨.
        memberReader.findWithLock(memberId);

        if (candidateJpaRepository.existsByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.APPROVED,
                EntityStatus.ACTIVE
        )) {
            throw new CoreException(ErrorType.DUPLICATE_CANDIDATE);
        }

        if (candidateJpaRepository.existsByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.PENDING,
                EntityStatus.ACTIVE
        )) {
            throw new CoreException(ErrorType.ALREADY_PENDING_CANDIDATE);
        }
    }

}