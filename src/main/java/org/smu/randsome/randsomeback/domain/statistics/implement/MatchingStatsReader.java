package org.smu.randsome.randsomeback.domain.statistics.implement;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

/**
 * 매칭 통계 조회를 담당한다.
 * 전체 매칭 수는 Redis cache-aside 패턴으로 캐싱한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingStatsReader {

    private final MatchingJpaRepository matchingJpaRepository;
    private final RedisRepository redisRepository;

    private static final Duration TOTAL_COUNT_TTL = Duration.ofSeconds(30);

    /**
     * 전체 매칭 신청 수를 조회한다.
     * 캐시 히트 시 Redis에서 반환하고, 미스 시 DB를 조회한 뒤 캐시에 저장한다.
     */
    public long countTotal() {
        return getFromCache().orElseGet(() -> {
            long count = matchingJpaRepository.countByStatus(EntityStatus.ACTIVE);
            putToCache(count);
            return count;
        });
    }

    public long countToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return matchingJpaRepository.countByCreatedAtBetweenAndStatus(startOfDay, endOfDay, EntityStatus.ACTIVE);
    }

    private Optional<Long> getFromCache() {
        try {
            String cached = redisRepository.get(CacheKeys.MATCHING_TOTAL_COUNT);
            if (cached != null) {
                return Optional.of(Long.parseLong(cached));
            }
        } catch (Exception e) {
            log.warn("[MatchingStatsReader] 캐시 조회 실패, DB 조회. key={}", CacheKeys.MATCHING_TOTAL_COUNT, e);
        }
        return Optional.empty();
    }

    private void putToCache(long count) {
        try {
            redisRepository.put(CacheKeys.MATCHING_TOTAL_COUNT, String.valueOf(count), TOTAL_COUNT_TTL);
        } catch (Exception e) {
            log.warn("[MatchingStatsReader] 캐시 저장 실패. key={}", CacheKeys.MATCHING_TOTAL_COUNT, e);
        }
    }

}