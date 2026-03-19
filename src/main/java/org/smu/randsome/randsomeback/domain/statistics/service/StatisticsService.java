package org.smu.randsome.randsomeback.domain.statistics.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.statistics.dto.response.DashboardResponse;
import org.smu.randsome.randsomeback.domain.statistics.implement.CandidateStatsReader;
import org.smu.randsome.randsomeback.domain.statistics.implement.MatchingStatsReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final MatchingStatsReader matchingStatsReader;
    private final CandidateStatsReader candidateStatsReader;

    /**
     * 대시보드 통계 정보를 조회한다.
     * - 후보자 승인 수
     * - 오늘 매칭 신청 수
     * - 전체 매칭 신청 수
     * */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .candidateCount(candidateStatsReader.countApproved())
                .todayMatchingCount(matchingStatsReader.countToday())
                .totalMatchingCount(matchingStatsReader.countTotal())
                .build();
    }

}
