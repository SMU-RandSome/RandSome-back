package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
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
class MatchingManagerIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MatchingExecutor matchingExecutor;
    final MemberJpaRepository memberJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;
    final MatchingIdealTypeSnapshotJpaRepository snapshotJpaRepository;

    @Test
    void 매칭_신청을_저장하면_PENDING_상태로_DB에_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        var result = matchingManager.apply(newMatching, member.getId());

        // then
        var saved = matchingJpaRepository.findById(result.getId()).orElseThrow();
        assertThat(saved).extracting(
                MatchingApplication::getId,
                MatchingApplication::getMember,
                MatchingApplication::getMatchingType,
                MatchingApplication::getApplicationCount,
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getCompletedAt
        ).containsExactly(
                result.getId(),
                member,
                MatchingType.RANDOM,
                3,
                ApplicationStatus.PENDING,
                null
        );
    }

    @Test
    void 동일_파라미터로_5초_내_재신청하면_TOO_MANY_MATCHING_REQUESTS를_던진다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();
        matchingManager.apply(newMatching, member.getId());


        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.TOO_MANY_MATCHING_REQUESTS.getMessage());
    }

    @Test
    void 인원수가_다르면_같은_타입으로_재신청해도_정상_처리된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        matchingManager.apply(NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build(), member.getId());

        // when
        var result = matchingManager.apply(NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(5)
                .build(), member.getId());

        // then
        assertThat(result.getApplicationCount()).isEqualTo(5);
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void 존재하지_않는_회원이면_NOT_FOUND_MEMBER를_던진다() {
        // given
        var nonExistentMemberId = 999L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.IDEAL)
                .applicationCount(2)
                .build();

        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, nonExistentMemberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 이상형_매칭_신청_시_스냅샷이_별도로_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var preference = IdealTypePreference.of(
                Set.of(PersonalityTag.ACTIVE),
                Set.of(FaceTypeTag.PUPPY),
                Set.of(DatingStyleTag.FREQUENT_CONTACT),
                Set.of(Mbti.ISTP)
        );
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.IDEAL)
                .applicationCount(2)
                .idealTypePreference(preference)
                .build();

        // when
        var result = matchingManager.apply(newMatching, member.getId());

        // then
        var snapshot = snapshotJpaRepository.findByMatchingApplicationId(result.getId());
        assertThat(snapshot).isPresent();

        var savedPreference = snapshot.get().toVO();
        assertThat(savedPreference.preferredPersonalityTags()).containsExactly(PersonalityTag.ACTIVE);
        assertThat(savedPreference.preferredFaceTypeTags()).containsExactly(FaceTypeTag.PUPPY);
        assertThat(savedPreference.preferredDatingStyleTags()).containsExactly(DatingStyleTag.FREQUENT_CONTACT);
        assertThat(savedPreference.preferredMbtis()).containsExactly(Mbti.ISTP);
    }

    @Test
    void 랜덤_매칭_신청_시_스냅샷은_저장되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        // when
        var result = matchingManager.apply(newMatching, member.getId());

        // then
        var snapshot = snapshotJpaRepository.findByMatchingApplicationId(result.getId());
        assertThat(snapshot).isEmpty();
    }


}