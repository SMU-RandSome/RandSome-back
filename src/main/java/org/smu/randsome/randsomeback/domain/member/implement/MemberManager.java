package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.service.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberManager {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create and persist a new Member from the given credentials, basic information, and social profile.
     *
     * @param credentials   the member's authentication credentials (email and password)
     * @param basicInfo     the member's basic profile information (legal name, gender, MBTI)
     * @param socialProfile the member's social profile information (Instagram ID, self-introduction, ideal description)
     * @return the persisted Member entity
     * @throws CoreException if an ACTIVE member already exists with the provided email (error type DUPLICATE_EMAIL)
     */
    public Member create(MemberCredentials credentials, MemberBasicInfo basicInfo, MemberSocialProfile socialProfile) {
        if (memberJpaRepository.existsByEmail_AddressAndStatus(credentials.email(), EntityStatus.ACTIVE)) {
            throw new CoreException(ErrorType.DUPLICATE_EMAIL);
        }

        /* NOTE: 파라미터가 많을 경우 어떻게 넘겨야할까??
                 여기서 VO를 생성하는 건 아닌거같아
                 애그리거트가 담당해야될 거 같고 Domain으로 넘기는 객체를 하나 더 만들어야 되나?
        */
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
    }

    /**
     * Updates the stored refresh token for the given member.
     *
     * @param member the Member whose refresh token will be replaced
     * @param refreshToken the new refresh token value to store
     */
    public void updateRefreshToken(Member member, String refreshToken) {
        member.updateRefreshToken(refreshToken);
    }

}