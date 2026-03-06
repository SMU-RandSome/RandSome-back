package org.smu.randsome.randsomeback.domain.terms.repository;

import org.smu.randsome.randsomeback.domain.terms.entity.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsAgreementJpaRepository extends JpaRepository<TermsAgreement, Long> {

}