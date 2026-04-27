package org.smu.randsome.randsomeback.domain.member.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.service.AttendanceService;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.service.CandidateService;
import org.smu.randsome.randsomeback.domain.matching.service.MatchingService;
import org.smu.randsome.randsomeback.domain.member.dto.request.DeviceTokenSyncRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.PasswordUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.response.MemberProfileResponse;
import org.smu.randsome.randsomeback.domain.member.dto.response.MemberStatsResponse;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.CandidateRegistrationStatusView;
import org.smu.randsome.randsomeback.domain.member.service.MemberDeviceService;
import org.smu.randsome.randsomeback.domain.member.service.MemberService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController extends MemberControllerDocs {

    private final MemberService memberService;
    private final MemberDeviceService memberDeviceService;
    private final CandidateService candidateService;
    private final MatchingService matchingService;
    private final AttendanceService attendanceService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/members/sign-up")
    public ApiResponse<Long> signUp(@RequestBody @Valid MemberCreateRequest request) {
        Long memberId = memberService.create(
                request.emailVerificationToken(),
                request.toCredentials(),
                request.toBasicInfo(),
                request.toSocialProfile(),
                request.toTagsInfo()
        );

        return ApiResponse.success(memberId);
    }

    @Override
    @GetMapping("/v1/members")
    public ApiResponse<MemberProfileResponse> getMyProfile(@LoginMember Long memberId) {
        Member member = memberService.getMyProfile(memberId);
        MemberProfileTag profileTag = memberService.getProfileTag(memberId);
        Optional<RegistrationStatus> myRegistrationStatus = candidateService.getMyRegistrationStatus(memberId);
        long exposureCount = matchingService.getExposureCount(memberId);

        MemberProfileResponse response = MemberProfileResponse.of(
                member,
                profileTag,
                CandidateRegistrationStatusView.from(myRegistrationStatus),
                exposureCount
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

    @Override
    @PatchMapping("/v1/members/devices")
    public ApiResponse<?> syncDevices(
            @RequestBody @Valid DeviceTokenSyncRequest request,
            @LoginMember Long memberId
    ) {
        memberDeviceService.syncDeviceTokens(memberId, request.deviceToken());

        return ApiResponse.success();
    }

    @Override
    @DeleteMapping("/v1/members/devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<?> deleteDevice(
            @RequestParam String deviceToken,
            @LoginMember Long memberId
    ) {
        memberDeviceService.deleteDeviceToken(memberId, deviceToken);

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/members/stats")
    public ApiResponse<MemberStatsResponse> getMyStats(@LoginMember Long memberId) {
        long exposureCount = matchingService.getExposureCount(memberId);
        long sentApplicationCount = matchingService.getSentApplicationCount(memberId);
        long attendanceDays = attendanceService.getAttendanceDays(memberId);

        return ApiResponse.success(
                MemberStatsResponse.of(exposureCount, sentApplicationCount, attendanceDays)
        );
    }

    @PostMapping("/v1/members/withdraw-candidate")
    public ApiResponse<?> withdraw(@LoginMember Long memberId) {
        candidateService.withdraw(memberId);

        return ApiResponse.success();
    }

}