package org.smu.randsome.randsomeback.domain.attendance.implement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class AttendanceValidatorTest extends UnitTestSupport {

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private AttendanceValidator attendanceValidator;

    @Test
    void 오늘_이미_출석한_회원이_캐시에_있으면_예외가_발생한다() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();
        String key = "attendance:" + memberId + ":" + today;
        given(redisRepository.get(key)).willReturn("1");

        // when & then
        assertThatThrownBy(() -> attendanceValidator.validateNotDuplicate(memberId, today))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_ATTENDANCE.getMessage());
    }

    @Test
    void 오늘_아직_출석하지_않은_회원이_캐시에_없으면_통과한다() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();
        String key = "attendance:" + memberId + ":" + today;
        given(redisRepository.get(key)).willReturn(null);

        // when & then
        attendanceValidator.validateNotDuplicate(memberId, today);
    }

}