package org.smu.randsome.randsomeback.domain.matching.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.dto.request.MatchingApplyRequest;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingHistoryItem;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingResultDetailItem;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MatchingController extends MatchingControllerDocs {

    private final MatchingService matchingService;

    @Override
    @PostMapping("/v1/matching")
    public ApiResponse<?> apply(
            @RequestBody @Valid MatchingApplyRequest request,
            @LoginMember Long memberId
    ) {
        matchingService.apply(request.toNewMatching(), memberId);

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/matching/applications")
    public ApiResponse<List<MatchingHistoryItem>> getMyApplications(
            @RequestParam(defaultValue = "PENDING", required = false) ApplicationStatus status,
            @LoginMember Long memberId
    ) {
        List<MatchingHistoryItem> response = matchingService.getMyApplications(memberId, status)
                .stream()
                .map(MatchingHistoryItem::from)
                .toList();

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/v1/matching/applications/{applicationId}/approved")
    public ApiResponse<List<MatchingResultDetailItem>> getApprovedApplication(
            @PathVariable Long applicationId,
            @LoginMember Long memberId
    ) {
        List<MatchingResultDetailItem> response = matchingService.getApprovedApplication(applicationId, memberId)
                .stream()
                .map(MatchingResultDetailItem::from)
                .toList();

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/v1/matching/applications/{applicationId}/cancel")
    public ApiResponse<?> cancel(
            @PathVariable Long applicationId,
            @LoginMember Long memberId
    ) {
        matchingService.cancel(applicationId, memberId);

        return ApiResponse.success();
    }


}
