package org.smu.randsome.randsomeback.admin.statistics.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class StatisticsAdminControllerTest extends ControllerTestSupport {

    // ===== GET /v1/admin/statistics/candidates/gender-count =====

    @TestAdmin
    @Test
    void 관리자가_후보자_성별_통계를_조회하면_200과_성별_카운트를_반환한다() {
        // given
        var items = List.of(
                new CandidateGenderCountItem(Gender.MALE, 5),
                new CandidateGenderCountItem(Gender.FEMALE, 3)
        );
        given(statisticsAdminService.findCandidateGenderCount()).willReturn(items);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics/candidates/gender-count"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.maleCount", v -> v.assertThat().isEqualTo(5))
                .hasPathSatisfying("$.data.femaleCount", v -> v.assertThat().isEqualTo(3));
    }

    // ===== GET /v1/admin/statistics/payments/status-count =====

    @TestAdmin
    @Test
    void 관리자가_결제_상태별_통계를_조회하면_200과_집계_결과를_반환한다() {
        // given
        var items = List.of(
                new PaymentStatusCountItem(PaymentStatus.PENDING, 3),
                new PaymentStatusCountItem(PaymentStatus.COMPLETED, 5),
                new PaymentStatusCountItem(PaymentStatus.REJECTED, 2)
        );
        given(statisticsAdminService.findPaymentStatusCount()).willReturn(items);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics/payments/status-count"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.pendingCount", v -> v.assertThat().isEqualTo(3))
                .hasPathSatisfying("$.data.processedCount", v -> v.assertThat().isEqualTo(7));
    }

    @TestAdmin
    @Test
    void 결제_내역이_없으면_pendingCount와_processedCount가_모두_0으로_반환된다() {
        // given
        given(statisticsAdminService.findPaymentStatusCount()).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics/payments/status-count"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.data.pendingCount", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.processedCount", v -> v.assertThat().isEqualTo(0));
    }

    // ===== 인가 검증 =====

    @Test
    void 인증_없이_접근하면_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics/candidates/gender-count"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());

        then(statisticsAdminService).shouldHaveNoInteractions();
    }

    @TestMember
    @Test
    void 일반_회원이_관리자_통계_API를_호출하면_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/statistics/candidates/gender-count"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());

        then(statisticsAdminService).shouldHaveNoInteractions();
    }

}