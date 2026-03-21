package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberValidator {

    private final JwtProvider jwtProvider;

    public void validateSignUpToken(String emailVerificationToken, String requestEmail) {
        String tokenEmail = jwtProvider.extractEmailFromVerificationToken(emailVerificationToken);
        if (tokenEmail.equals(requestEmail)) {
            return;
        }
        throw new CoreException(ErrorType.INVALID_SIGNUP_REQUEST);
    }

    public void validateAdmin(Member member) {
        if (member.isAdmin()) {
            return;
        }
        throw new CoreException(ErrorType.FORBIDDEN_ERROR);
    }

    public void validateUpdatePassword(String passwordVerificationToken, Member member) {
        String tokenEmail = jwtProvider.extractEmailFromVerificationToken(passwordVerificationToken);

        if (member.isEmailCorrect(tokenEmail)) {
            return;
        }
        throw new CoreException(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST);
    }

}