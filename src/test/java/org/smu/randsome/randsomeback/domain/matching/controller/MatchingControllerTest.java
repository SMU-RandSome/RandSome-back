package org.smu.randsome.randsomeback.domain.matching.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.matching.controller.dto.MatchingApplyRequest;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class MatchingControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 매칭_신청에_성공하면_200을_반환한다() throws Exception {
        // given
        var request = new MatchingApplyRequest(2, MatchingType.RANDOM);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() throws Exception {
        // given
        var request = new MatchingApplyRequest(2, MatchingType.RANDOM);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 매칭_인원수가_최솟값_미만이면_400을_반환한다() throws Exception {
        // given
        var request = new MatchingApplyRequest(0, MatchingType.RANDOM);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 매칭_인원수가_최댓값_초과이면_400을_반환한다() throws Exception {
        // given
        var request = new MatchingApplyRequest(6, MatchingType.RANDOM);

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 존재하지_않는_회원이면_404를_반환한다() throws Exception {
        // given
        var request = new MatchingApplyRequest(2, MatchingType.RANDOM);
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(matchingService).apply(any(), any());

        // when & then
        assertThat(mvcTester.post().uri("/v1/matching")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_MEMBER.getMessage()));
    }

    @Test
    @TestMember
    void 내_신청_내역_조회에_성공하면_200과_목록을_반환한다() {
        // given
        given(matchingService.getMyApplications(any(), any())).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications?status=PENDING"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isNotNull());
    }

    @Test
    void 내_신청_내역_조회에서_인증되지_않은_사용자는_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications?status=PENDING"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 내_신청_내역_조회에서_잘못된_status는_400을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications?status=INVALID"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 승인된_신청_상세_조회에_성공하면_200과_목록을_반환한다() {
        // given
        given(matchingService.getApprovedApplication(any(), any())).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications/1/approved"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
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
    void 승인되지_않은_신청_조회시_400을_반환한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING))
                .given(matchingService).getApprovedApplication(any(), any());

        // when & then
        assertThat(mvcTester.get().uri("/v1/matching/applications/1/approved"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message",
                        v -> v.assertThat().isEqualTo(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING.getMessage()));
    }

}