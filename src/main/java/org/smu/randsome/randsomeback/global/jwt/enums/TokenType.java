package org.smu.randsome.randsomeback.global.jwt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenType {

    AUTHORIZATION_HEADER ("Authorization"),
    BEARER_PREFIX        ("Bearer "),
    ACCESS               ("accessToken"),
    REFRESH              ("refreshToken"),
    VERIFICATION         ("verificationToken"),
    QR                   ("qrToken"),

    ;

    private final String value;

    public static boolean isAccessToken(String tokenType) {
        return ACCESS.getValue().equals(tokenType);
    }

}