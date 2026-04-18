package org.smu.randsome.randsomeback.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.member.dto.request.RestrictionRequest;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class MemberAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_회원_목록_조회에_성공하면_200을_반환한다() {
        // given
        var response = new MemberAdminResponse(
                1L,
                "nickname",
                "홍길동",
                Gender.MALE,
                Mbti.INTJ,
                Role.ROLE_MEMBER
        );

        given(memberAdminService.getMembers(any()))
                .willReturn(new PageImpl<>(List.of(response)));

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/members"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPath("$.data.content");

        then(memberAdminService).should().getMembers(any());
    }

    @TestAdmin
    @Test
    void 관리자가_회원_상세_조회에_성공하면_200을_반환한다() {
        // given
        var member = MemberFixture.create();
        var response = MemberDetailResponse.of(member);

        given(memberAdminService.getMemberDetail(1L)).willReturn(response);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/members/1"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.id", v -> v.assertThat().isEqualTo(response.id()));

        then(memberAdminService).should().getMemberDetail(1L);
    }

    @TestAdmin
    @Test
    void 관리자가_회원을_제한한다() throws JsonProcessingException {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/members/{memberId}/suspensions", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RestrictionRequest("부적절한 행동"))))
                .apply(print())
                .hasStatusOk();

        verify(memberAdminService).suspendMember(any(), any());
    }

}