package org.smu.randsome.randsomeback.admin.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.payment.dto.request.PaymentRejectRequest;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PaymentAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_결제_승인에_성공하면_200을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/payments/1/confirm"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());

        then(paymentAdminService).should().confirm(1L);
    }

    @TestAdmin
    @Test
    void 관리자가_결제_거절에_성공하면_200을_반환한다() throws Exception {
        // given
        var request = new PaymentRejectRequest("증빙 서류 미비");

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/payments/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());

        then(paymentAdminService).should().reject(1L, "증빙 서류 미비");
    }

    @TestAdmin
    @Test
    void 결제_거절_사유가_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new PaymentRejectRequest("");

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/payments/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        then(paymentAdminService).should(never()).reject(1L, "");
    }

    @TestMember
    @Test
    void 일반_회원이_관리자_결제_API를_호출하면_403을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/payments/1/confirm"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());

        then(paymentAdminService).shouldHaveNoInteractions();
    }

}