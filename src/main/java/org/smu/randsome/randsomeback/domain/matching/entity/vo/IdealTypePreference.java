package org.smu.randsome.randsomeback.domain.matching.entity.vo;

import org.smu.randsome.randsomeback.domain.member.entity.vo.MyProfileTags;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

/**
 * 이상형 매칭 신청 시 신청자가 원하는 태그 조건을 나타내는 Value Object.
 * 각 카테고리별로 하나의 태그를 선택하며, 후보자 프로필과 일치하면 해당 카테고리 점수(1점)를 부여한다.
 */
public record IdealTypePreference(
        PersonalityTag preferredPersonalityTag,
        FaceTypeTag preferredFaceTypeTag,
        DatingStyleTag preferredDatingStyleTag
) {

    public static IdealTypePreference of(
            PersonalityTag preferredPersonalityTag,
            FaceTypeTag preferredFaceTypeTag,
            DatingStyleTag preferredDatingStyleTag
    ) {
        return new IdealTypePreference(preferredPersonalityTag, preferredFaceTypeTag, preferredDatingStyleTag);
    }

    /**
     * 후보자의 프로필 태그와 비교하여 카테고리별 일치 수(0~3)를 반환한다.
     * 각 카테고리에서 후보자 태그가 선호 태그와 일치하면 1점이 부여된다.
     *
     * @param candidateTags 후보자의 내 소개 태그
     * @return 일치 점수 (0~3)
     */
    public int scoreAgainst(MyProfileTags candidateTags) {
        int score = 0;
        if (preferredPersonalityTag != null && preferredPersonalityTag == candidateTags.personalityTag()) {
            score++;
        }
        if (preferredFaceTypeTag != null && preferredFaceTypeTag == candidateTags.faceTypeTag()) {
            score++;
        }
        if (preferredDatingStyleTag != null && preferredDatingStyleTag == candidateTags.datingStyleTag()) {
            score++;
        }
        return score;
    }

}
