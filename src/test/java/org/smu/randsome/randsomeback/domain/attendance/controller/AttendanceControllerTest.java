package org.smu.randsome.randsomeback.domain.attendance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
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

}