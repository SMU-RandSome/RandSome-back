package org.smu.randsome.randsomeback.domain.terms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class TermsAgreement extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long termsId;

    @Column(nullable = false)
    private boolean agreed;

    @Column(nullable = false)
    private LocalDateTime agreedAt;

    public static TermsAgreement agree(Long memberId, Long termsId) {
        TermsAgreement termsAgreement = new TermsAgreement();

        termsAgreement.memberId = memberId;
        termsAgreement.termsId = termsId;
        termsAgreement.agreed = true;
        termsAgreement.agreedAt = LocalDateTime.now();

        return termsAgreement;
    }

}