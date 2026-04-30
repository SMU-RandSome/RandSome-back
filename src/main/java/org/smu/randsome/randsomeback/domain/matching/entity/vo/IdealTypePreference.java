package org.smu.randsome.randsomeback.domain.matching.entity.vo;

import java.util.Set;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

/**
 * 이상형 매칭 신청 시 신청자가 원하는 태그 조건을 나타내는 Value Object.
 * 각 카테고리별로 여러 태그를 선택할 수 있으며, 후보자 프로필 태그가 선호 목록에 포함되면 1점이 부여된다.
 * 최대 점수: 4점 (성격 + 얼굴상 + 연애스타일 + MBTI)
 */
public record IdealTypePreference(
        Set<PersonalityTag> preferredPersonalityTags,
        Set<FaceTypeTag> preferredFaceTypeTags,
        Set<DatingStyleTag> preferredDatingStyleTags,
        Set<Mbti> preferredMbtis
) {

    public IdealTypePreference {
        preferredPersonalityTags = preferredPersonalityTags != null
                ? Set.copyOf(preferredPersonalityTags) : Set.of();
        preferredFaceTypeTags = preferredFaceTypeTags != null
                ? Set.copyOf(preferredFaceTypeTags) : Set.of();
        preferredDatingStyleTags = preferredDatingStyleTags != null
                ? Set.copyOf(preferredDatingStyleTags) : Set.of();
        preferredMbtis = preferredMbtis != null
                ? Set.copyOf(preferredMbtis) : Set.of();
    }

    public static IdealTypePreference of(
            Set<PersonalityTag> personalityTags,
            Set<FaceTypeTag> faceTypeTags,
            Set<DatingStyleTag> datingStyleTags,
            Set<Mbti> mbtis
    ) {
        return new IdealTypePreference(personalityTags, faceTypeTags, datingStyleTags, mbtis);
    }

    /**
     * 후보자의 프로필 태그 및 MBTI와 비교하여 카테고리별 일치 수(0~4)를 반환한다.
     * 각 카테고리에서 후보자 태그가 선호 목록에 포함되면 1점이 부여된다.
     * 빈 선호 목록은 해당 카테고리를 무시한다 (0점, 패널티 아님).
     *
     * @param candidateProfileTag 후보자의 프로필 태그
     * @param candidateMbti 후보자의 MBTI
     * @return 일치 점수 (0~4)
     */
    public int scoreAgainst(ProfileTags candidateProfileTag, Mbti candidateMbti) {
        int score = 0;
        if (!preferredPersonalityTags.isEmpty()
                && preferredPersonalityTags.contains(candidateProfileTag.personalityTag())) {
            score++;
        }
        if (!preferredFaceTypeTags.isEmpty()
                && preferredFaceTypeTags.contains(candidateProfileTag.faceTypeTag())) {
            score++;
        }
        if (!preferredDatingStyleTags.isEmpty()
                && preferredDatingStyleTags.contains(candidateProfileTag.datingStyleTag())) {
            score++;
        }
        if (!preferredMbtis.isEmpty()
                && preferredMbtis.contains(candidateMbti)) {
            score++;
        }
        return score;
    }

}
