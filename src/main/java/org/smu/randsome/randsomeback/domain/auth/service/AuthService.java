package org.smu.randsome.randsomeback.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.auth.enums.EMAIL;
import org.smu.randsome.randsomeback.domain.auth.implement.verificationcode.VerificationCodeManager;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final EmailSender emailSender;
    private final VerificationCodeManager verificationCodeManager;

    public void sendVerificationCode(String email) {
        String code = verificationCodeManager.generateVerificationCode(email);
 
        emailSender.send(email, EMAIL.EMAIL_SUBJECT.getValue(), EMAIL.EMAIL_BODY_TEMPLATE.getValue().formatted(code));
    }

}