package org.smu.randsome.randsomeback.admin.member.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.smu.randsome.randsomeback.domain.bankaccount.BankAccount;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.Role;

@Schema(name = "관리자 회원 상세 조회 응답", description = "관리자가 회원 상세 조회 시 반환되는 정보입니다.")
public record MemberDetailResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "randsomeUser")
        String nickname,

        @Schema(description = "실명", example = "홍길동")
        String legalName,

        @Schema(description = "이메일", example = "20211233@sangmyung.kr")
        String email,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @Schema(description = "MBTI", example = "ENTJ")
        Mbti mbti,

        @Schema(description = "회원 역할", example = "ROLE_MEMBER")
        Role role,

        @Schema(description = "인스타그램 아이디", example = "my_insta", nullable = true)
        String instagramId,

        @Schema(description = "자기소개", example = "안녕하세요, 저는 홍길동입니다.", nullable = true)
        String selfIntroduction,

        @Schema(description = "이상형 소개", example = "성실하고 배려심 있는 사람이 좋아요.", nullable = true)
        String idealDescription,

        @Schema(description = "은행 이름", example = "국민은행")
        String bankName,

        @Schema(description = "계좌 번호", example = "123456789012")
        String accountNumber
) {

    public static MemberDetailResponse from(Member member, BankAccount bankAccount) {
        return new MemberDetailResponse(
                member.getId(),
                member.getNickname(),
                member.getLegalName(),
                member.getEmail().address(),
                member.getGender(),
                member.getMbti(),
                member.getRole(),
                member.getSocialProfile().instagramId(),
                member.getSocialProfile().selfIntroduction(),
                member.getSocialProfile().idealDescription(),
                bankAccount.getBankName(),
                bankAccount.getAccountNumber()
        );
    }

}
