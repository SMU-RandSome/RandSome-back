package org.smu.randsome.randsomeback.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.auth.dto.command.VerificationEmailCode;
import org.smu.randsome.randsomeback.domain.auth.dto.request.EmailVerificationCodeVerifyRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.request.EmailVerificationRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.request.LoginRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.request.TokenReissueRequest;
import org.smu.randsome.randsomeback.domain.auth.enums.VerificationPurpose;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AuthControllerTest extends ControllerTestSupport {

    @Test
    void 상명대_이메일로_인증_코드_전송을_요청하면_200을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationRequest(MemberFixture.DEFAULT_EMAIL);

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk();

        verify(emailVerificationService).sendVerificationCodeAsync(MemberFixture.DEFAULT_EMAIL);
    }

    @Test
    void 인증_코드_검증_성공_시_200과_이메일_인증_토큰을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest(MemberFixture.DEFAULT_EMAIL, "123456");
        var verificationEmailCode = new VerificationEmailCode(
                MemberFixture.DEFAULT_EMAIL,
                "123456",
                VerificationPurpose.SIGN_UP
        );
        given(emailVerificationService.verifyEmailCode(verificationEmailCode))
                .willReturn("email.verification.jwt");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify?purpose=SIGN_UP")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk();

        verify(emailVerificationService).verifyEmailCode(verificationEmailCode);
    }

    @Test
    void 인증_코드_검증_요청_시_purpose가_없으면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest(MemberFixture.DEFAULT_EMAIL, "123456");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_검증_요청_시_purpose가_유효하지_않으면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest(MemberFixture.DEFAULT_EMAIL, "123456");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify?purpose=INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 로그인_성공시_AccessToken과RefreshToken을_발급한다() throws JsonProcessingException {
        // given
        var loginRequest = new LoginRequest(MemberFixture.DEFAULT_EMAIL, MemberFixture.DEFAULT_RAW_PASSWORD);
        var tokenResponse = new TokenResponse("access.token", "refresh.token");
        given(authService.login(MemberFixture.DEFAULT_EMAIL, MemberFixture.DEFAULT_RAW_PASSWORD))
                .willReturn(tokenResponse);

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.accessToken", v -> v.assertThat().isEqualTo(tokenResponse.accessToken()))
                .hasPathSatisfying("$.data.refreshToken", v -> v.assertThat().isEqualTo(tokenResponse.refreshToken()));
    }

    @Test
    void 토큰_재발급_성공시_AccessToken과RefreshToken을_발급한다() throws JsonProcessingException {
        // given
        var request = new TokenReissueRequest("old.refresh.token");
        var tokenResponse = new TokenResponse("new.access.token", "new.refresh.token");
        given(authService.reissue("old.refresh.token"))
                .willReturn(tokenResponse);

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.accessToken", v -> v.assertThat().isEqualTo(tokenResponse.accessToken()))
                .hasPathSatisfying("$.data.refreshToken", v -> v.assertThat().isEqualTo(tokenResponse.refreshToken()));

        verify(authService).reissue("old.refresh.token");
    }

    @Test
    @TestMember
    void 로그아웃_성공_시_200을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/logout"))
                .apply(print())
                .hasStatusOk();

        verify(authService).logout(1L);
    }

    @Test
    void 로그아웃_요청_시_인증되지_않으면_403을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/logout"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 상명대_이메일이_아닌_경우_인증_코드_전송_요청_시_400을_반환한다() throws Exception {
        // given — @sangmyung.kr 도메인이 아닌 이메일은 API 계약상 거부된다
        var request = new EmailVerificationRequest("student@gmail.com");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

}