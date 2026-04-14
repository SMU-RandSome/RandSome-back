package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class IdealMatchingStrategyIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MemberJpaRepository memberJpaRepository;
    final MatchingResultJpaRepository matchingResultJpaRepository;

    @Test
    void 이상형_매칭_승인_시_이상형_태그와_일치하는_후보자로_매칭_결과가_저장된다() {
        // given
        var applicant = memberJpaRepository.save(createMaleApplicant());
        var highScoreCandidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212001@sangmyung.kr",
                PersonalityTag.ACTIVE,              // 이상형과 일치
                FaceTypeTag.PUPPY,                  // 이상형과 일치
                DatingStyleTag.FREQUENT_CONTACT     // 이상형과 일치
        ));
        var lowScoreCandidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212002@sangmyung.kr",
                PersonalityTag.QUIET,
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(1)
                        .idealTypePreference(IdealTypePreference.of(
                                PersonalityTag.ACTIVE,
                                FaceTypeTag.PUPPY,
                                DatingStyleTag.FREQUENT_CONTACT
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.executeMatching(application, TestDateTimeUtils.now());

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId()).isEqualTo(highScoreCandidate.getId());
    }

    @Test
    void 동일_점수의_후보자_중_무작위로_선택된다() {
        // given
        var applicant = memberJpaRepository.save(createMaleApplicant());
        var candidate1 = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212003@sangmyung.kr",
                PersonalityTag.ACTIVE,              // 일치
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        ));
        var candidate2 = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212004@sangmyung.kr",
                PersonalityTag.ACTIVE,              // 일치
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(1)
                        .idealTypePreference(IdealTypePreference.of(
                                PersonalityTag.ACTIVE,
                                FaceTypeTag.PUPPY,
                                DatingStyleTag.FREQUENT_CONTACT
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.executeMatching(application, TestDateTimeUtils.now());

        // then: 둘 다 점수가 1점으로 동일하므로 둘 중 하나가 선택됨
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId())
                .isIn(candidate1.getId(), candidate2.getId());
    }

    @Test
    void 부분_일치하는_후보자는_낮은_점수로_평가된다() {
        // given
        var applicant = memberJpaRepository.save(createMaleApplicant());
        var fullMatchCandidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212005@sangmyung.kr",
                PersonalityTag.ACTIVE,              // 일치 (1점)
                FaceTypeTag.PUPPY,                  // 일치 (1점)
                DatingStyleTag.FREQUENT_CONTACT     // 일치 (1점)
        ));
        var partialMatchCandidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212006@sangmyung.kr",
                PersonalityTag.ACTIVE,              // 일치 (1점)
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(1)
                        .idealTypePreference(IdealTypePreference.of(
                                PersonalityTag.ACTIVE,
                                FaceTypeTag.PUPPY,
                                DatingStyleTag.FREQUENT_CONTACT
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.executeMatching(application, TestDateTimeUtils.now());

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId()).isEqualTo(fullMatchCandidate.getId());
    }

    @Test
    void 신청_인원수만큼_점수_높은_순서대로_매칭_결과가_저장된다() {
        // given
        var applicant = memberJpaRepository.save(createMaleApplicant());
        var score3Candidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212007@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        ));
        var score2Candidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212008@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.MODERATE_CONTACT
        ));
        var score1Candidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212009@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(2)
                        .idealTypePreference(IdealTypePreference.of(
                                PersonalityTag.ACTIVE,
                                FaceTypeTag.PUPPY,
                                DatingStyleTag.FREQUENT_CONTACT
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.executeMatching(application, TestDateTimeUtils.now());

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(2);
        var candidateIds = results.stream()
                .map(r -> r.getCandidate().getId())
                .toList();
        assertThat(candidateIds).contains(score3Candidate.getId(), score2Candidate.getId());
        assertThat(candidateIds).doesNotContain(score1Candidate.getId());
    }

    @Test
    void 후보자가_신청_인원수보다_적으면_있는_만큼만_저장된다() {
        // given
        var applicant = memberJpaRepository.save(createMaleApplicant());
        memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212010@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.EXPRESSIVE
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(5)
                        .idealTypePreference(IdealTypePreference.of(
                                PersonalityTag.ACTIVE,
                                FaceTypeTag.PUPPY,
                                DatingStyleTag.EXPRESSIVE
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.executeMatching(application, TestDateTimeUtils.now());

        // then
        assertThat(matchingResultJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void 신청_인원수보다_많은_후보자_중_상위만_선택된다() {
        // given
        var applicant = memberJpaRepository.save(createMaleApplicant());
        var score3Candidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212011@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.FREQUENT_CONTACT
        ));
        var score2Candidate1 = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212012@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.MODERATE_CONTACT
        ));
        var score2Candidate2 = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212013@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.MODERATE_CONTACT
        ));
        var score1Candidate = memberJpaRepository.save(createFemaleCandidateWithTags(
                "202212014@sangmyung.kr",
                PersonalityTag.ACTIVE,
                FaceTypeTag.CAT,
                DatingStyleTag.MODERATE_CONTACT
        ));

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(2)
                        .idealTypePreference(IdealTypePreference.of(
                                PersonalityTag.ACTIVE,
                                FaceTypeTag.PUPPY,
                                DatingStyleTag.FREQUENT_CONTACT
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingManager.executeMatching(application, TestDateTimeUtils.now());

        // then: 3점(1명) + 2점(2명) + 1점(1명) 중 신청인원수(2명)만큼 상위를 선택
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(2);
        var candidateIds = results.stream()
                .map(r -> r.getCandidate().getId())
                .toList();
        assertThat(candidateIds).contains(score3Candidate.getId());
        // 2점 동점자(2명) 중 하나 포함
        assertThat(candidateIds).containsAnyOf(score2Candidate1.getId(), score2Candidate2.getId());
        // 1점 후보자는 포함되지 않음
        assertThat(candidateIds).doesNotContain(score1Candidate.getId());
    }

    // Helper methods
    private Member createMaleApplicant() {
        return Member.create(
                "202312345@sangmyung.kr",
                "password123!",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(),
                "홍길동",
                Gender.MALE,
                org.smu.randsome.randsomeback.domain.member.enums.Mbti.ISTP,
                org.smu.randsome.randsomeback.domain.member.enums.Department.SOFTWARE,
                "insta_id",
                "안녕하세요",
                "착한 사람",
                PersonalityTag.ACTIVE,
                FaceTypeTag.PUPPY,
                DatingStyleTag.EXPRESSIVE
        );
    }

    private Member createFemaleCandidateWithTags(
            String email,
            PersonalityTag personalityTag,
            FaceTypeTag faceTypeTag,
            DatingStyleTag datingStyleTag
    ) {
        Member candidate = Member.create(
                email,
                "password123!",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(),
                "김미영",
                Gender.FEMALE,
                org.smu.randsome.randsomeback.domain.member.enums.Mbti.ENFP,
                org.smu.randsome.randsomeback.domain.member.enums.Department.ELECTRONICS_ENGINEERING,
                email.substring(0, email.indexOf('@')),
                "안녕하세요",
                "좋은 사람",
                personalityTag,
                faceTypeTag,
                datingStyleTag
        );
        candidate.updateRole(org.smu.randsome.randsomeback.domain.member.enums.Role.ROLE_CANDIDATE);
        return candidate;
    }

}
