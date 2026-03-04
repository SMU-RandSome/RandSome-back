package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.time.Clock;
import java.time.LocalDateTime;

record VerificationCodeEntry(String code, LocalDateTime expiredAt) {

    boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(expiredAt);
    }

}