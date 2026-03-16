package org.smu.randsome.randsomeback.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class AuthServiceTest extends UnitTestSupport {

    @InjectMocks
    AuthService authService;

    @Mock
    MemberReader memberReader;

    @Mock
    MemberManager memberManager;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    Member member;

    @Test
    void 로그인_성공_시_리프레시_토큰을_업데이트한다() {
        // given
        var email = "student@sangmyung.kr";
        var password = "password123!";
        var accessToken = "access.token";
        var refreshToken = "refresh.token";

        given(memberReader.findByAccount(email, password)).willReturn(member);
        given(member.getId()).willReturn(1L);
        given(member.getRole()).willReturn(Role.ROLE_MEMBER);
        given(jwtProvider.createTokens(1L, Role.ROLE_MEMBER))
                .willReturn(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build());

        // when
        TokenResponse response = authService.login(email, password);

        // then
        assertThat(response).extracting(
                TokenResponse::accessToken,
                TokenResponse::refreshToken
        ).containsExactly(
                accessToken,
                refreshToken
        );
        verify(memberReader).findByAccount(email, password);
        verify(jwtProvider).createTokens(1L, Role.ROLE_MEMBER);
        verify(memberManager).updateRefreshToken(member, refreshToken);
    }

    @Test
    void 계정정보가_일치하지_않을_경우_예외를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var password = "password123!";

        given(memberReader.findByAccount(email, password))
                .willThrow(new CoreException(ErrorType.INVALID_ACCOUNT));

        // when
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_ACCOUNT.getMessage());
        // then
        verify(memberReader).findByAccount(email, password);
    }

    @Test
    void 토큰_재발급_성공_시_리프레시_토큰을_업데이트한다() {
        // given
        var oldRefreshToken = "old.refresh.token";
        var newAccessToken = "new.access.token";
        var newRefreshToken = "new.refresh.token";

        given(memberReader.findByRefreshToken(oldRefreshToken)).willReturn(member);
        given(jwtProvider.isTokenValid(oldRefreshToken)).willReturn(true);
        given(member.getId()).willReturn(1L);
        given(member.getRole()).willReturn(Role.ROLE_MEMBER);
        given(jwtProvider.createTokens(1L, Role.ROLE_MEMBER))
                .willReturn(TokenResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build());

        // when
        TokenResponse response = authService.reissue(oldRefreshToken);

        // then
        assertThat(response).extracting(
                TokenResponse::accessToken,
                TokenResponse::refreshToken
        ).containsExactly(
                newAccessToken,
                newRefreshToken
        );
        verify(memberReader).findByRefreshToken(oldRefreshToken);
        verify(jwtProvider).isTokenValid(oldRefreshToken);
        verify(jwtProvider).createTokens(1L, Role.ROLE_MEMBER);
        verify(memberManager).updateRefreshToken(member, newRefreshToken);
    }

    @Test
    void 토큰_재발급_시_유효하지_않은_리프레시_토큰이면_예외를_반환한다() {
        // given
        var expiredRefreshToken = "expired.refresh.token";

        given(memberReader.findByRefreshToken(expiredRefreshToken)).willReturn(member);
        given(jwtProvider.isTokenValid(expiredRefreshToken)).willReturn(false);

        // when // then
        assertThatThrownBy(() -> authService.reissue(expiredRefreshToken))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_TOKEN.getMessage());

        verify(memberReader).findByRefreshToken(expiredRefreshToken);
        verify(jwtProvider).isTokenValid(expiredRefreshToken);
        verifyNoInteractions(memberManager);
    }

    @Test
    void 토큰_재발급_시_활성_회원이_없으면_예외를_반환한다() {
        // given
        var refreshToken = "invalid.refresh.token";
        given(memberReader.findByRefreshToken(refreshToken))
                .willThrow(new CoreException(ErrorType.NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN));

        // when // then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN.getMessage());

        verify(memberReader).findByRefreshToken(refreshToken);
        verifyNoInteractions(jwtProvider, memberManager);
    }

}