package org.smu.randsome.randsomeback.domain.auth.service;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;

class AuthServiceTest extends UnitTestSupport {

    @InjectMocks
    AuthService authService;

    @Mock
    EmailSender emailSender;

    @Mock
    VerificationCodeManager verificationCodeManager;

    @Test
    void 인증_코드를_생성하고_해당_이메일로_전송한다() {
        // given
        var email = "student@sangmyung.kr";
        var code = "123456";
        given(verificationCodeManager.generateVerificationCode(email)).willReturn(code);

        // when
        authService.sendVerificationCodeAsync(email);

        // then
        verify(verificationCodeManager).generateVerificationCode(email);
        verify(emailSender).send(
                eq(email),
                eq(EMAIL.EMAIL_SUBJECT.getValue()),
                contains(code)
        );
    }

}