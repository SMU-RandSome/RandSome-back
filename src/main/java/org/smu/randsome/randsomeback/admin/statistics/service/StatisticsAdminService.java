package org.smu.randsome.randsomeback.admin.statistics.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberStatisticsReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StatisticsAdminService {

    private final MemberStatisticsReader memberStatisticsReader;

    public List<CandidateGenderCountItem> findCandidateGenderCount() {
        return memberStatisticsReader.findGenderCountByRole(Role.ROLE_CANDIDATE);
    }


}