package org.smu.randsome.randsomeback.domain.attendance.implement;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceCacheManager {

    private final RedisRepository redisRepository;

    public void markAttendedToday(Long memberId, LocalDate date) {
        redisRepository.put(CacheKeys.attendance(memberId, date), "1", calculateTtlUntilMidnight(date));

        log.info("[AttendanceCacheManager] 출석 체크 Redis 캐시에 저장 - memberId={}, date={}", memberId, date);
    }

    private Duration calculateTtlUntilMidnight(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = date.plusDays(1).atStartOfDay();
        Duration duration = Duration.between(now, midnight);
        // 만약 현재 시간이 자정 이후라면 음수 값이 나올 수 있으므로, 음수인 경우 TTL을 0으로 설정하여 즉시 만료되도록 합니다.
        return duration.isNegative() ? Duration.ZERO : duration;
    }

}