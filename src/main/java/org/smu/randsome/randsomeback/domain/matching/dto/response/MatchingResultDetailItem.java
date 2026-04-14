package org.smu.randsome.randsomeback.domain.matching.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.vo.SocialProfile;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;

/**
 * 매칭 결과 상세 정보 응답 DTO다.
 * <br/>매칭이 완료된 신청의 후보자 정보를 상세히 표시할 때 사용된다.
 * <br/>후보자의 기본 정보, 프로필, 이상형 설명 등을 포함한다.
 */
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

    /**
     * MatchingResult 엔티티와 후보자 정보를 응답 DTO로 변환한다.
     * <br/>후보자의 기본 정보와 소셜 프로필 데이터를 조합하여 반환한다.
     *
     * @param result 매칭 결과 엔티티
     * @return 변환된 후보자 상세 정보 DTO
     */
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