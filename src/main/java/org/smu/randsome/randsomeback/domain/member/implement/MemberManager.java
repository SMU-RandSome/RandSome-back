package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberManager {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public Member create(MemberCredentials credentials, MemberBasicInfo basicInfo, MemberSocialProfile socialProfile) {
        if (memberJpaRepository.existsByEmail_AddressAndStatus(credentials.email(), EntityStatus.ACTIVE)) {
            throw new CoreException(ErrorType.DUPLICATE_EMAIL);
        }

        /* NOTE: 파라미터가 많을 경우 어떻게 넘겨야할까??
                 여기서 VO를 생성하는 건 아닌거같아
                 애그리거트가 담당해야될 거 같고 Domain으로 넘기는 객체를 하나 더 만들어야 되나?
        */
        try {
            return memberJpaRepository.save(Member.create(
                    credentials.email(),
                    credentials.password(),
                    passwordEncoder,
                    basicInfo.legalName(),
                    basicInfo.gender(),
                    basicInfo.mbti(),
                    socialProfile.instagramId(),
                    socialProfile.selfIntroduction(),
                    socialProfile.idealDescription()
            ));
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.DUPLICATE_EMAIL);
        }
    }

    public void updateRefreshToken(Member member, String refreshToken) {
        member.updateRefreshToken(refreshToken);
    }

    @Transactional
    public void updateProfile(Long memberId, UpdateProfile updateProfile) {
        Member member = memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));

        member.updateProfile(
                updateProfile.legalName(),
                updateProfile.mbti(),
                updateProfile.instagramId(),
                updateProfile.selfIntroduction(),
                updateProfile.idealDescription()
        );
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

}