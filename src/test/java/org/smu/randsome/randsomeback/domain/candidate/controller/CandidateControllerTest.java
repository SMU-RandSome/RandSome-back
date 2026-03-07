package org.smu.randsome.randsomeback.domain.candidate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class CandidateControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 후보자_지원에_성공하면_200을_반환한다() {
        assertThat(mvcTester.post().uri("/v1/candidate-registrations"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() {
        assertThat(mvcTester.post().uri("/v1/candidate-registrations"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

}