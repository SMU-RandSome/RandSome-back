package org.smu.randsome.randsomeback.domain.terms.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.terms.entity.Terms;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsJpaRepository extends JpaRepository<Terms, Long> {

    List<Terms> findAllByStatus(EntityStatus status);

}