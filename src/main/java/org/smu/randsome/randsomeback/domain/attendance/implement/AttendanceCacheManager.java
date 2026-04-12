package org.smu.randsome.randsomeback.domain.attendance.implement;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceCacheManager {

    protected static final String KEY_PREFIX = "attendance:";

    private final RedisRepository redisRepository;

    public void markAttendedToday(Long memberId, LocalDate date) {
        redisRepository.put(buildKey(memberId, date), "1", calculateTtlUntilMidnight(date));

        log.info("[AttendanceCacheManager] 출석 체크 Redis 캐시에 저장 - memberId={}, date={}", memberId, date);
    }

    private String buildKey(Long memberId, LocalDate date) {
        return KEY_PREFIX + memberId + ":" + date;
    }

    private Duration calculateTtlUntilMidnight(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = date.plusDays(1).atStartOfDay();
        return Duration.between(now, midnight);
    }

}