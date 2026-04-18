package org.smu.randsome.randsomeback.domain.attendance.implement;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.attendance.event.AttendanceCheckedEvent;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceCacheManager {

    private final RedisRepository redisRepository;

    /**
     * 출석 체크 트랜잭션이 커밋된 뒤 Redis에 출석 여부를 기록한다. <br>
     * DB 커밋 실패 시 롤백되더라도 Redis에 남아 당일 재시도가 불가해지는 문제를 방지하기 위해
     * AFTER_COMMIT 단계에서 처리한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AttendanceCheckedEvent event) {
        try {
            redisRepository.put(
                    CacheKeys.attendance(event.memberId(), event.date()),
                    "1",
                    calculateTtlUntilMidnight(event.date())
            );
            log.info("[AttendanceCacheManager] 출석 체크 Redis 캐시에 저장 - memberId={}, date={}", event.memberId(), event.date());
        } catch (Exception e) {
            log.error("[AttendanceCacheManager] Redis 캐시 저장 실패 - memberId={}, date={}", event.memberId(), event.date(), e);
        }
    }

    private Duration calculateTtlUntilMidnight(LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = date.plusDays(1).atStartOfDay();
        Duration duration = Duration.between(now, midnight);
        return duration.isNegative() ? Duration.ZERO : duration;
    }

}
