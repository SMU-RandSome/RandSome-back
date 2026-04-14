package org.smu.randsome.randsomeback.domain.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.report.dto.request.ReportCreateRequest;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class ReportControllerTest extends ControllerTestSupport {

    @TestMember
    @Test
    void 신고를_성공적으로_생성한다() throws Exception {
        // given
        var request = new ReportCreateRequest(1L, ReportReason.INAPPROPRIATE_CONTENT, "부적절한 프로필입니다.");
        given(reportService.createReport(any(), eq(1L))).willReturn(100L);

        // when & then
        assertThat(mvcTester.post().uri("/v1/reports")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .apply(print())
                .hasStatus(HttpStatus.CREATED.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isEqualTo(100));
    }

}