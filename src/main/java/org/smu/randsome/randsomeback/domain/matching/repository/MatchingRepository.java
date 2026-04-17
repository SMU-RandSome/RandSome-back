package org.smu.randsome.randsomeback.domain.matching.repository;

import org.smu.randsome.randsomeback.global.entity.EntityStatus;

public interface MatchingRepository extends MatchingJpaRepository, MatchingQueryDslRepository {

    boolean existsByIdAndMemberIdAndStatus(Long id, Long memberId, EntityStatus status);
}