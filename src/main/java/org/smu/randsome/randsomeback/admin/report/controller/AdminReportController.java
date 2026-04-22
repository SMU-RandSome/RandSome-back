package org.smu.randsome.randsomeback.admin.report.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.report.dto.response.AdminReportDetailResponse;
import org.smu.randsome.randsomeback.admin.report.dto.response.AdminReportListItem;
import org.smu.randsome.randsomeback.admin.report.enums.AdminReportStatusFilter;
import org.smu.randsome.randsomeback.admin.report.service.AdminReportService;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminReportController extends AdminReportControllerDocs {

    private final AdminReportService adminReportService;

    @Override
    @GetMapping("/v1/admin/reports")
    public ApiResponse<List<AdminReportListItem>> getReports(
            @RequestParam(defaultValue = "PENDING") AdminReportStatusFilter statusFilter
    ) {
        List<Report> reports = adminReportService.findReportsByFilter(statusFilter);
        List<AdminReportListItem> items = reports.stream()
                .map(AdminReportListItem::from)
                .toList();

        return ApiResponse.success(items);
    }

    @Override
    @GetMapping("/v1/admin/reports/{reportId}")
    public ApiResponse<AdminReportDetailResponse> getReport(@PathVariable Long reportId) {
        Report report = adminReportService.findReport(reportId);
        long activeCount = adminReportService.countActiveReportsForMember(report.getReportedMember().getId());

        return ApiResponse.success(AdminReportDetailResponse.of(report, activeCount));
    }

    @Override
    @PostMapping("/v1/admin/reports/{reportId}/resolve")
    public ApiResponse<?> resolveReport(@PathVariable Long reportId) {
        adminReportService.resolveReport(reportId);

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/admin/reports/{reportId}/reject")
    public ApiResponse<?> rejectReport(@PathVariable Long reportId) {
        adminReportService.rejectReport(reportId);

        return ApiResponse.success();
    }

}