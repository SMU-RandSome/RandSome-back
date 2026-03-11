package org.smu.randsome.randsomeback.domain.candidate.implement;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CandidateManager {

    private final CandidateJpaRepository candidateJpaRepository;
    private final MemberReader memberReader;

    public CandidateRegistration apply(Long memberId) {
        Member member = memberReader.find(memberId);

        return candidateJpaRepository.save(CandidateRegistration.apply(member));
    }

    @Transactional
    public void approve(Long registrationId, LocalDateTime approvedAt) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatusWithMember(registrationId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE));

        registration.approve(approvedAt);
        registration.getMember().updateRole(Role.ROLE_CANDIDATE);
    }

    @Transactional
    public void reject(Long registrationId, String rejectedReason, LocalDateTime rejectedAt) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatus(registrationId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE));

        registration.reject(rejectedReason, rejectedAt);
    }

}