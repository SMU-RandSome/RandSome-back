package org.smu.randsome.randsomeback.admin.candidate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;

class CandidateAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_후보자_신청_승인을_한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/candidates/{candidateRegistrationId}/approve", 1L))
                .apply(print())
                .hasStatusOk();
    }

}