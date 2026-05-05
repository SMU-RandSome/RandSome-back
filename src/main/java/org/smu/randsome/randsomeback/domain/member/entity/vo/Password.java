package org.smu.randsome.randsomeback.domain.member.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
public record Password(
        @Column(name = "password", nullable = false)
        String hashedValue
) {

    public static Password create(String rawPassword, PasswordEncoder encoder) {
        return new Password(encoder.encode(rawPassword));
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.hashedValue);
    }

}