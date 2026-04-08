package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class RandomMatchingStrategyIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MemberJpaRepository memberJpaRepository;
    final MatchingResultJpaRepository matchingResultJpaRepository;

    @Test
    void RANDOM_매칭_승인_시_반대_성별_후보자로_매칭_결과가_저장된다() {
        // given
        var maleApplicant = memberJpaRepository.save(MemberFixture.create()); // MALE, SOFTWARE
        var femaleCandidate1 = memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202212001@sangmyung.kr",
                Gender.FEMALE,
                MemberFixture.OTHER_DEPARTMENT
        ));
        var femaleCandidate2 = memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202212002@sangmyung.kr",
                Gender.FEMALE,
                MemberFixture.OTHER_DEPARTMENT
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.RANDOM)
                        .applicationCount(2)
                        .build(),
                maleApplicant.getId()
        );

        // when
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(r -> r.getCandidate().getGender())
                .containsOnly(Gender.FEMALE);
        assertThat(results).extracting(r -> r.getCandidate().getId())
                .containsExactlyInAnyOrder(femaleCandidate1.getId(), femaleCandidate2.getId());
    }

    @Test
    void 신청_인원수만큼_매칭_결과가_저장된다() {
        // given
        var applicant = memberJpaRepository.save(MemberFixture.create()); // MALE, SOFTWARE
        for (int i = 1; i <= 5; i++) {
            memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                    "20221000" + i + "@sangmyung.kr",
                    Gender.FEMALE,
                    MemberFixture.OTHER_DEPARTMENT
            ));
        }

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.RANDOM)
                        .applicationCount(3)
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // then
        assertThat(matchingResultJpaRepository.findAll()).hasSize(3);
    }

    @Test
    void 후보자가_없으면_매칭_결과가_저장되지_않는다() {
        // given: no FEMALE candidates
        var applicant = memberJpaRepository.save(MemberFixture.create()); // MALE
        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.RANDOM)
                        .applicationCount(2)
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // then
        assertThat(matchingResultJpaRepository.findAll()).isEmpty();
    }

    @Test
    void 여성_신청자는_남성_후보자와_매칭된다() {
        // given
        var femaleApplicant = memberJpaRepository.save(MemberFixture.createWithGender(
                "202212010@sangmyung.kr",
                Gender.FEMALE)
        );
        var maleCandidate = memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202212011@sangmyung.kr",
                Gender.MALE,
                MemberFixture.OTHER_DEPARTMENT)
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.RANDOM)
                        .applicationCount(1)
                        .build(),
                femaleApplicant.getId()
        );

        // when
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId()).isEqualTo(maleCandidate.getId());
        assertThat(results.getFirst().getCandidate().getGender()).isEqualTo(Gender.MALE);
    }

    @Test
    void 후보자가_신청_인원수보다_적으면_있는_만큼만_저장된다() {
        // given: 후보자 2명, 신청 인원수 5명
        var applicant = memberJpaRepository.save(MemberFixture.create());
        memberJpaRepository.save(MemberFixture.createCandidateWithDepartment("202212030@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));
        memberJpaRepository.save(MemberFixture.createCandidateWithDepartment("202212031@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.RANDOM)
                        .applicationCount(5)
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // then
        assertThat(matchingResultJpaRepository.findAll()).hasSize(2);
    }

    @Test
    void 매칭_결과에_신청서_정보가_올바르게_연결된다() {
        // given
        var applicant = memberJpaRepository.save(MemberFixture.create());
        memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202212020@sangmyung.kr",
                Gender.FEMALE,
                MemberFixture.OTHER_DEPARTMENT)
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.RANDOM)
                        .applicationCount(1)
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.approve(application.getId(), TestDateTimeUtils.now());

        // then
        var result = matchingResultJpaRepository.findAll().getFirst();
        assertThat(result.getMatchingApplication().getId()).isEqualTo(application.getId());
    }

}