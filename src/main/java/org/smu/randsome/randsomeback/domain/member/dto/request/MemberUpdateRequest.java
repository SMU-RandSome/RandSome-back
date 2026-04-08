package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.bankaccount.dto.command.UpdateBankAccount;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;

@Schema(name = "회원 프로필 수정 요청 DTO", description = "회원 프로필 수정 시 필요한 정보를 담는 DTO입니다.")
@Builder
public record MemberUpdateRequest(
        @Schema(description = "실명", example = "홍길동")
        @NotBlank(message = "실명은 필수입니다.")
        String legalName,

        @Schema(description = "MBTI", example = "ENTJ")
        @NotNull(message = "MBTI는 필수입니다.")
        Mbti mbti,

        @Schema(description = "학과", example = "SOFTWARE")
        @NotNull(message = "학과는 필수입니다.")
        Department department,

        @Schema(description = "인스타그램 아이디", example = "my_insta", nullable = true)
        String instagramId,

        @Schema(description = "자기소개", example = "안녕하세요, 저는 홍길동입니다.", nullable = true)
        String selfIntroduction,

        @Schema(description = "이상형 소개", example = "성실하고 배려심 있는 사람이 좋아요.", nullable = true)
        String idealDescription,

        @Schema(description = "은행명", example = "국민은행")
        @NotBlank(message = "은행명은 필수입니다.")
        String bankName,

        @Schema(description = "계좌번호", example = "123456789012")
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber
) {

    public UpdateBankAccount toUpdateBankAccount() {
        return UpdateBankAccount.builder()
                .bankName(bankName)
                .accountNumber(accountNumber)
                .accountHolder(legalName)
                .build();
    }

    public UpdateProfile toUpdateProfile() {
        return UpdateProfile.builder()
                .legalName(legalName)
                .mbti(mbti)
                .department(department)
                .instagramId(instagramId)
                .selfIntroduction(selfIntroduction)
                .idealDescription(idealDescription)
                .build();
    }

}