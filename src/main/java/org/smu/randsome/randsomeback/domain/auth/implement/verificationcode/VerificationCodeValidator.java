package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerificationCodeValidator {

    private final Clock clock;
    private final VerificationCodeStore codeStore;

    public void verifyCode(String email, String inputCode) {
        VerificationCodeEntry entry = codeStore.get(email);

        if (entry == null) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_NOT_FOUND);
        }
        if (entry.isExpired(clock)) {
            codeStore.remove(email);
            throw new CoreException(ErrorType.VERIFICATION_CODE_EXPIRED);
        }
        if (!entry.code().equals(inputCode)) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_MISMATCH);
        }

        if (!codeStore.removeIfPresent(email, entry)) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_NOT_FOUND);
        }
    }

}