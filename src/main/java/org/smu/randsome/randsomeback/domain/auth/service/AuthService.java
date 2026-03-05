package org.smu.randsome.randsomeback.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberReader memberReader;
    private final MemberManager memberManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse login(String email, String password) {
        Member member = memberReader.findByAccount(email, password);

        TokenResponse tokenResponse = jwtProvider.createTokens(member.getId(), member.getRole());
        memberManager.updateRefreshToken(member, tokenResponse.refreshToken());

        log.info("[AuthService] 로그인 성공. memberId: {}", member.getId());

        return tokenResponse;
    }

}