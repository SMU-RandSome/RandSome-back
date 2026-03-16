package org.smu.randsome.randsomeback.admin.member.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.member.controller.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.controller.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.admin.member.service.MemberAdminService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/v1/admin/members")
@RestController
public class MemberAdminController extends MemberAdminControllerDocs {

    private final MemberAdminService memberAdminService;

    @Override
    @GetMapping
    public ApiResponse<PageResponse<MemberAdminResponse>> getMembers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
                PageResponse.from(memberAdminService.getMembers(pageable))
        );
    }

    @Override
    @GetMapping("/{memberId}")
    public ApiResponse<MemberDetailResponse> getMemberDetail(@PathVariable Long memberId) {
        return ApiResponse.success(
                memberAdminService.getMemberDetail(memberId)
        );
    }

}
