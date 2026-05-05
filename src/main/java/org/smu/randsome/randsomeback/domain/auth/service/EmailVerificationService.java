package org.smu.randsome.randsomeback.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.auth.dto.command.VerificationEmailCode;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeValidator;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
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

        log.info("[EmailVerificationService] 인증코드 발송 완료 - email={}", email);
    }

    public String verifyEmailCode(VerificationEmailCode verificationEmailCode) {
        String email = verificationEmailCode.email();

        verificationCodeValidator.verifyCode(email, verificationEmailCode.code());

        log.info("[EmailVerificationService] 인증코드 검증 완료 - email={}", email);

        return jwtProvider.generateEmailVerificationToken(email, verificationEmailCode.purpose());
    }

}