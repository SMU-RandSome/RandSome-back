package org.smu.randsome.randsomeback.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.fixture.BankAccountFixture;
import org.smu.randsome.randsomeback.domain.member.dto.request.DeviceTokenSyncRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.PasswordUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
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
                eq(request.toBankAccountInfo())
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
    void 회원가입_요청_이메일이_상명대_이메일이_아니면_400을_반환한다() throws Exception {
        // given
        var request = new MemberCreateRequest(
                "email.verification.token",
                "student@gmail.com",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                true,
                "국민은행",
                "123456789012"
        );

        // when & then
        assertThat(mvcTester.post().uri("/v1/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 회원가입_요청_이메일_인증_토큰이_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new MemberCreateRequest(
                "",
                "202312345@sangmyung.kr",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                true,
                "국민은행",
                "123456789012"
        );

        // when & then
        assertThat(mvcTester.post().uri("/v1/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 약관에_동의하지_않으면_400을_반환한다() throws Exception {
        // given
        var request = new MemberCreateRequest(
                "email.verification.token",
                "202312345@sangmyung.kr",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                false,
                "국민은행",
                "123456789012"
        );

        // when & then
        assertThat(mvcTester.post().uri("/v1/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 은행명이_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new MemberCreateRequest(
                "email.verification.token",
                "202312345@sangmyung.kr",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                true,
                "",
                "123456789012"
        );

        // when & then
        assertThat(mvcTester.post().uri("/v1/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 계좌번호가_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new MemberCreateRequest(
                "email.verification.token",
                "202312345@sangmyung.kr",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                true,
                "국민은행",
                ""
        );

        // when & then
        assertThat(mvcTester.post().uri("/v1/members/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 내_프로필_조회에_성공하면_200을_반환한다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("남자#ABC12345");
        given(member.getLegalName()).willReturn(MemberFixture.DEFAULT_LEGAL_NAME);
        given(member.getEmail()).willReturn(MemberFixture.email());
        given(member.getGender()).willReturn(MemberFixture.DEFAULT_GENDER);
        given(member.getMbti()).willReturn(MemberFixture.DEFAULT_MBTI);
        given(member.getRole()).willReturn(Role.ROLE_MEMBER);
        given(member.getSocialProfile()).willReturn(MemberFixture.socialProfile());
        given(memberService.getMyProfile(any())).willReturn(member);
        given(bankAccountService.findByMemberId(any())).willReturn(BankAccountFixture.create());
        given(candidateService.getMyRegistrationStatus(any())).willReturn(Optional.empty());

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.id", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.data.bankName", v -> v.assertThat().isEqualTo(BankAccountFixture.DEFAULT_BANK_NAME))
                .hasPathSatisfying("$.data.accountNumber", v -> v.assertThat().isEqualTo(BankAccountFixture.DEFAULT_ACCOUNT_NUMBER))
                .hasPathSatisfying("$.data.candidateRegistrationStatus", v -> v.assertThat().isEqualTo("NOT_APPLIED"))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    @TestMember
    void 후보자_신청_중이면_프로필_조회_시_PENDING을_반환한다() {
        // given
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("남자#ABC12345");
        given(member.getLegalName()).willReturn(MemberFixture.DEFAULT_LEGAL_NAME);
        given(member.getEmail()).willReturn(MemberFixture.email());
        given(member.getGender()).willReturn(MemberFixture.DEFAULT_GENDER);
        given(member.getMbti()).willReturn(MemberFixture.DEFAULT_MBTI);
        given(member.getRole()).willReturn(Role.ROLE_MEMBER);
        given(member.getSocialProfile()).willReturn(MemberFixture.socialProfile());
        given(memberService.getMyProfile(any())).willReturn(member);
        given(bankAccountService.findByMemberId(any())).willReturn(BankAccountFixture.create());
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
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        given(member.getNickname()).willReturn("남자#ABC12345");
        given(member.getLegalName()).willReturn(MemberFixture.DEFAULT_LEGAL_NAME);
        given(member.getEmail()).willReturn(MemberFixture.email());
        given(member.getGender()).willReturn(MemberFixture.DEFAULT_GENDER);
        given(member.getMbti()).willReturn(MemberFixture.DEFAULT_MBTI);
        given(member.getRole()).willReturn(Role.ROLE_MEMBER);
        given(member.getSocialProfile()).willReturn(MemberFixture.socialProfile());
        given(memberService.getMyProfile(any())).willReturn(member);
        given(bankAccountService.findByMemberId(any())).willReturn(BankAccountFixture.create());
        given(candidateService.getMyRegistrationStatus(any())).willReturn(Optional.of(RegistrationStatus.APPROVED));

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.data.candidateRegistrationStatus", v -> v.assertThat().isEqualTo("APPROVED"));
    }

    @Test
    @TestMember
    void 계좌가_없는_회원이면_프로필_조회_시_404를_반환한다() {
        // given
        Member member = mock(Member.class);
        given(memberService.getMyProfile(any())).willReturn(member);
        willThrow(new CoreException(ErrorType.NOT_FOUND_BANK_ACCOUNT))
                .given(bankAccountService).findByMemberId(any());

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_BANK_ACCOUNT.getMessage()));
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() {
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @TestMember
    void 존재하지_않는_회원이면_404를_반환한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberService).getMyProfile(any());

        // when & then
        assertThat(mvcTester.get().uri("/v1/members"))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_MEMBER.getMessage()));
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

    @Test
    @TestMember
    void 프로필_업데이트_시_실명이_없으면_400을_반환한다() throws Exception {
        // given
        var request = MemberUpdateRequest.builder()
                .legalName("")
                .mbti(Mbti.ENFP)
                .build();

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 프로필_업데이트_시_MBTI가_없으면_400을_반환한다() throws Exception {
        // given
        var request = MemberUpdateRequest.builder()
                .legalName("김철수")
                .mbti(null)
                .build();

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 프로필_업데이트_시_존재하지_않는_회원이면_404를_반환한다() throws Exception {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberService).updateProfile(any(), any(), any());

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createValidUpdateRequest())))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_MEMBER.getMessage()));
    }

    @Test
    @TestMember
    void 프로필_수정_시_은행명이_비어있으면_400을_반환한다() throws Exception {
        var request = MemberUpdateRequest.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .bankName("")
                .accountNumber("123456789012")
                .build();

        assertThat(mvcTester.patch().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @TestMember
    void 프로필_수정_시_계좌번호가_비어있으면_400을_반환한다() throws Exception {
        var request = MemberUpdateRequest.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .bankName("국민은행")
                .accountNumber("")
                .build();

        assertThat(mvcTester.patch().uri("/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    private MemberUpdateRequest createValidUpdateRequest() {
        return MemberUpdateRequest.builder()
                .legalName("김철수")
                .mbti(Mbti.ENFP)
                .instagramId("new_insta")
                .selfIntroduction("새 자기소개")
                .idealDescription("새 이상형")
                .bankName("국민은행")
                .accountNumber("123456789012")
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

    @Test
    void 비밀번호_변경_시_인증_토큰이_비어있으면_400을_반환한다() throws Exception {
        var request = new PasswordUpdateRequest("", "202312345@sangmyung.kr", "newPassword123!");

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 비밀번호_변경_시_이메일이_비어있으면_400을_반환한다() throws Exception {
        var request = new PasswordUpdateRequest("password.verification.token", "", "newPassword123!");

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 비밀번호_변경_시_이메일이_상명대_이메일이_아니면_400을_반환한다() throws Exception {
        var request = new PasswordUpdateRequest("password.verification.token", "student@gmail.com", "newPassword123!");

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 비밀번호_변경_시_새_비밀번호가_비어있으면_400을_반환한다() throws Exception {
        var request = new PasswordUpdateRequest("password.verification.token", "202312345@sangmyung.kr", "");

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 비밀번호_변경_시_새_비밀번호가_8자_미만이면_400을_반환한다() throws Exception {
        var request = new PasswordUpdateRequest("password.verification.token", "202312345@sangmyung.kr", "short1!");

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 비밀번호_변경_시_이메일_인증_토큰이_유효하지_않으면_400을_반환한다() throws Exception {
        willThrow(new CoreException(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST))
                .given(memberService).updatePassword(any(), any(), any());

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createValidPasswordUpdateRequest())))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.INVALID_PASSWORD_UPDATE_REQUEST.getMessage()));
    }

    @Test
    void 비밀번호_변경_시_존재하지_않는_회원이면_404를_반환한다() throws Exception {
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberService).updatePassword(any(), any(), any());

        assertThat(mvcTester.patch().uri("/v1/members/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createValidPasswordUpdateRequest())))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_MEMBER.getMessage()));
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
    @TestMember
    void 디바이스_토큰이_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new DeviceTokenSyncRequest("");

        // when & then
        assertThat(mvcTester.patch().uri("/v1/members/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
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

    @TestMember
    @Test
    void 승인된_후보자가_없으면_철회_시_404를_반환한다() {
        willThrow(new CoreException(ErrorType.NOT_FOUND_CANDIDATE))
                .given(candidateService).withdraw(any());

        assertThat(mvcTester.post().uri("/v1/members/withdraw-candidate"))
                .apply(print())
                .hasStatus(HttpStatus.NOT_FOUND.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_FOUND_CANDIDATE.getMessage()));
    }

    @TestMember
    @Test
    void 승인되지_않은_상태에서_철회하면_400을_반환한다() {
        willThrow(new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED))
                .given(candidateService).withdraw(any());

        assertThat(mvcTester.post().uri("/v1/members/withdraw-candidate"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"))
                .hasPathSatisfying("$.error.message", v -> v.assertThat().isEqualTo(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED.getMessage()));
    }

    private MemberCreateRequest createValidRequest() {
        return new MemberCreateRequest(
                "email.verification.token",
                "202312345@sangmyung.kr",
                "password123!",
                "홍길동",
                Gender.MALE,
                Mbti.ISTP,
                "my_insta",
                "안녕하세요",
                "착한 사람",
                true,
                "국민은행",
                "123456789012"
        );
    }

}