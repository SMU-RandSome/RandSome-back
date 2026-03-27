package org.smu.randsome.randsomeback.domain.candidate.implement;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class CandidateManager {

    private final CandidateJpaRepository candidateJpaRepository;
    private final MemberManager memberManager;
    private final MemberReader memberReader;

    public CandidateRegistration apply(Long memberId) {
        Member member = memberReader.find(memberId);
        CandidateRegistration registration = candidateJpaRepository.save(CandidateRegistration.apply(member));

        log.info("[CandidateManager] 후보자 등록 생성 완료 - registrationId={}, memberId={}",
                registration.getId(),
                memberId);

        return registration;
    }

    @Transactional
    public CandidateRegistration approve(Long registrationId, LocalDateTime approvedAt) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatusWithMember(
                registrationId,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE));

        registration.approve(approvedAt);

        Member member = registration.getMember();
        memberManager.updateRole(member, Role.ROLE_CANDIDATE);

        log.info("[CandidateManager] 후보자 등록 승인 처리 완료 - registrationId={}, memberId={}",
                registrationId,
                member.getId());

        return registration;
    }

    @Transactional
    public void reject(Long registrationId, String rejectedReason, LocalDateTime rejectedAt) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatus(registrationId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE));

        registration.reject(rejectedReason, rejectedAt);

        log.info("[CandidateManager] 후보자 등록 거절 처리 완료 - registrationId={}", registrationId);
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

        Member candidate = registration.getMember();
        memberManager.updateRole(candidate, Role.ROLE_MEMBER);

        log.info("[CandidateManager] 후보자 등록 철회 처리 완료 - registrationId={}, memberId={}",
                registration.getId(),
                memberId);
    }

}