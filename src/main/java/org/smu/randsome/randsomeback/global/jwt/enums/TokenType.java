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

    ;

    private final String value;

}