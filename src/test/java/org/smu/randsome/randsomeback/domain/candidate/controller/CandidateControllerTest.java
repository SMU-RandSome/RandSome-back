package org.smu.randsome.randsomeback.domain.candidate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
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

    @TestMember
    @Test
    void 후보자_철회에_성공하면_200을_반환한다() {
        assertThat(mvcTester.post().uri("/v1/candidate-registrations/withdraw"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @TestMember
    @Test
    void 승인된_후보자가_없으면_철회_시_404를_반환한다() {
        willThrow(new CoreException(ErrorType.NOT_FOUND_CANDIDATE))
                .given(candidateService).withdraw(any());

        assertThat(mvcTester.post().uri("/v1/candidate-registrations/withdraw"))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_CANDIDATE.getMessage()));
    }

    @TestMember
    @Test
    void 승인되지_않은_상태에서_철회하면_400을_반환한다() {
        willThrow(new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED))
                .given(candidateService).withdraw(any());

        assertThat(mvcTester.post().uri("/v1/candidate-registrations/withdraw"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED.getMessage()));
    }

    @Test
    void 인증되지_않은_사용자가_철회_시_403을_반환한다() {
        assertThat(mvcTester.post().uri("/v1/candidate-registrations/withdraw"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

}