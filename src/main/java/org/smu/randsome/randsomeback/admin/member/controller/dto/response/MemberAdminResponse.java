package org.smu.randsome.randsomeback.admin.member.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;

@Schema(
        name = "관리자 회원 조회 응답 DTO",
        description = "관리자가 회원 목록 조회 시 반환되는 DTO입니다."
)
public record MemberAdminResponse(

        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "randsomeUser")
        String nickname,

        @Schema(description = "실명", example = "홍길동")
        String legalName,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @Schema(description = "MBTI", example = "INTJ")
        Mbti mbti,

        @Schema(description = "회원 역할", example = "ROLE_MEMBER")
        Role role
) {

    public static MemberAdminResponse from(Member member) {
        return new MemberAdminResponse(
                member.getId(),
                member.getNickname(),
                member.getLegalName(),
                member.getGender(),
                member.getMbti(),
                member.getRole()
        );
    }

}