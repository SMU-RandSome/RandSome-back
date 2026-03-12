package org.smu.randsome.randsomeback.domain.statistics.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.statistics.controller.dto.response.DashboardResponse;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class StatisticsControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 대시보드_통계_조회에_성공하면_200을_반환한다() throws Exception {
        // given
        var response = new DashboardResponse(5L, 3L, 20L);
        given(statisticsService.getDashboard()).willReturn(response);

        // when & then
        assertThat(mvcTester.get().uri("/v1/statistics/dashboard"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.candidateCount", v -> v.assertThat().isEqualTo(5))
                .hasPathSatisfying("$.data.todayMatchingCount", v -> v.assertThat().isEqualTo(3))
                .hasPathSatisfying("$.data.totalMatchingCount", v -> v.assertThat().isEqualTo(20))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() {
        assertThat(mvcTester.get().uri("/v1/statistics/dashboard"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

}