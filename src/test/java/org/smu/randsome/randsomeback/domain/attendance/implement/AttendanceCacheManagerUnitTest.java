package org.smu.randsome.randsomeback.domain.attendance.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.attendance.event.AttendanceCheckedEvent;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class AttendanceCacheManagerUnitTest extends UnitTestSupport {

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private AttendanceCacheManager attendanceCacheManager;

    @Test
    void 출석_체크를_캐싱할_때_Redis에_1_을_저장하고_TTL을_설정한다() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();
        String expectedKey = "attendance:" + memberId + ":" + today;

        // when
        attendanceCacheManager.handle(new AttendanceCheckedEvent(memberId, today));

        // then
        verify(redisRepository).put(eq(expectedKey), eq("1"), any(Duration.class));
    }

}
