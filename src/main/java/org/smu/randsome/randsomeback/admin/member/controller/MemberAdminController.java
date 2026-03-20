package org.smu.randsome.randsomeback.admin.member.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.admin.member.service.MemberAdminService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberAdminController extends MemberAdminControllerDocs {

    private final MemberAdminService memberAdminService;

    @Override
    @GetMapping("/v1/admin/members")
    public ApiResponse<PageResponse<MemberAdminResponse>> getMembers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
                PageResponse.from(memberAdminService.getMembers(pageable))
        );
    }

    @Override
    @GetMapping("/v1/admin/members/{memberId}")
    public ApiResponse<MemberDetailResponse> getMemberDetail(@PathVariable Long memberId) {
        return ApiResponse.success(
                memberAdminService.getMemberDetail(memberId)
        );
    }

    @Override
    @GetMapping("/v1/admin/members/search")
    public ApiResponse<PageResponse<MemberAdminResponse>> searchMembers(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
                PageResponse.from(memberAdminService.searchMembers(query, pageable))
        );
    }

}
