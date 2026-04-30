package org.smu.randsome.randsomeback.domain.matching.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

/**
 * 매칭 결과 상세 정보 응답 DTO다.
 * <br/>매칭이 완료된 신청의 후보자 정보를 상세히 표시할 때 사용된다.
 * <br/>후보자의 기본 정보, 프로필, 이상형 설명 등을 포함한다.
 */
@Schema(description = "매칭 상세 응답 DTO")
@Builder
public record MatchingResultDetailItem(
        @Schema(description = "매칭 결과 ID", example = "1")
        Long id,

        @Schema(description = "후보자 닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "후보자 성별", example = "MALE")
        Gender gender,

        @Schema(description = "후보자 MBTI", example = "INTJ")
        Mbti mbti,

        @Schema(description = "후보자 인스타그램 아이디", example = "hong_gildong")
        String instagramId,

        @Schema(description = "후보자 자기소개", example = "안녕하세요! 저는 홍길동입니다.")
        String selfIntroduction,

        @Schema(description = "후보자 이상형 설명", example = "저는 따뜻하고 이해심 많은 사람을 좋아해요.")
        String idealDescription,

        @Schema(description = "후보자 성격 태그", example = "OUTGOING")
        PersonalityTag personalityTag,

        @Schema(description = "후보자 얼굴상 태그", example = "CUTE")
        FaceTypeTag faceTypeTag,

        @Schema(description = "후보자 연애 스타일 태그", example = "SERIOUS")
        DatingStyleTag datingStyleTag,

        @Schema(description = "탈퇴 여부", example = "false")
        boolean withdrawn
) {

    /**
     * MatchingResult 엔티티와 후보자 정보를 응답 DTO로 변환한다.
     * <br/>후보자가 탈퇴한 경우 개인정보를 가리고 탈퇴 상태를 표시한다.
     * <br/>활성 후보자의 경우 기본 정보와 소셜 프로필 데이터를 조합하여 반환한다.
     *
     * @param result 매칭 결과 엔티티
     * @param profileTag 후보자 프로필 태그 (탈퇴 회원의 경우 null 가능)
     * @return 변환된 후보자 상세 정보 DTO
     */
    public static MatchingResultDetailItem from(MatchingResult result, ProfileTags profileTag) {
        Member candidate = result.getCandidate();

        if (candidate.isDeleted()) {
            return MatchingResultDetailItem.builder()
                    .id(result.getId())
                    .nickname("탈퇴한 회원")
                    .withdrawn(true)
                    .build();
        }

        SocialProfile socialProfile = candidate.getSocialProfile();

        return MatchingResultDetailItem.builder()
                .id(result.getId())
                .nickname(candidate.getNickname())
                .gender(candidate.getGender())
                .mbti(candidate.getMbti())
                .instagramId(socialProfile.instagramId())
                .selfIntroduction(socialProfile.selfIntroduction())
                .idealDescription(socialProfile.idealDescription())
                .personalityTag(profileTag.personalityTag())
                .faceTypeTag(profileTag.faceTypeTag())
                .datingStyleTag(profileTag.datingStyleTag())
                .withdrawn(false)
                .build();
    }

}