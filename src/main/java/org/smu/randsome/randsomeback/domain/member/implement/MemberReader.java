package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
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


}