package org.smu.randsome.randsomeback.domain.member.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Embeddable
public record Email(
        @Column(name = "email", nullable = false)
        String address
) {

    private static final String SMU_DOMAIN = "@sangmyung.kr";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    public Email {
        if (address == null || !address.matches(EMAIL_REGEX)) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (!address.endsWith(SMU_DOMAIN)) {
            throw new CoreException(ErrorType.INVALID_EMAIL_DOMAIN);
        }
    }

}