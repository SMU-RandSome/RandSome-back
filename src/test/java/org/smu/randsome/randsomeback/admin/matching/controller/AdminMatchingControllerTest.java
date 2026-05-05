package org.smu.randsome.randsomeback.admin.matching.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.http.HttpStatus;

class AdminMatchingControllerTest extends ControllerTestSupport {

    @Test
    @TestAdmin
    void 관리자가_조회하면_200과_페이지_응답_구조를_반환한다() {
        PageResponse<MatchingApplication> page = PageResponse.of(List.of(), 0, 20, 0L);
        given(adminMatchingService.findMatchings(any(), any())).willReturn(page);

        assertThat(mvcTester.get().uri("/v1/admin/matching-applications"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.content", v -> v.assertThat().isNotNull())
                .hasPathSatisfying("$.data.totalElements", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.page", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.size", v -> v.assertThat().isEqualTo(20))
                .hasPathSatisfying("$.data.hasNext", v -> v.assertThat().isEqualTo(false));
    }

    @Test
    @TestAdmin
    void 기본_파라미터로_요청하면_200을_반환한다() {
        given(adminMatchingService.findMatchings(any(), any()))
                .willReturn(PageResponse.of(List.of(), 0, 20, 0L));

        assertThat(mvcTester.get().uri("/v1/admin/matching-applications"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value());
    }

    @Test
    @TestAdmin
    void 필터_파라미터를_전달하면_200을_반환한다() {
        given(adminMatchingService.findMatchings(any(), any()))
                .willReturn(PageResponse.of(List.of(), 0, 10, 0L));

        assertThat(mvcTester.get().uri("/v1/admin/matching-applications?date=2024-04-01&gender=MALE&keyword=홍길동&sort=OLDEST&page=1&size=10"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value());
    }

}