package org.smu.randsome.randsomeback.domain.candidate.implement;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
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
    public CandidateRegistration approve(Long registrationId, LocalDateTime approvedAt) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatusWithMember(
                registrationId,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE));

        registration.approve(approvedAt);

        // NOTE: #1 회원 역할을 변경을 어디서 하는 게 좋을까? 근데 여긴 후보자 관리하는 곳이니까 여기서 하는게 맞는 것 같기도 하고... MemberManager에게 넘겨야할까..?
        Member member = registration.getMember();
        member.updateRole(Role.ROLE_CANDIDATE);

        return registration;
    }

    @Transactional
    public void reject(Long registrationId, String rejectedReason, LocalDateTime rejectedAt) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatus(registrationId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE));

        registration.reject(rejectedReason, rejectedAt);
    }

    @Transactional
    public void withdraw(Long memberId) {
        // 회원의 활성화된 신청이 존재하는지 확인 (APPROVED 여부 무관)
        boolean hasActiveRegistration = candidateJpaRepository.existsByMemberIdAndStatus(memberId, EntityStatus.ACTIVE);

        if (!hasActiveRegistration) {
            throw new CoreException(ErrorType.NOT_FOUND_CANDIDATE);
        }

        // APPROVED 상태의 활성 신청만 조회
        CandidateRegistration registration = candidateJpaRepository.findByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.APPROVED,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED));

        registration.withdraw(LocalDateTime.now());

        // NOTE: #1
        Member candidate = registration.getMember();
        candidate.updateRole(Role.ROLE_MEMBER);
    }

}