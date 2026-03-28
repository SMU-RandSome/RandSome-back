package org.smu.randsome.randsomeback.domain.member.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.jwt.TokenHasher;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberReader {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public Member findByAccount(String loginId, String password) {
        Member member = memberJpaRepository.findByEmail_AddressAndStatus(loginId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.INVALID_ACCOUNT));

        if (member.isPasswordCorrect(password, passwordEncoder)) {
            return member;
        }

        throw new CoreException(ErrorType.INVALID_ACCOUNT);
    }

    public Member find(Long memberId) {
        return memberJpaRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Member findWithLock(Long memberId) {
        return memberJpaRepository.findByIdAndStatusWithLock(memberId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

    public Page<Member> findAll(Pageable pageable) {
        return memberJpaRepository.findAllByStatusAndRoleNot(EntityStatus.ACTIVE, Role.ROLE_ADMIN, pageable);
    }

    public List<Member> findCandidatesByGender(Gender gender, int count) {
        return memberJpaRepository.findRandomCandidatesByGender(gender.name(), count);
    }

    public Member findByRefreshToken(String refreshToken) {
        return memberJpaRepository.findByRefreshTokenAndStatus(TokenHasher.hash(refreshToken), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN));
    }

    public Member findByEmail(String email) {
        return memberJpaRepository.findByEmail_AddressAndStatus(email, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));
    }

}