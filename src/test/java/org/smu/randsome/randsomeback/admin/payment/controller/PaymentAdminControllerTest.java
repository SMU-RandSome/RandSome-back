package org.smu.randsome.randsomeback.admin.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.payment.dto.request.PaymentRejectRequest;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class PaymentAdminControllerTest extends ControllerTestSupport {

    // ===== POST /v1/admin/payments/{paymentId}/confirm =====

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

    // ===== POST /v1/admin/payments/{paymentId}/reject =====

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

    // ===== GET /v1/admin/payments =====

    @TestAdmin
    @Test
    void 관리자가_PENDING_필터로_결제_내역을_조회하면_200과_결제_목록을_반환한다() {
        // given
        var member = mock(Member.class);
        given(member.getLegalName()).willReturn("홍길동");

        var payment = mock(Payment.class);
        given(payment.getId()).willReturn(1L);
        given(payment.getMember()).willReturn(member);
        given(payment.getPaymentType()).willReturn(PaymentType.CANDIDATE_REGISTRATION);
        given(payment.getPaymentStatus()).willReturn(PaymentStatus.PENDING);
        given(payment.getAmount()).willReturn(BigDecimal.valueOf(2000));

        Page<PaymentWithReason> page = new PageImpl<>(List.of(new PaymentWithReason(payment, null)));
        given(paymentAdminService.findPayments(any(), any(Pageable.class))).willReturn(page);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/payments?filterStatus=PENDING"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.totalElements", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.data.content[0].paymentId", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.data.content[0].memberName", v -> v.assertThat().isEqualTo("홍길동"))
                .hasPathSatisfying("$.data.content[0].paymentStatus", v -> v.assertThat().isEqualTo("PENDING"));
    }

    @TestAdmin
    @Test
    void 관리자가_PROCESSED_필터로_결제_내역을_조회하면_200을_반환한다() {
        // given
        given(paymentAdminService.findPayments(any(), any(Pageable.class))).willReturn(Page.empty());

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/payments?filterStatus=PROCESSED"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

    @TestAdmin
    @Test
    void 조회_결과가_없으면_빈_content와_함께_200을_반환한다() {
        // given
        given(paymentAdminService.findPayments(any(), any(Pageable.class))).willReturn(Page.empty());

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/payments?filterStatus=PENDING"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.totalElements", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.content", v -> v.assertThat().asArray().isEmpty());
    }

    @TestAdmin
    @Test
    void filterStatus_파라미터가_없으면_400을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/payments"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        then(paymentAdminService).shouldHaveNoInteractions();
    }

    @TestAdmin
    @Test
    void 잘못된_filterStatus_값이면_400을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/payments?filterStatus=INVALID_STATUS"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());

        then(paymentAdminService).shouldHaveNoInteractions();
    }

    // ===== 인가 검증 =====

    @Test
    void 인증_없이_접근하면_401_또는_403을_반환한다() {
        // when & then: TestSecurityConfig에 별도 AuthenticationEntryPoint가 없어 403 반환
        assertThat(mvcTester.get().uri("/v1/admin/payments?filterStatus=PENDING"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());

        then(paymentAdminService).shouldHaveNoInteractions();
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
