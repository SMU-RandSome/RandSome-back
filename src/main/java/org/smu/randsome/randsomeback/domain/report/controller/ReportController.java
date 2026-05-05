package org.smu.randsome.randsomeback.domain.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.report.dto.request.ReportCreateRequest;
import org.smu.randsome.randsomeback.domain.report.service.ReportService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ReportController extends ReportControllerDocs {

    private final ReportService reportService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/reports")
    public ApiResponse<Long> createReport(
            @RequestBody @Valid ReportCreateRequest request,
            @LoginMember Long memberId
    ) {
        Long reportId = reportService.createReport(request.toNewReport(), memberId);

        return ApiResponse.success(reportId);
    }

}