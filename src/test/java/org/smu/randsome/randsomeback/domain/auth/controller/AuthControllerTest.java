package org.smu.randsome.randsomeback.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationCodeVerifyRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationRequest;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AuthControllerTest extends ControllerTestSupport {

    @Test
    void 상명대_이메일로_인증_코드_전송을_요청하면_200을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationRequest("student@sangmyung.kr");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatusOk();

        verify(emailVerificationService).sendVerificationCodeAsync("student@sangmyung.kr");
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
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_검증_성공_시_200과_이메일_인증_토큰을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest("student@sangmyung.kr", "123456");
        given(emailVerificationService.verifyEmailCode("student@sangmyung.kr", "123456"))
                .willReturn("email.verification.jwt");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatusOk();

        verify(emailVerificationService).verifyEmailCode("student@sangmyung.kr", "123456");
    }

    @Test
    void 인증_코드_검증_시_이메일이_상명대_이메일이_아니면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest("student@gmail.com", "123456");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_검증_시_코드가_6자리_숫자가_아니면_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest("student@sangmyung.kr", "12345");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void 인증_코드_불일치_시_400을_반환한다() throws Exception {
        // given
        var request = new EmailVerificationCodeVerifyRequest("student@sangmyung.kr", "000000");
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_MISMATCH))
                .given(emailVerificationService).verifyEmailCode("student@sangmyung.kr", "000000");

        // when & then
        assertThat(mvcTester.post().uri("/v1/auth/email/verification-codes/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

}