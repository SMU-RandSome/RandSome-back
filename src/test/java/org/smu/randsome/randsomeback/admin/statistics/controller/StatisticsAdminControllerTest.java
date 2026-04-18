package org.smu.randsome.randsomeback.admin.statistics.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
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

}