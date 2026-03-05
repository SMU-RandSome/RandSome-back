package org.smu.randsome.randsomeback.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationCodeVerifyRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.LoginRequest;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
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
    void 상명대_이메일이_아니면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationRequest("student@gmail.com");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 이메일이_빈_값이면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationRequest("");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 이메일_형식이_올바르지_않으면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationRequest("not-an-email");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_검증_성공_시_200과_이메일_인증_토큰을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest(MemberFixture.DEFAULT_EMAIL, "123456");
        given(emailVerificationService.verifyEmailCode(MemberFixture.DEFAULT_EMAIL, "123456"))
                .willReturn("email.verification.jwt");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk();

        verify(emailVerificationService).verifyEmailCode(MemberFixture.DEFAULT_EMAIL, "123456");
    }

    @Test
    void 인증_코드_검증_시_이메일이_상명대_이메일이_아니면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest("20231323@naver.com", "123456");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_검증_시_코드가_6자리_숫자가_아니면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest(MemberFixture.DEFAULT_EMAIL, "12345");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_불일치_시_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest(MemberFixture.DEFAULT_EMAIL, "000000");
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_MISMATCH))
                .given(emailVerificationService).verifyEmailCode(MemberFixture.DEFAULT_EMAIL, "000000");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
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

}