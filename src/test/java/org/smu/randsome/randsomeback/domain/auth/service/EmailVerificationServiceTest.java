package org.smu.randsome.randsomeback.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeValidator;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class EmailVerificationServiceTest extends UnitTestSupport {

    @InjectMocks
    EmailVerificationService emailVerificationService;

    @Mock
    EmailSender emailSender;

    @Mock
    VerificationCodeManager verificationCodeManager;

    @Mock
    VerificationCodeValidator verificationCodeValidator;

    @Mock
    JwtProvider jwtProvider;

    @Test
    void 인증_코드를_생성하고_해당_이메일로_전송한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        given(verificationCodeManager.generateVerificationCode(email)).willReturn(code);

        // when
        emailVerificationService.sendVerificationCodeAsync(email);

        // then
        verify(verificationCodeManager).generateVerificationCode(email);
        verify(emailSender).send(
                eq(email),
                eq(EMAIL.EMAIL_SUBJECT.getValue()),
                contains(code)
        );
    }

    @Test
    void 인증_코드_검증_성공_시_이메일_인증_JWT를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        var expectedToken = "email.verification.jwt";
        given(jwtProvider.generateEmailVerificationToken(email)).willReturn(expectedToken);

        // when
        String result = emailVerificationService.verifyEmailCode(email, code);

        // then
        verify(verificationCodeValidator).verifyCode(email, code);
        verify(jwtProvider).generateEmailVerificationToken(email);
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void 만료된_코드로_검증_시_예외를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_EXPIRED))
                .given(verificationCodeValidator).verifyCode(email, code);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmailCode(email, code))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_EXPIRED);
    }

    @Test
    void 인증_코드_요청_없이_검증_시_예외를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_NOT_FOUND))
                .given(verificationCodeValidator).verifyCode(email, code);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmailCode(email, code))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    void 인증_코드_불일치_시_예외를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "000000";
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_MISMATCH))
                .given(verificationCodeValidator).verifyCode(email, code);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmailCode(email, code))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_MISMATCH);
    }
}

