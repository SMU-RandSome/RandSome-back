package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerificationCodeValidator {

    private final VerificationCodeStore codeStore;

    public void verifyCode(String email, String inputCode) {
        String storedCode = codeStore.get(email);

        if (storedCode == null) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_NOT_FOUND);
        }
        if (!storedCode.equals(inputCode)) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_MISMATCH);
        }
        if (!codeStore.removeIfPresent(email, storedCode)) {
            throw new CoreException(ErrorType.VERIFICATION_CODE_NOT_FOUND);
        }
    }

}