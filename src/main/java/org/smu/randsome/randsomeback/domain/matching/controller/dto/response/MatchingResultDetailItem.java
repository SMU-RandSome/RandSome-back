package org.smu.randsome.randsomeback.domain.matching.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;

@Schema(description = "매칭 상세 응답 DTO")
@Builder
public record MatchingResultDetailItem(
        Long id,
        String nickname,
        Gender gender,
        Mbti mbti,
        String instagramId,
        String selfIntroduction,
        String idealDescription
) {

    public static MatchingResultDetailItem from(MatchingResult result) {
        Member candidate = result.getCandidate();
        SocialProfile socialProfile = candidate.getSocialProfile();

        return MatchingResultDetailItem.builder()
                .id(result.getId())
                .nickname(candidate.getNickname())
                .gender(candidate.getGender())
                .mbti(candidate.getMbti())
                .instagramId(socialProfile.instagramId())
                .selfIntroduction(socialProfile.selfIntroduction())
                .idealDescription(socialProfile.idealDescription())
                .build();
    }

}