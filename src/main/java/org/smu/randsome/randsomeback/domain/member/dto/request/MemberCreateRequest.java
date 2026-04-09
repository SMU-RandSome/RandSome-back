package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.smu.randsome.randsomeback.domain.bankaccount.dto.command.BankAccountInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberBasicInfo;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberCredentials;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberSocialProfile;
import org.smu.randsome.randsomeback.domain.member.dto.command.MemberTagsInfo;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

@Schema(description = "회원 가입 요청 DTO")
public record MemberCreateRequest(
        @Schema(description = "이메일 인증 토큰", example = "abc123def456")
        @NotBlank(message = "이메일 인증 토큰은 필수입니다.")
        String emailVerificationToken,

        @Schema(description = "이메일", example = "20211233@sangmyung.kr")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Pattern(regexp = ".+@sangmyung\\.kr$", message = "상명대학교 이메일(@sangmyung.kr)만 사용 가능합니다.")
        String email,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String password,

        @Schema(description = "실명", example = "홍길동")
        @NotBlank(message = "실명은 필수입니다.")
        @Size(max = 50, message = "실명은 50자 이하여야 합니다.")
        String legalName,

        @Schema(description = "성별", example = "MALE")
        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @Schema(description = "MBTI", example = "ENTJ")
        @NotNull(message = "MBTI는 필수입니다.")
        Mbti mbti,

        @Schema(description = "학과", example = "SOFTWARE")
        @NotNull(message = "학과는 필수입니다.")
        Department department,

        @Schema(description = "인스타그램 아이디", example = "my_insta")
        @Size(max = 255, message = "인스타그램 아이디는 255자 이하여야 합니다.")
        String instagramId,

        @Schema(description = "자기소개", example = "안녕하세요, 저는 홍길동입니다.")
        @Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
        String selfIntroduction,

        @Schema(description = "이상형 소개", example = "최명재 같은 사람 말고 다 좋아요!!.")
        @Size(max = 1000, message = "이상형 소개는 1000자 이하여야 합니다.")
        String idealDescription,

        @Schema(description = "약관 전체 동의 여부", example = "true")
        @AssertTrue(message = "약관에 동의해야 합니다.")
        boolean agreedToTerms,

        @Schema(description = "은행명", example = "국민은행")
        @NotBlank(message = "은행명은 필수입니다.")
        String bankName,

        @Schema(description = "계좌번호", example = "123456789012")
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber,

        @Schema(description = "내 성격 태그", example = "ACTIVE")
        @NotNull(message = "성격 태그는 필수입니다.")
        PersonalityTag personalityTag,

        @Schema(description = "내 얼굴상 태그", example = "PUPPY")
        @NotNull(message = "얼굴상 태그는 필수입니다.")
        FaceTypeTag faceTypeTag,

        @Schema(description = "내 연애 스타일 태그", example = "EXPRESSIVE")
        @NotNull(message = "연애 스타일 태그는 필수입니다.")
        DatingStyleTag datingStyleTag
) {

    public MemberCredentials toCredentials() {
        return new MemberCredentials(email, password);
    }

    public MemberBasicInfo toBasicInfo() {
        return new MemberBasicInfo(legalName, gender, mbti, department);
    }

    public MemberSocialProfile toSocialProfile() {
        return new MemberSocialProfile(instagramId, selfIntroduction, idealDescription);
    }

    public BankAccountInfo toBankAccountInfo() {
        return new BankAccountInfo(bankName, accountNumber, legalName);
    }

    public MemberTagsInfo toTagsInfo() {
        return new MemberTagsInfo(personalityTag, faceTypeTag, datingStyleTag);
    }

}