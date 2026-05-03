package org.smu.randsome.randsomeback.domain.matching.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.request.MatchingApplyRequest;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.http.MediaType;

class MatchingControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 매칭_신청에_성공하면_200과_매칭_결과를_반환한다() throws Exception {
        // given
        var request = MatchingApplyRequest.forRandom(2);

        var matchingApplication = MatchingApplication.apply(
                Mockito.mock(Member.class),
                MatchingType.RANDOM,
                3
        );
        matchingApplication.complete(TestDateTimeUtils.now(), 3);
        given(matchingService.apply(any(), any()))
                .willReturn(matchingApplication);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matchings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull())
                .hasPathSatisfying("$.data.requestedCount", v -> v.assertThat().isEqualTo(3))
                .hasPathSatisfying("$.data.matchingType", v -> v.assertThat().isEqualTo("RANDOM"))
                .hasPathSatisfying("$.data.matchedCount", v -> v.assertThat().isEqualTo(3))
                .hasPathSatisfying("$.data.refundedTickets", v -> v.assertThat().isNotNull())
                .hasPathSatisfying("$.data.isPartialMatch", v -> v.assertThat().isNotNull())
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    @TestMember
    void 매칭_신청_내역_목록_조회에_성공하면_200과_목록을_반환한다() {
        // given
        given(matchingService.findMatchings(any())).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/matchings"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull());
    }

    @Test
    @TestMember
    void 승인된_신청_상세_조회에_성공하면_200과_목록을_반환한다() {
        // given
        given(matchingService.findApplication(any(), any())).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/matchings/applications/{applicationId}", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull());
    }


}