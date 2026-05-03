package org.smu.randsome.randsomeback.domain.matching.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.dto.ProfileTags;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

class IdealTypePreferenceTest extends UnitTestSupport {

    @Test
    void 모든_카테고리가_일치하면_4점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of(Mbti.ISTP)
        );
        ProfileTags profileTag = new ProfileTags(
PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(profileTag, Mbti.ISTP);

        // then
        assertThat(score).isEqualTo(4);
    }

    @Test
    void 태그_3개만_일치하고_MBTI_불일치면_3점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of(Mbti.ENFP)
        );
        ProfileTags profileTag = new ProfileTags(
PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(profileTag, Mbti.ISTP);

        // then
        assertThat(score).isEqualTo(3);
    }

    @Test
    void 다중_선택_중_하나가_일치하면_해당_카테고리_1점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE, PersonalityTag.QUIET, PersonalityTag.AFFECTIONATE),
                Set.of(FaceTypeTag.PUPPY, FaceTypeTag.CAT),
                Set.of(),
                Set.of()
        );
        ProfileTags profileTag = new ProfileTags(
PersonalityTag.QUIET, FaceTypeTag.BEAR, DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(profileTag, Mbti.ISTP);

        // then
        assertThat(score).isEqualTo(1); // 성격만 일치
    }

    @Test
    void 모두_불일치하면_0점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of(Mbti.ENFP)
        );
        ProfileTags profileTag = new ProfileTags(
PersonalityTag.QUIET, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(profileTag, Mbti.ISTP);

        // then
        assertThat(score).isEqualTo(0);
    }

    @Test
    void 빈_선호_목록은_해당_카테고리를_무시한다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                Set.of(),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of()
        );
        ProfileTags profileTag = new ProfileTags(
PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(profileTag, Mbti.ISTP);

        // then
        assertThat(score).isEqualTo(2); // 성격, MBTI는 무시, 얼굴상+연애스타일만 계산
    }

    @Test
    void MBTI만_일치하면_1점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                Set.of(PersonalityTag.QUIET),
                Set.of(FaceTypeTag.CAT),
                Set.of(DatingStyleTag.MODERATE_CONTACT),
                Set.of(Mbti.ISTP)
        );
        ProfileTags profileTag = new ProfileTags(
PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(profileTag, Mbti.ISTP);

        // then
        assertThat(score).isEqualTo(1);
    }

}
