package org.smu.randsome.randsomeback.global.jwt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenExpiration {

    ACCESS_TOKEN       (2 * 60 * 60 * 1000L),      // 2시간
    REFRESH_TOKEN      (7 * 24 * 60 * 60 * 1000L), // 1주일
    VERIFICATION_TOKEN (5 * 60 * 1000L),           // 5분
    QR_TOKEN           (30 * 1000L),               // 30초
    ;

    private final long expirationTime;

}