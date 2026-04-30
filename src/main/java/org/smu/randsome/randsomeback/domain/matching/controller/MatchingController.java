package org.smu.randsome.randsomeback.domain.matching.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.dto.request.MatchingApplyRequest;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingApplicationResponse;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingHistoryItem;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingResultDetailItem;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MatchingController extends MatchingControllerDocs {

    private final MatchingService matchingService;
    private final MemberService memberService;

    @Override
    @PostMapping("/v1/matchings")
    public ApiResponse<MatchingApplicationResponse> apply(
            @RequestBody @Valid MatchingApplyRequest request,
            @LoginMember Long memberId
    ) {
        MatchingApplication matchingApplication = matchingService.apply(request.toNewMatching(), memberId);

        return ApiResponse.success(MatchingApplicationResponse.of(matchingApplication));
    }

    @Override
    @GetMapping("/v1/matchings")
    public ApiResponse<List<MatchingHistoryItem>> findMatchings(@LoginMember Long memberId) {
        List<MatchingHistoryItem> response = matchingService.findMatchings(memberId)
                .stream()
                .map(MatchingHistoryItem::from)
                .toList();

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/v1/matchings/applications/{applicationId}")
    public ApiResponse<List<MatchingResultDetailItem>> findApplication(
            @PathVariable Long applicationId,
            @LoginMember Long memberId
    ) {
        List<MatchingResult> results = matchingService.findApplication(applicationId, memberId);

        List<Long> candidateIds = results.stream()
                .map(result -> result.getCandidate().getId())
                .toList();
        Map<Long, ProfileTags> profileTagMap = memberService.getProfileTags(candidateIds);

        List<MatchingResultDetailItem> response = results.stream()
                .map(result -> MatchingResultDetailItem.from(
                        result,
                        profileTagMap.get(result.getCandidate().getId())
                ))
                .toList();

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/v1/matchings/applications/{applicationId}/cancel")
    public ApiResponse<?> cancel(
            @PathVariable Long applicationId,
            @LoginMember Long memberId
    ) {
        matchingService.cancel(applicationId, memberId);

        return ApiResponse.success();
    }


}
