package org.smu.randsome.randsomeback.domain.matching.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingIdealTypeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingIdealTypeSnapshotJpaRepository extends JpaRepository<MatchingIdealTypeSnapshot, Long> {

    Optional<MatchingIdealTypeSnapshot> findByMatchingApplicationId(Long matchingApplicationId);

}