package org.smu.randsome.randsomeback.domain.attendance.implement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.repository.AttendanceJpaRepository;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.dao.DataIntegrityViolationException;

class AttendanceManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    AttendanceManager attendanceManager;

    @Mock
    MemberReader memberReader;

    @Mock
    AttendanceJpaRepository attendanceJpaRepository;

    @Test
    void 출석_기록을_정상적으로_생성한다() {
        // given
        var member = MemberFixture.create();
        given(memberReader.find(1L)).willReturn(member);
        given(attendanceJpaRepository.saveAndFlush(any(Attendance.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        attendanceManager.create(1L, LocalDate.now());

        // then
        verify(attendanceJpaRepository).saveAndFlush(any(Attendance.class));
    }

    @Test
    void DB_유니크_제약_위반_시_DUPLICATE_ATTENDANCE_예외로_변환한다() {
        // given — Redis 캐시가 비어있어 1차 검증을 통과했으나 DB 제약 조건으로 중복 감지된 경우
        var member = MemberFixture.create();
        given(memberReader.find(1L)).willReturn(member);
        willThrow(DataIntegrityViolationException.class)
                .given(attendanceJpaRepository).saveAndFlush(any(Attendance.class));

        // when & then
        assertThatThrownBy(() -> attendanceManager.create(1L, LocalDate.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_ATTENDANCE.getMessage());
    }

}
