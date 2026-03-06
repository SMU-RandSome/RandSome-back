package org.smu.randsome.randsomeback.domain.terms.repository;

import org.smu.randsome.randsomeback.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsJpaRepository extends JpaRepository<Terms, Long> {

}