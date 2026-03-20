package org.smu.randsome.randsomeback.admin.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberStatisticsReader;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentStatisticsReader;

class StatisticsAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    StatisticsAdminService statisticsAdminService;

    @Mock
    MemberStatisticsReader memberStatisticsReader;

    @Mock
    PaymentStatisticsReader paymentStatisticsReader;

    @Test
    void 후보자_성별_통계_조회를_MemberStatisticsReader에_위임하고_결과를_반환한다() {
        // given
        var expected = List.of(
                new CandidateGenderCountItem(Gender.MALE, 5),
                new CandidateGenderCountItem(Gender.FEMALE, 3)
        );
        given(memberStatisticsReader.findGenderCountByRole(Role.ROLE_CANDIDATE)).willReturn(expected);

        // when
        var result = statisticsAdminService.findCandidateGenderCount();

        // then
        assertThat(result).isEqualTo(expected);
        verify(memberStatisticsReader).findGenderCountByRole(Role.ROLE_CANDIDATE);
    }

    @Test
    void 후보자가_없으면_빈_리스트를_반환한다() {
        // given
        given(memberStatisticsReader.findGenderCountByRole(Role.ROLE_CANDIDATE)).willReturn(List.of());

        // when
        var result = statisticsAdminService.findCandidateGenderCount();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 결제_상태별_통계_조회를_PaymentStatisticsReader에_위임하고_결과를_반환한다() {
        // given
        var expected = List.of(
                new PaymentStatusCountItem(PaymentStatus.PENDING, 3),
                new PaymentStatusCountItem(PaymentStatus.COMPLETED, 5)
        );
        given(paymentStatisticsReader.findAllStatusCount()).willReturn(expected);

        // when
        var result = statisticsAdminService.findPaymentStatusCount();

        // then
        assertThat(result).isEqualTo(expected);
        verify(paymentStatisticsReader).findAllStatusCount();
    }

    @Test
    void 결제_내역이_없으면_빈_리스트를_반환한다() {
        // given
        given(paymentStatisticsReader.findAllStatusCount()).willReturn(List.of());

        // when
        var result = statisticsAdminService.findPaymentStatusCount();

        // then
        assertThat(result).isEmpty();
    }

}