package org.smu.randsome.randsomeback.admin.candidate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.candidate.dto.request.CandidateRejectRequest;
import org.smu.randsome.randsomeback.admin.candidate.service.CandidateAdminService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateAdminController extends CandidateAdminControllerDocs {

    private final CandidateAdminService candidateAdminService;

    @Override
    @PostMapping("/v1/admin/candidates/{candidateRegistrationId}/approve")
    public ApiResponse<?> approve(@PathVariable Long candidateRegistrationId) {
        candidateAdminService.approve(candidateRegistrationId);

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/admin/candidates/{candidateRegistrationId}/reject")
    public ApiResponse<?> reject(
            @PathVariable Long candidateRegistrationId,
            @RequestBody @Valid CandidateRejectRequest request
    ) {
        candidateAdminService.reject(candidateRegistrationId, request.reason());

        return ApiResponse.success();
    }

}