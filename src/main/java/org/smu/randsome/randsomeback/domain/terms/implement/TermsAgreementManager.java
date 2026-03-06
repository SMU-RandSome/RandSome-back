package org.smu.randsome.randsomeback.domain.terms.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.terms.entity.TermsAgreement;
import org.smu.randsome.randsomeback.domain.terms.repository.TermsAgreementJpaRepository;
import org.smu.randsome.randsomeback.domain.terms.repository.TermsJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TermsAgreementManager {

    private final TermsJpaRepository termsJpaRepository;
    private final TermsAgreementJpaRepository termsAgreementJpaRepository;

    // DESC: 회원이 모든 약관에 동의한 기록을 저장
    public void saveAll(Long memberId) {
        List<TermsAgreement> agreements = termsJpaRepository.findAllByStatus(EntityStatus.ACTIVE)
                .stream()
                .map(terms -> TermsAgreement.agree(memberId, terms.getId()))
                .toList();
        termsAgreementJpaRepository.saveAll(agreements);
    }

}