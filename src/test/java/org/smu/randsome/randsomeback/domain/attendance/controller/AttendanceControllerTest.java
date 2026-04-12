package org.smu.randsome.randsomeback.domain.attendance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.security.annotation.TestMember;

class AttendanceControllerTest extends ControllerTestSupport {

    @TestMember
    @Test
    void 회원이_출석_체크를_한다() {
        // given

        // when
        assertThat(mvcTester.post().uri("/v1/attendance"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());

        // then
        verify(attendanceService, times(1)).attend(any(Long.class));
    }

    @TestMember
    @Test
    void 내_출석_현황을_조회한다() {
        // given
        List<Attendance> attendances = List.of(
                Attendance.create(MemberFixture.create(), LocalDate.of(2026, 5, 6)),
                Attendance.create(MemberFixture.create(), LocalDate.of(2026, 5, 7))
        );
        given(attendanceService.getMyAttendance(any(Long.class)))
                .willReturn(attendances);

        // when
        assertThat(mvcTester.get().uri("/v1/attendance"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.totalDays", v -> v.assertThat().isEqualTo(23))
                .hasPathSatisfying("$.data.attendedDays", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data.attendanceDates", v -> v.assertThat().isNotNull());

        // then
        verify(attendanceService, times(1)).getMyAttendance(any(Long.class));
    }

}