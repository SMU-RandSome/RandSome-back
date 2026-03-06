package org.smu.randsome.randsomeback.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.member.controller.dto.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
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
