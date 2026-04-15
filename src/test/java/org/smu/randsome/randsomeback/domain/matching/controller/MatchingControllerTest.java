package org.smu.randsome.randsomeback.domain.matching.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.request.MatchingApplyRequest;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class MatchingControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 매칭_신청에_성공하면_200을_반환한다() throws Exception {
        // given
        var request = MatchingApplyRequest.forRandom(2);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() throws Exception {
        // given
        var request = MatchingApplyRequest.forRandom(2);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
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
    void 매칭_신청_내역_조회에서_인증되지_않은_사용자는_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/matchings"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 승인된_신청_상세_조회에_성공하면_200과_목록을_반환한다() {
        // given
        given(matchingService.getApprovedApplication(any(), any())).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications/1/approved"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull());
    }

    @Test
    void 승인된_신청_상세_조회에서_인증되지_않은_사용자는_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications/1/approved"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 매칭_신청_취소에_성공하면_200을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/matching/applications/1/cancel"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    void 취소_요청에서_인증되지_않은_사용자는_403을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/matching/applications/1/cancel"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

}