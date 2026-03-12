package org.smu.randsome.randsomeback.domain.matching.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingJpaRepository extends JpaRepository<MatchingApplication, Long> {

    Optional<MatchingApplication> findByIdAndStatus(Long id, EntityStatus status);

}