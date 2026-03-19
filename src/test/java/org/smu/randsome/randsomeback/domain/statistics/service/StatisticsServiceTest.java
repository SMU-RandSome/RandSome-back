package org.smu.randsome.randsomeback.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.statistics.dto.response.DashboardResponse;
import org.smu.randsome.randsomeback.domain.statistics.implement.CandidateStatsReader;
import org.smu.randsome.randsomeback.domain.statistics.implement.MatchingStatsReader;

class StatisticsServiceTest extends UnitTestSupport {

    @InjectMocks
    StatisticsService statisticsService;

    @Mock
    MatchingStatsReader matchingStatsReader;

    @Mock
    CandidateStatsReader candidateStatsReader;

    @Test
    void 대시보드_통계를_조회한다() {
        // given
        given(candidateStatsReader.countApproved()).willReturn(5L);
        given(matchingStatsReader.countToday()).willReturn(3L);
        given(matchingStatsReader.countTotal()).willReturn(20L);

        // when
        DashboardResponse response = statisticsService.getDashboard();

        // then
        assertThat(response).extracting(
                DashboardResponse::candidateCount,
                DashboardResponse::todayMatchingCount,
                DashboardResponse::totalMatchingCount
        ).containsExactly(
                5L,
                3L,
                20L
        );
    }

}