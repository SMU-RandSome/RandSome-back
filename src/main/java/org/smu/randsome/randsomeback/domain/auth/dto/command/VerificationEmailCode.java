package org.smu.randsome.randsomeback.domain.auth.dto.command;

import org.smu.randsome.randsomeback.domain.auth.enums.VerificationPurpose;

public record VerificationEmailCode(
        String email,
        String code,
        VerificationPurpose purpose
) {

}