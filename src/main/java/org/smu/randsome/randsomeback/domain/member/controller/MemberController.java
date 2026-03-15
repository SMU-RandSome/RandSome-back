package org.smu.randsome.randsomeback.domain.member.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.controller.dto.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.controller.dto.MemberProfileResponse;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController extends MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @PostMapping("/v1/members/sign-up")
    public ResponseEntity<ApiResponse<Long>> signUp(@RequestBody @Valid MemberCreateRequest request) {
        Long memberId = memberService.create(
                request.emailVerificationToken(),
                request.toCredentials(),
                request.toBasicInfo(),
                request.toSocialProfile(),
                request.toBankAccountInfo()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(memberId));
    }

    @Override
    @GetMapping("/v1/members/me")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(@LoginMember Long memberId) {
        Member member = memberService.getMyProfile(memberId);

        return ResponseEntity.ok(ApiResponse.success(MemberProfileResponse.from(member)));
    }

}