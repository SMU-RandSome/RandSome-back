package org.smu.randsome.randsomeback.admin.candidate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.candidate.dto.request.CandidateRejectRequest;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.http.MediaType;

class CandidateAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_후보자_신청_승인을_한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/candidates/{candidateRegistrationId}/approve", 1L))
                .apply(print())
                .hasStatusOk();
    }

    @TestAdmin
    @Test
    void 관리자가_후보자_신청을_거절_한다() throws JsonProcessingException {
        // given
        var request = new CandidateRejectRequest("부적절한 지원입니다.");

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/candidates/{candidateRegistrationId}/reject", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk();
    }

}