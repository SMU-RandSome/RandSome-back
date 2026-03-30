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
import org.smu.randsome.randsomeback.domain.auth.dto.command.VerificationEmailCode;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;
import org.smu.randsome.randsomeback.domain.auth.enums.VerificationPurpose;
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
        var verificationEmailCode = new VerificationEmailCode(email, code, VerificationPurpose.SIGN_UP);
        var expectedToken = "email.verification.jwt";
        given(jwtProvider.generateEmailVerificationToken(email, VerificationPurpose.SIGN_UP)).willReturn(expectedToken);

        // when
        String result = emailVerificationService.verifyEmailCode(verificationEmailCode);

        // then
        verify(verificationCodeValidator).verifyCode(email, code);
        verify(jwtProvider).generateEmailVerificationToken(email, VerificationPurpose.SIGN_UP);
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void 인증_코드_요청_없이_검증_시_예외를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        var verificationEmailCode = new VerificationEmailCode(email, code, VerificationPurpose.SIGN_UP);
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_NOT_FOUND))
                .given(verificationCodeValidator).verifyCode(email, code);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmailCode(verificationEmailCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    void 인증_코드_불일치_시_예외를_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "000000";
        var verificationEmailCode = new VerificationEmailCode(email, code, VerificationPurpose.SIGN_UP);
        willThrow(new CoreException(ErrorType.VERIFICATION_CODE_MISMATCH))
                .given(verificationCodeValidator).verifyCode(email, code);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmailCode(verificationEmailCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.VERIFICATION_CODE_MISMATCH);
    }

    @Test
    void 비밀번호_재설정_목적으로_인증_성공_시_해당_목적의_토큰을_반환한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        var verificationEmailCode = new VerificationEmailCode(email, code, VerificationPurpose.PASSWORD_RESET);
        var expectedToken = "password.reset.verification.jwt";
        given(jwtProvider.generateEmailVerificationToken(email, VerificationPurpose.PASSWORD_RESET)).willReturn(expectedToken);

        // when
        String result = emailVerificationService.verifyEmailCode(verificationEmailCode);

        // then
        verify(verificationCodeValidator).verifyCode(email, code);
        verify(jwtProvider).generateEmailVerificationToken(email, VerificationPurpose.PASSWORD_RESET);
        assertThat(result).isEqualTo(expectedToken);
    }

}