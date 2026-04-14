package org.smu.randsome.randsomeback.domain.matching.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.vo.MyProfileTags;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

class IdealTypePreferenceTest extends UnitTestSupport {

    @Test
    void 이상형_태그와_프로필_태그가_모두_일치하면_3점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(3);
    }

    @Test
    void 이상형_태그와_프로필_태그_중_일부만_일치하면_해당_점수만_부여된다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.ACTIVE,
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(1); // 성격만 일치
    }

    @Test
    void 이상형_태그와_프로필_태그가_모두_불일치하면_0점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.QUIET,
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(0);
    }

    @Test
    void 성격_태그만_일치하면_1점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.ACTIVE,
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(1);
    }

    @Test
    void 얼굴상_태그만_일치하면_1점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.QUIET,
                FaceTypeTag.PUPPY,
                DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(1);
    }

    @Test
    void 연애스타일_태그만_일치하면_1점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.QUIET,
                FaceTypeTag.CAT,
                DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(1);
    }

    @Test
    void 성격과_얼굴상만_일치하면_2점이다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.MODERATE_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(2);
    }

    @Test
    void null_이상형_태그는_무시된다() {
        // given
        IdealTypePreference preference = IdealTypePreference.of(
                null,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );
        MyProfileTags tags = MyProfileTags.create(
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        );

        // when
        int score = preference.scoreAgainst(tags);

        // then
        assertThat(score).isEqualTo(2); // 성격은 무시, 얼굴상과 연애스타일만 계산
    }

}
