package org.smu.randsome.randsomeback.domain.member.entity.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class SocialProfileTest extends UnitTestSupport {

    @Test
    void 소셜_프로필_VO를_생성한다() {
        SocialProfile profile = MemberFixture.socialProfile();

        assertThat(profile).isNotNull().extracting(
                SocialProfile::instagramId,
                SocialProfile::selfIntroduction,
                SocialProfile::idealDescription
        ).containsExactly(
                MemberFixture.DEFAULT_INSTAGRAM_ID,
                MemberFixture.DEFAULT_SELF_INTRODUCTION,
                MemberFixture.DEFAULT_IDEAL_DESCRIPTION
        );
    }

    @Test
    void 인스타그램_아이디가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> SocialProfile.create(null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 동일한_값을_가진_두_SocialProfile_VO는_동등하다() {
        SocialProfile profile1 = MemberFixture.socialProfile();
        SocialProfile profile2 = MemberFixture.socialProfile();

        assertThat(profile1).isEqualTo(profile2);
    }

    @Test
    void 다른_값을_가진_두_SocialProfile_VO는_동등하지_않다() {
        SocialProfile profile1 = SocialProfile.create("insta1", "소개", "이상형");
        SocialProfile profile2 = SocialProfile.create("insta2", "소개", "이상형");

        assertThat(profile1).isNotEqualTo(profile2);
    }

}