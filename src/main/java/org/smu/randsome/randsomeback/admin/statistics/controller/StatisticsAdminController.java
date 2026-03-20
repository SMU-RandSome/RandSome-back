package org.smu.randsome.randsomeback.admin.statistics.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.statistics.dto.response.PaymentStatusStatisticsResponse;
import org.smu.randsome.randsomeback.admin.statistics.service.StatisticsAdminService;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StatisticsAdminController extends StatisticsAdminControllerDocs {

    private final StatisticsAdminService statisticsAdminService;

    @Override
    @GetMapping("/v1/admin/statistics/candidates/gender-count")
    public ApiResponse<List<CandidateGenderCountItem>> getCandidateGenderCount() {
        List<CandidateGenderCountItem> items = statisticsAdminService.findCandidateGenderCount();

        return ApiResponse.success(items);
    }

    @Override
    @GetMapping("/v1/admin/statistics/payments/status-count")
    public ApiResponse<PaymentStatusStatisticsResponse> getPaymentStatusCount() {
        List<PaymentStatusCountItem> items = statisticsAdminService.findPaymentStatusCount();

        return ApiResponse.success(PaymentStatusStatisticsResponse.from(items));
    }

}