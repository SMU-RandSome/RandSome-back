package org.smu.randsome.randsomeback.domain.matching.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

class MatchingIdealTypeSnapshotTest extends UnitTestSupport {

    @Test
    void 스냅샷을_생성하면_이상형_조건이_저장된다() {
        // given
        var preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE, PersonalityTag.QUIET),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of(Mbti.ISTP, Mbti.ENFP)
        );

        // when
        var snapshot = MatchingIdealTypeSnapshot.create(1L, preference);

        // then
        assertThat(snapshot.getMatchingApplicationId()).isEqualTo(1L);
        assertThat(snapshot.getPreferredPersonalityTags()).containsExactlyInAnyOrder(PersonalityTag.ACTIVE, PersonalityTag.QUIET);
        assertThat(snapshot.getPreferredFaceTypeTags()).containsExactly(FaceTypeTag.PUPPY);
        assertThat(snapshot.getPreferredDatingStyleTags()).containsExactly(DatingStyleTag.FREQUENT_CONTACT);
        assertThat(snapshot.getPreferredMbtis()).containsExactlyInAnyOrder(Mbti.ISTP, Mbti.ENFP);
    }

    @Test
    void toVO로_변환하면_원본_선호_조건과_동일하다() {
        // given
        var preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE),
                Set.of(FaceTypeTag.CAT),
                Set.of(DatingStyleTag.MODERATE_CONTACT),
                Set.of(Mbti.ENFP)
        );
        var snapshot = MatchingIdealTypeSnapshot.create(1L, preference);

        // when
        var result = snapshot.toVO();

        // then
        assertThat(result.preferredPersonalityTags()).isEqualTo(preference.preferredPersonalityTags());
        assertThat(result.preferredFaceTypeTags()).isEqualTo(preference.preferredFaceTypeTags());
        assertThat(result.preferredDatingStyleTags()).isEqualTo(preference.preferredDatingStyleTags());
        assertThat(result.preferredMbtis()).isEqualTo(preference.preferredMbtis());
    }

    @Test
    void preference가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> MatchingIdealTypeSnapshot.create(1L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void matchingApplicationId가_null이면_예외가_발생한다() {
        // given
        var preference = IdealTypePreference.of(Set.of(), Set.of(), Set.of(), Set.of());

        // when & then
        assertThatThrownBy(() -> MatchingIdealTypeSnapshot.create(null, preference))
                .isInstanceOf(NullPointerException.class);
    }

}
