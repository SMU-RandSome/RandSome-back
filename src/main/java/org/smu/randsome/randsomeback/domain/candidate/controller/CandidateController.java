package org.smu.randsome.randsomeback.domain.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateController extends CandidateControllerDocs {

    private final CandidateService candidateService;

    @Override
    @PostMapping("/v1/candidate-registrations")
    public ApiResponse<?> apply(@LoginMember Long memberId) {
        candidateService.apply(memberId);

        return ApiResponse.success();
    }

}
