package org.smu.randsome.randsomeback.domain.attendance.implement;

import static org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceCacheManager.KEY_PREFIX;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceValidator {

    private final RedisRepository redisRepository;

    public void validateNotDuplicate(Long memberId, LocalDate today) {
        if (isAttendedToday(memberId, today)) {
            throw new CoreException(ErrorType.DUPLICATE_ATTENDANCE);
        }
    }

    private boolean isAttendedToday(Long memberId, LocalDate date) {
        return redisRepository.get(buildKey(memberId, date)) != null;
    }

    private String buildKey(Long memberId, LocalDate date) {
        return KEY_PREFIX + memberId + ":" + date;
    }

}