package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.auth.enums.VerificationPurpose;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.jwt.JwtProvider;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberValidator {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void validateSignUpToken(String emailVerificationToken, String requestEmail) {
        VerificationPurpose verificationPurpose = jwtProvider.extractVerificationPurposeFromToken(emailVerificationToken);
        if (!verificationPurpose.isSignUp()) {
            throw new CoreException(ErrorType.INVALID_VERIFICATION_PURPOSE);
        }

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

    public void validateWithdraw(Member member, String rawPassword) {
        if (member.isAdmin()) {
            throw new CoreException(ErrorType.ADMIN_CANNOT_WITHDRAW);
        }
        if (!member.isPasswordCorrect(rawPassword, passwordEncoder)) {
            throw new CoreException(ErrorType.INCORRECT_PASSWORD);
        }
    }

    public void validateUpdatePassword(String passwordVerificationToken, Member member) {
        VerificationPurpose verificationPurpose = jwtProvider.extractVerificationPurposeFromToken(passwordVerificationToken);
        if (!verificationPurpose.isPasswordReset()) {
            throw new CoreException(ErrorType.INVALID_VERIFICATION_PURPOSE);
        }

        String tokenEmail = jwtProvider.extractEmailFromVerificationToken(passwordVerificationToken);

        if (member.isEmailCorrect(tokenEmail)) {
            return;
        }
        throw new CoreException(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST);
    }

}