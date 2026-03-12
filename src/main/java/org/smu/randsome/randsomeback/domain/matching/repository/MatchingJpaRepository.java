package org.smu.randsome.randsomeback.domain.matching.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchingJpaRepository extends JpaRepository<MatchingApplication, Long> {

    Optional<MatchingApplication> findByIdAndStatus(Long id, EntityStatus status);

    long countByStatus(EntityStatus status);

    long countByCreatedAtBetweenAndStatus(LocalDateTime start, LocalDateTime end, EntityStatus status);

    @Query(""" 
            SELECT ma FROM MatchingApplication ma
            JOIN FETCH ma.member m
            WHERE ma.id = :id AND ma.status = :status
            """
    )
    Optional<MatchingApplication> findByIdAndStatusWithMember(@Param("id") Long id, @Param("status") EntityStatus status);

}