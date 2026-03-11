package org.smu.randsome.randsomeback.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.member.controller.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

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

    @TestMember
    @Test
    void 일반_회원이_관리자_회원_API를_호출하면_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/members"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());

        then(memberAdminService).shouldHaveNoInteractions();
    }

}