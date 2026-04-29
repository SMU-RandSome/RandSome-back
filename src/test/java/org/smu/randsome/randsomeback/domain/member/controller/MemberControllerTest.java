package org.smu.randsome.randsomeback.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.member.dto.request.DeviceTokenSyncRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.PasswordUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.WithdrawRequest;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class MemberControllerTest extends ControllerTestSupport {

    @Test
    void 회원가입_요청이_유효하면_201을_반환한다() throws Exception {
        // given
        var request = createValidRequest();
        given(memberService.create(
                eq(request.emailVerificationToken()),
                eq(request.toCredentials()),
                eq(request.toBasicInfo()),
                eq(request.toSocialProfile()),
                eq(request.toTagsInfo())
        )).willReturn(1L);

        // when & then
        assertThat(mvcTester.post().uri("/v1/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.CREATED.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    @TestMember
    void 내_프로필_조회에_성공하면_200을_반환한다() {
        // given
        Member member = MemberFixture.create();
        given(memberService.getMyProfile(any())).willReturn(member);
        given(memberService.getProfileTag(any())).willReturn(MemberFixture.createProfileTag(member));
        given(candidateService.getMyRegistrationStatus(any())).willReturn(Optional.empty());

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.candidateRegistrationStatus", v -> v.assertThat().isEqualTo("NOT_APPLIED"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    @TestMember
    void 후보자_신청_중이면_프로필_조회_시_PENDING을_반환한다() {
        // given
        Member member = MemberFixture.create();
        given(memberService.getMyProfile(any())).willReturn(member);
        given(memberService.getProfileTag(any())).willReturn(MemberFixture.createProfileTag(member));
        given(candidateService.getMyRegistrationStatus(any())).willReturn(Optional.of(RegistrationStatus.PENDING));

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.data.candidateRegistrationStatus", v -> v.assertThat().isEqualTo("PENDING"));
    }

    @Test
    @TestMember
    void 후보자_승인_완료이면_프로필_조회_시_APPROVED를_반환한다() {
        // given
        Member member = MemberFixture.create();
        given(memberService.getMyProfile(any())).willReturn(member);
        given(memberService.getProfileTag(any())).willReturn(MemberFixture.createProfileTag(member));
        given(candidateService.getMyRegistrationStatus(any())).willReturn(Optional.of(RegistrationStatus.APPROVED));

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.data.candidateRegistrationStatus", v -> v.assertThat().isEqualTo("APPROVED"));
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() {
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 프로필_업데이트에_성공하면_200을_반환한다() throws Exception {
        // given
        var request = createValidUpdateRequest();

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.OK.value());
    }

    private MemberUpdateRequest createValidUpdateRequest() {
        return MemberUpdateRequest.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .department(Department.SOFTWARE)
                .instagramId("new_insta")
                .selfIntroduction("새 자기소개")
                .idealDescription("새 이상형")
                .personalityTag(PersonalityTag.QUIET)
                .faceTypeTag(FaceTypeTag.BEAR)
                .datingStyleTag(DatingStyleTag.EXPRESSIVE)
                .build();
    }

    @Test
    void 비밀번호_변경_요청이_유효하면_200을_반환한다() throws Exception {
        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createValidPasswordUpdateRequest())))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    private PasswordUpdateRequest createValidPasswordUpdateRequest() {
        return new PasswordUpdateRequest("password.verification.token", "202312345@sangmyung.kr", "newPassword123!");
    }

    @Test
    @TestMember
    void 디바이스_토큰_동기화_요청이_유효하면_200을_반환한다() throws Exception {
        // given
        var request = new DeviceTokenSyncRequest("fcm_device_token_12345");

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    void 디바이스_토큰_동기화_시_인증되지_않은_사용자는_403을_반환한다() throws Exception {
        // given
        var request = new DeviceTokenSyncRequest("fcm_device_token_12345");

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @TestMember
    @Test
    void 디바이스_토큰_삭제_성공시_204를_반환한다() {
        // when
        assertThat(mvcTester.delete().uri("/v1/members/devices")
                .param("deviceToken", "fcm_device_token_12345"))
                .apply(print())
                .hasStatus(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @TestMember
    void 회원_탈퇴_요청이_유효하면_204를_반환한다() throws Exception {
        // given
        var request = new WithdrawRequest("password123!");

        // when & then
        assertThat(mvcTester.delete().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 회원_탈퇴_시_인증되지_않은_사용자는_403을_반환한다() throws Exception {
        // given
        var request = new WithdrawRequest("password123!");

        // when & then
        assertThat(mvcTester.delete().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 회원_탈퇴_시_비밀번호가_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new WithdrawRequest("");

        // when & then
        assertThat(mvcTester.delete().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @TestMember
    @Test
    void 후보자_철회에_성공하면_200을_반환한다() {
        assertThat(mvcTester.post().uri("/v1/members/withdraw-candidate"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }


    @Test
    @TestMember
    void 내_통계_조회에_성공하면_200을_반환한다() {
        // given
        given(matchingService.getExposureCount(any())).willReturn(10L);
        given(matchingService.getSentApplicationCount(any())).willReturn(3L);
        given(attendanceService.getAttendanceDays(any())).willReturn(7L);

        // when & then
        assertThat(mvcTester.get().uri("/v1/members/stats"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.exposureCount", v -> v.assertThat().isEqualTo(10))
                .hasPathSatisfying("$.data.sentApplicationCount", v -> v.assertThat().isEqualTo(3))
                .hasPathSatisfying("$.data.attendanceDays", v -> v.assertThat().isEqualTo(7))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    private MemberCreateRequest createValidRequest() {
        return new MemberCreateRequest(
                "email.verification.token",
                "202312345@sangmyung.kr",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                Department.SOFTWARE,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                true,
                PersonalityTag.ACTIVE,
                FaceTypeTag.BEAR,
                DatingStyleTag.EXPRESSIVE
        );
    }

}