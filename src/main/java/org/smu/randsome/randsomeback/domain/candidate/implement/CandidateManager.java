package org.smu.randsome.randsomeback.domain.candidate.implement;

import java.time.LocalDateTime;
import java.util.List;
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

    public CandidateRegistration approve(Long candidateRegistrationId) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatusWithMember(
                candidateRegistrationId,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION));

        registration.approve(LocalDateTime.now());

        memberManager.updateRole(registration.getMember(), Role.ROLE_CANDIDATE);

        log.info("[CandidateManager] 후보자 등록 승인 처리 완료 - candidateRegistrationId={}", candidateRegistrationId);

        return registration;
    }

    public void reject(Long registrationId, String rejectedReason) {
        CandidateRegistration registration = candidateJpaRepository.findByIdAndStatus(
                registrationId,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION));

        registration.reject(rejectedReason, LocalDateTime.now());

        log.info("[CandidateManager] 후보자 등록 거절 처리 완료 - registrationId={}", registrationId);
    }

    @Transactional
    public void withdraw(Long memberId) {
        // 회원의 활성화된 신청이 존재하는지 확인 (APPROVED 여부 무관)
        List<CandidateRegistration> registrations = candidateJpaRepository.findAllByMemberIdAndStatus(memberId,
                EntityStatus.ACTIVE);

        if (registrations.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_CANDIDATE);
        }

        // APPROVED는 하나만 존재할 수 있으므로 findFirst()로 충분
        CandidateRegistration registration = registrations.stream()
                .filter(CandidateRegistration::isApproved)
                .findFirst()
                .orElseThrow(() -> new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED));

        registration.withdraw(LocalDateTime.now());
        memberManager.updateRole(registration.getMember(), Role.ROLE_MEMBER);

        log.info("[CandidateManager] 후보자 철회 처리 완료 - registrationId={}, memberId={}",
                registration.getId(),
                memberId);
    }

    public void withdrawAllByMemberId(Long memberId) {
        List<CandidateRegistration> registrations = candidateJpaRepository.findAllByMemberIdAndStatus(
                memberId, EntityStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        for (CandidateRegistration registration : registrations) {
            if (registration.isApproved()) {
                registration.withdraw(now);
            } else if (registration.isPending()) {
                registration.cancel();
            }
        }

        log.info("[CandidateManager] 회원 탈퇴에 따른 후보자 등록 정리 완료 - memberId={}, count={}", memberId, registrations.size());
    }

    @Transactional
    public CandidateRegistration cancel(Long memberId) {
        CandidateRegistration candidateRegistration = candidateJpaRepository.findByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.PENDING,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_ALLOW_CANCEL_NON_PENDING));

        candidateRegistration.cancel();

        log.info("[CandidateManager] 후보 신청 취소 처리 완료 - registrationId={}, memberId={}",
                candidateRegistration.getId(), memberId);

        return candidateRegistration;
    }

}