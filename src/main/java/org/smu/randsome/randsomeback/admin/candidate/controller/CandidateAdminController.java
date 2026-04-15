package org.smu.randsome.randsomeback.admin.candidate.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.candidate.service.CandidateAdminService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateAdminController {

    private final CandidateAdminService candidateAdminService;

    @PostMapping("/v1/admin/candidates/{candidateRegistrationId}/approve")
    public ApiResponse<?> approve(@PathVariable Long candidateRegistrationId) {
        candidateAdminService.approve(candidateRegistrationId);

        return ApiResponse.success();
    }

}