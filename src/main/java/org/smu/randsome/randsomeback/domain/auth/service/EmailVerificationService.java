package org.smu.randsome.randsomeback.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeValidator;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    private final EmailSender emailSender;
    private final VerificationCodeManager verificationCodeManager;
    private final VerificationCodeValidator verificationCodeValidator;
    private final JwtProvider jwtProvider;

    @Async
    public void sendVerificationCodeAsync(String email) {
        String code = verificationCodeManager.generateVerificationCode(email);

        emailSender.send(email, EMAIL.EMAIL_SUBJECT.getValue(), EMAIL.EMAIL_BODY_TEMPLATE.getValue().formatted(code));
    }

    public String verifyEmailCode(String email, String code) {
        verificationCodeValidator.verifyCode(email, code);

        return jwtProvider.generateEmailVerificationToken(email);
    }
}

