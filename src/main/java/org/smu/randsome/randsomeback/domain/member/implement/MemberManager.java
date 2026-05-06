package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberTagsInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberRestriction;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.event.MemberProfileTagUpdatedEvent;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberRestrictionJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberManager {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRestrictionJpaRepository memberRestrictionJpaRepository;
    private final MemberProfileTagManager memberProfileTagManager;
    private final MemberDeviceManager memberDeviceManager;
    private final PasswordEncoder passwordEncoder;
    private final SuspensionManager suspensionManager;
    private final ApplicationEventPublisher eventPublisher;

    public Member create(
            MemberCredentials credentials,
            MemberBasicInfo basicInfo,
            MemberSocialProfile socialProfile,
            MemberTagsInfo tagsInfo
    ) {
        if (memberJpaRepository.existsByEmail_AddressAndStatusNot(credentials.email(), EntityStatus.DELETED)) {
            throw new CoreException(ErrorType.DUPLICATE_EMAIL);
        }

        if (memberJpaRepository.existsBySocialProfile_InstagramIdAndStatusNot(socialProfile.instagramId(), EntityStatus.DELETED)) {
            throw new CoreException(ErrorType.DUPLICATE_INSTAGRAM_ID);
        }

        Member member = memberJpaRepository.save(Member.create(
                credentials.email(),
                credentials.password(),
                passwordEncoder,
                basicInfo.legalName(),
                basicInfo.gender(),
                basicInfo.mbti(),
                basicInfo.department(),
                socialProfile.instagramId(),
                socialProfile.selfIntroduction(),
                socialProfile.idealDescription()
        ));

        memberProfileTagManager.create(
                member,
                tagsInfo.personalityTag(),
                tagsInfo.faceTypeTag(),
                tagsInfo.datingStyleTag()
        );

        return member;
    }

    public void updateRefreshToken(Member member, String refreshToken) {
        member.updateRefreshToken(refreshToken);
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfile updateProfile) {
        Member member = memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));

        if (memberJpaRepository.existsBySocialProfile_InstagramIdAndStatusNotAndIdNot(updateProfile.instagramId(), EntityStatus.DELETED, memberId)) {
            throw new CoreException(ErrorType.DUPLICATE_INSTAGRAM_ID);
        }

        member.updateProfile(
                updateProfile.legalName(),
                updateProfile.mbti(),
                updateProfile.department(),
                updateProfile.instagramId(),
                updateProfile.selfIntroduction(),
                updateProfile.idealDescription()
        );

        memberProfileTagManager.updateTags(
                memberId,
                updateProfile.personalityTag(),
                updateProfile.faceTypeTag(),
                updateProfile.datingStyleTag()
        );
        eventPublisher.publishEvent(new MemberProfileTagUpdatedEvent(memberId));
        log.info("[MemberManager] 프로필 수정 완료 - memberId={}", memberId);
    }

    @Transactional
    public void logout(Long memberId) {
        Member member = memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));

        // 추후 FCM 토큰 삭제 로직 추가 예정 -> 아님 API로 뺄수도?
        member.revokeRefreshToken();

        log.info("[MemberManager] 로그아웃 처리 완료 - memberId={}", memberId);
    }

    public void updatePassword(Member member, String newPassword) {
        member.updatePassword(newPassword, passwordEncoder);

        log.info("[MemberManager] 비밀번호 변경 완료 - memberId={}", member.getId());
    }

    public void updateRole(Member member, Role role) {
        member.updateRole(role);

        log.info("[MemberManager] 권한 변경 완료 - memberId={}, newRole={}", member.getId(), role);
    }

    @Transactional
    public void updateRoleById(Long memberId, Role role) {
        validateRoleForManualUpdate(role);

        Member member = memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
        member.updateRole(role);

        log.info("[MemberManager] 권한 변경 완료 - memberId={}, newRole={}", memberId, role);
    }

    @Transactional
    public void suspend(Long memberId, String reason) {
        Member member = memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
        member.suspend();

        memberRestrictionJpaRepository.save(MemberRestriction.create(member, reason));
        suspensionManager.suspend(memberId);

        log.info("[MemberManager] 회원 정지 처리 완료 - memberId = {}", memberId);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberJpaRepository.findByIdAndStatusNot(memberId, EntityStatus.DELETED)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));

        member.withdraw();
        memberDeviceManager.deleteAllByMemberId(memberId);
        memberProfileTagManager.deleteByMemberId(memberId);
        eventPublisher.publishEvent(new MemberProfileTagUpdatedEvent(memberId));

        log.info("[MemberManager] 회원 탈퇴 처리 완료 - memberId={}", memberId);
    }

    @Transactional
    public void restore(Long memberId) {
        Member member = memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.SUSPENDED)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));

        member.active();
        member.updateRole(Role.ROLE_MEMBER);
        suspensionManager.restore(memberId);

        log.info("[MemberManager] 회원 복구 처리 완료 - memberId = {}", member.getId());
    }

    private void validateRoleForManualUpdate(Role role) {
        if (role == Role.ROLE_SUSPEND_MEMBER) {
            throw new CoreException(ErrorType.INVALID_ROLE_UPDATE);
        }
    }

}