package org.smu.randsome.randsomeback.admin.candidate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.candidate.dto.request.CandidateRejectRequest;
import org.smu.randsome.randsomeback.admin.candidate.service.CandidateAdminService;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.dto.response.CandidateRegistrationItem;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateAdminController extends CandidateAdminControllerDocs {

    private final CandidateAdminService candidateAdminService;

    @Override
    @PostMapping("/v1/admin/candidate-registrations/{candidateRegistrationId}/approve")
    public ApiResponse<?> approve(@PathVariable Long candidateRegistrationId) {
        candidateAdminService.approve(candidateRegistrationId);

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/admin/candidate-registrations/{candidateRegistrationId}/reject")
    public ApiResponse<?> reject(
            @PathVariable Long candidateRegistrationId,
            @RequestBody @Valid CandidateRejectRequest request
    ) {
        candidateAdminService.reject(candidateRegistrationId, request.reason());

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/admin/candidate-registrations")
    public ApiResponse<CursorSlice<CandidateRegistrationItem>> findCandidates(
            @RequestParam(defaultValue = "PENDING") CandidateRegistrationFilter filter,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorSlice<CandidateRegistration> candidates = candidateAdminService.findCandidates(
                new CandidateRegistrationSearchCondition(filter, keyword),
                Cursor.of(lastId, size)
        );

        return ApiResponse.success(CursorSlice.of(
                candidates.items().stream()
                        .map(CandidateRegistrationItem::from)
                        .toList(),
                candidates.nextCursor(),
                candidates.hasNext()
        ));
    }

}