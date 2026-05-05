package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.member.dto.command.UpdateProfile;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

@Schema(name = "회원 프로필 수정 요청 DTO", description = "회원 프로필 수정 시 필요한 정보를 담는 DTO입니다.")
@Builder
public record MemberUpdateRequest(
        @Schema(description = "실명", example = "홍길동")
        @Size(max = 50, message = "실명은 50자 이하여야 합니다.")
        @NotBlank(message = "실명은 필수입니다.")
        String legalName,

        @Schema(description = "MBTI", example = "ENTJ")
        @NotNull(message = "MBTI는 필수입니다.")
        Mbti mbti,

        @Schema(description = "학과", example = "SOFTWARE")
        @NotNull(message = "학과는 필수입니다.")
        Department department,

        @Schema(description = "인스타그램 아이디", example = "my_insta")
        @Size(max = 255, message = "인스타그램 아이디는 255자 이하여야 합니다.")
        @NotBlank(message = "인스타그램 아이디는 필수입니다.")
        String instagramId,

        @Schema(description = "자기소개", example = "안녕하세요, 저는 홍길동입니다.")
        @Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
        String selfIntroduction,

        @Schema(description = "이상형 소개", example = "최명재 같은 사람 말고 다 좋아요!!.")
        @Size(max = 1000, message = "이상형 소개는 1000자 이하여야 합니다.")
        String idealDescription,

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

    public UpdateProfile toUpdateProfile() {
        return UpdateProfile.builder()
                .legalName(legalName)
                .mbti(mbti)
                .department(department)
                .instagramId(instagramId)
                .selfIntroduction(selfIntroduction)
                .idealDescription(idealDescription)
                .personalityTag(personalityTag)
                .faceTypeTag(faceTypeTag)
                .datingStyleTag(datingStyleTag)
                .build();
    }

}