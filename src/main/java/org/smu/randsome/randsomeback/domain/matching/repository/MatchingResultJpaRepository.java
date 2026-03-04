package org.smu.randsome.randsomeback.domain.matching.repository;

import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingResultJpaRepository extends JpaRepository<MatchingResult, Long> {

}