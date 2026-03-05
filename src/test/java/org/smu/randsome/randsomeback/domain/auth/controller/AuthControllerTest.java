package org.smu.randsome.randsomeback.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.EmailVerificationRequest;
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

        verify(authService).sendVerificationCodeAsync("student@sangmyung.kr");
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

        verifyNoInteractions(authService);
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

        verifyNoInteractions(authService);
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

        verifyNoInteractions(authService);
    }

}