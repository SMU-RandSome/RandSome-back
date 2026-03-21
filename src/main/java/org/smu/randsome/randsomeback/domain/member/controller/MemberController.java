package org.smu.randsome.randsomeback.domain.member.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.PasswordUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.response.MemberProfileResponse;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.CandidateRegistrationStatusView;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController extends MemberControllerDocs {

    private final MemberService memberService;
    private final CandidateService candidateService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/members/sign-up")
    public ApiResponse<Long> signUp(@RequestBody @Valid MemberCreateRequest request) {
        Long memberId = memberService.create(
                request.emailVerificationToken(),
                request.toCredentials(),
                request.toBasicInfo(),
                request.toSocialProfile(),
                request.toBankAccountInfo()
        );

        return ApiResponse.success(memberId);
    }

    @Override
    @GetMapping("/v1/members")
    public ApiResponse<MemberProfileResponse> getMyProfile(@LoginMember Long memberId) {
        Member member = memberService.getMyProfile(memberId);
        Optional<RegistrationStatus> myRegistrationStatus = candidateService.getMyRegistrationStatus(memberId);

        MemberProfileResponse response = MemberProfileResponse.of(
                member,
                CandidateRegistrationStatusView.from(myRegistrationStatus)
        );

        return ApiResponse.success(response);
    }

    @Override
    @PatchMapping("/v1/members")
    public ApiResponse<?> updateProfile(
            @RequestBody @Valid MemberUpdateRequest request,
            @LoginMember Long memberId
    ) {
        memberService.updateProfile(memberId, request.toUpdateProfile());

        return ApiResponse.success();
    }

    @Override
    @PatchMapping("/v1/members/password")
    public ApiResponse<?> updatePassword(
            @RequestBody @Valid PasswordUpdateRequest request
    ) {
        memberService.updatePassword(request.newPassword(), request.emailVerificationToken(), request.email());

        return ApiResponse.success();
    }

}