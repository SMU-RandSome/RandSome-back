package org.smu.randsome.randsomeback.domain.matching.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

}