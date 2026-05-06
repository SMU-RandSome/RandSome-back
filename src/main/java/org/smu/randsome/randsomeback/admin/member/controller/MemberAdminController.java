package org.smu.randsome.randsomeback.admin.member.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.member.dto.request.RestrictionRequest;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.admin.member.service.MemberAdminService;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSearchCondition;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberAdminController extends MemberAdminControllerDocs {

    private final MemberAdminService memberAdminService;

    @Override
    @GetMapping("/v1/admin/members")
    public ApiResponse<PageResponse<MemberAdminResponse>> findMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<Member> response = memberAdminService.findMembers(
                new MemberSearchCondition(keyword),
                new OffsetLimit(page, size)
        );

        return ApiResponse.success(response.map(MemberAdminResponse::from));
    }

    @Override
    @GetMapping("/v1/admin/members/{memberId}")
    public ApiResponse<MemberDetailResponse> findMemberDetail(@PathVariable Long memberId) {
        return ApiResponse.success(
                memberAdminService.findMemberDetail(memberId)
        );
    }

    @Override
    @PostMapping("/v1/admin/members/{memberId}/suspensions")
    public ApiResponse<?> suspendMember(@PathVariable Long memberId, @RequestBody RestrictionRequest request) {
        memberAdminService.suspendMember(memberId, request.reason());

        return ApiResponse.success();
    }

    @Override
    @DeleteMapping("/v1/admin/members/{memberId}/suspensions")
    public ApiResponse<?> restoreMember(@PathVariable Long memberId) {
        memberAdminService.restoreMember(memberId);

        return ApiResponse.success();
    }

    @Override
    @PatchMapping("/v1/admin/members/{memberId}/roles")
    public ApiResponse<?> updateRole(@PathVariable Long memberId, @RequestParam Role role) {
        memberAdminService.updateRole(memberId, role);

        return ApiResponse.success();

    }
}
