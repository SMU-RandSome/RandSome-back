package org.smu.randsome.randsomeback.domain.auth.enums;

public enum VerificationPurpose {

    SIGN_UP,
    PASSWORD_RESET,
    ;

    public boolean isSignUp() {
        return this == SIGN_UP;
    }

    public boolean isPasswordReset() {
        return this == PASSWORD_RESET;
    }

}