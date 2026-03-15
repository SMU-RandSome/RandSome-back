package org.smu.randsome.randsomeback.domain.matching.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchingResultJpaRepository extends JpaRepository<MatchingResult, Long> {

    @Query("SELECT mr FROM MatchingResult mr " +
            "JOIN FETCH mr.candidate " +
            "JOIN FETCH mr.matchingApplication " +
            "WHERE mr.matchingApplication.id = :applicationId " +
            "AND mr.status = :status"
    )
    List<MatchingResult> findAllByApplicationAndStatus(Long applicationId, EntityStatus status);

}