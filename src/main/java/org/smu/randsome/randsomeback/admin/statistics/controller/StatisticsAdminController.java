package org.smu.randsome.randsomeback.admin.statistics.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.statistics.dto.response.CandidateGenderCountResponse;
import org.smu.randsome.randsomeback.admin.statistics.service.StatisticsAdminService;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StatisticsAdminController extends StatisticsAdminControllerDocs {

    private final StatisticsAdminService statisticsAdminService;

    @Override
    @GetMapping("/v1/admin/statistics/candidates/gender-count")
    public ApiResponse<CandidateGenderCountResponse> getCandidateGenderCount() {
        List<CandidateGenderCountItem> items = statisticsAdminService.findCandidateGenderCount();

        return ApiResponse.success(CandidateGenderCountResponse.from(items));
    }


}