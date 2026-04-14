package org.smu.randsome.randsomeback.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.MyProfileTags;
import org.smu.randsome.randsomeback.domain.member.enums.CandidateRegistrationStatusView;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.enums.Role;

@Schema(
        name = "회원 프로필 조회 응답 DTO",
        description = "내 프로필 조회 시 반환되는 회원 프로필 정보입니다."
)
@Builder
public record MemberProfileResponse(
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

        @Schema(description = "학과", example = "SOFTWARE")
        String department,

        @Schema(description = "회원 역할", example = "ROLE_MEMBER")
        Role role,

        @Schema(description = "인스타그램 아이디", example = "my_insta", nullable = true)
        String instagramId,

        @Schema(description = "자기소개", example = "안녕하세요, 저는 홍길동입니다.", nullable = true)
        String selfIntroduction,

        @Schema(description = "이상형 소개", example = "성실하고 배려심 있는 사람이 좋아요.", nullable = true)
        String idealDescription,

        @Schema(description = "후보자 신청 상태", example = "NOT_APPLIED")
        CandidateRegistrationStatusView candidateRegistrationStatus,

        @Schema(description = "후보자 노출 횟수", example = "5")
        long exposureCount,

        @Schema(description = "내 성격 태그", example = "ACTIVE", nullable = true)
        PersonalityTag personalityTag,

        @Schema(description = "내 얼굴상 태그", example = "PUPPY", nullable = true)
        FaceTypeTag faceTypeTag,

        @Schema(description = "내 연애 스타일 태그", example = "EXPRESSIVE", nullable = true)
        DatingStyleTag datingStyleTag
) {

    public static MemberProfileResponse of(
            Member member,
            CandidateRegistrationStatusView candidateRegistrationStatus,
            long exposureCount
    ) {
        MyProfileTags profileTags = member.getMyProfileTags();

        return MemberProfileResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .legalName(member.getLegalName())
                .email(member.getEmail().address())
                .gender(member.getGender())
                .mbti(member.getMbti())
                .department(member.getDepartment().getDisplayName())
                .role(member.getRole())
                .instagramId(member.getSocialProfile().instagramId())
                .selfIntroduction(member.getSocialProfile().selfIntroduction())
                .idealDescription(member.getSocialProfile().idealDescription())
                .candidateRegistrationStatus(candidateRegistrationStatus)
                .exposureCount(exposureCount)
                .personalityTag(profileTags.personalityTag())
                .faceTypeTag(profileTags.faceTypeTag())
                .datingStyleTag(profileTags.datingStyleTag())
                .build();
    }

}
