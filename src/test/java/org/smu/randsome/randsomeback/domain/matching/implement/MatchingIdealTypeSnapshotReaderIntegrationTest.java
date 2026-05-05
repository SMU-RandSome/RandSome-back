package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingIdealTypeSnapshot;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingIdealTypeSnapshotJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingIdealTypeSnapshotReaderIntegrationTest extends IntegrationTestSupport {

    final MatchingIdealTypeSnapshotReader snapshotReader;
    final MatchingIdealTypeSnapshotJpaRepository snapshotJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;
    final MemberJpaRepository memberJpaRepository;

    @Test
    void 매칭_신청_ID로_이상형_스냅샷을_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var application = matchingJpaRepository.save(
                MatchingApplication.apply(member, MatchingType.IDEAL, 1)
        );
        var preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of(Mbti.ISTP)
        );
        snapshotJpaRepository.save(MatchingIdealTypeSnapshot.create(application.getId(), preference));

        // when
        var result = snapshotReader.find(application.getId());

        // then
        assertThat(result.preferredPersonalityTags()).containsExactly(PersonalityTag.ACTIVE);
        assertThat(result.preferredFaceTypeTags()).containsExactly(FaceTypeTag.PUPPY);
        assertThat(result.preferredDatingStyleTags()).containsExactly(DatingStyleTag.FREQUENT_CONTACT);
        assertThat(result.preferredMbtis()).containsExactly(Mbti.ISTP);
    }

    @Test
    void 스냅샷이_없으면_NOT_FOUND_IDEAL_TYPE_SNAPSHOT을_던진다() {
        // given
        var nonExistentApplicationId = 999L;

        // when & then
        assertThatThrownBy(() -> snapshotReader.find(nonExistentApplicationId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_IDEAL_TYPE_SNAPSHOT.getMessage());
    }

}
