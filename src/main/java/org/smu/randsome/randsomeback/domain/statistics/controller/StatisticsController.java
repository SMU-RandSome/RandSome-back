package org.smu.randsome.randsomeback.domain.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.statistics.controller.dto.response.DashboardResponse;
import org.smu.randsome.randsomeback.domain.statistics.service.StatisticsService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StatisticsController extends StatisticsControllerDocs {

    private final StatisticsService statisticsService;

    @Override
    @GetMapping("/v1/statistics/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse response = statisticsService.getDashboard();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}