package org.smu.randsome.randsomeback.domain.matching.repository;

import org.smu.randsome.randsomeback.domain.matching.entity.MatchingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingJpaRepository extends JpaRepository<MatchingRequest, Long> {

}