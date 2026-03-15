package org.smu.randsome.randsomeback.domain.matching.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.controller.dto.MatchingApplyRequest;
import org.smu.randsome.randsomeback.domain.matching.controller.dto.response.MatchingHistoryItem;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MatchingController extends MatchingControllerDocs {

    private final MatchingService matchingService;

    @PostMapping("/v1/matching")
    public ResponseEntity<ApiResponse<?>> apply(
            @RequestBody @Valid MatchingApplyRequest request,
            @LoginMember Long memberId
    ) {
        matchingService.apply(request.toNewMatching(), memberId);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/v1/matching/applications")
    public ResponseEntity<ApiResponse<List<MatchingHistoryItem>>> getMyApplications(
            @RequestParam(defaultValue = "PENDING", required = false) ApplicationStatus status,
            @LoginMember Long memberId
    ) {
        List<MatchingHistoryItem> response = matchingService.getMyApplications(memberId, status)
                .stream()
                .map(MatchingHistoryItem::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}