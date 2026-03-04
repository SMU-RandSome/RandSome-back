package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerificationCodeManager {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int EXPIRY_MINUTES = 5;

    private final Clock clock;
    private final VerificationCodeStore codeStore;

    public String generateVerificationCode(String email) {
        String code = generateCode();

        codeStore.put(email, new VerificationCodeEntry(code, LocalDateTime.now(clock).plusMinutes(EXPIRY_MINUTES)));

        return code;
    }

    public void invalidateVerificationCode(String email) {
        codeStore.remove(email);
    }

    private static String generateCode() {
        return Integer.toString(100_000 + SECURE_RANDOM.nextInt(900_000));
    }

}