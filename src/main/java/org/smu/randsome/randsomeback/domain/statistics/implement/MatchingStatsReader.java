package org.smu.randsome.randsomeback.domain.statistics.implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingStatsReader {

    private final MatchingJpaRepository matchingJpaRepository;

    public long countTotal() {
        return matchingJpaRepository.countByStatus(EntityStatus.ACTIVE);
    }

    public long countToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return matchingJpaRepository.countByCreatedAtBetweenAndStatus(startOfDay, endOfDay, EntityStatus.ACTIVE);
    }

}