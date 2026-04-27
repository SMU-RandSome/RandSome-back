package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingExecutor;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class IdealMatchingStrategyIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MatchingExecutor matchingExecutor;
    final MemberJpaRepository memberJpaRepository;
    final MemberProfileTagJpaRepository memberProfileTagJpaRepository;
    final MatchingResultJpaRepository matchingResultJpaRepository;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Test
    void 이상형_매칭_승인_시_이상형_태그와_일치하는_후보자로_매칭_결과가_저장된다() {
        // given
        var applicant = saveMemberWithTags(
                "202312345@sangmyung.kr", Gender.MALE,
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );
        var highScoreCandidate = saveCandidateWithTags(
                "202212001@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );
        var lowScoreCandidate = saveCandidateWithTags(
                "202212002@sangmyung.kr",
                PersonalityTag.QUIET, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(1)
                        .idealTypePreference(IdealTypePreference.of(
                                Set.of(PersonalityTag.ACTIVE), Set.of(FaceTypeTag.PUPPY), Set.of(DatingStyleTag.FREQUENT_CONTACT), Set.of()
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingExecutor.execute(application);

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId()).isEqualTo(highScoreCandidate.getId());
    }

    @Test
    void 동일_점수의_후보자_중_무작위로_선택된다() {
        // given
        var applicant = saveMemberWithTags(
                "202312345@sangmyung.kr", Gender.MALE,
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );
        var candidate1 = saveCandidateWithTags(
                "202212003@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );
        var candidate2 = saveCandidateWithTags(
                "202212004@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(1)
                        .idealTypePreference(IdealTypePreference.of(
                                Set.of(PersonalityTag.ACTIVE), Set.of(FaceTypeTag.PUPPY), Set.of(DatingStyleTag.FREQUENT_CONTACT), Set.of()
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingExecutor.execute(application);

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId())
                .isIn(candidate1.getId(), candidate2.getId());
    }

    @Test
    void 부분_일치하는_후보자는_낮은_점수로_평가된다() {
        // given
        var applicant = saveMemberWithTags(
                "202312345@sangmyung.kr", Gender.MALE,
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );
        var fullMatchCandidate = saveCandidateWithTags(
                "202212005@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );
        var partialMatchCandidate = saveCandidateWithTags(
                "202212006@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(1)
                        .idealTypePreference(IdealTypePreference.of(
                                Set.of(PersonalityTag.ACTIVE), Set.of(FaceTypeTag.PUPPY), Set.of(DatingStyleTag.FREQUENT_CONTACT), Set.of()
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingExecutor.execute(application);

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getCandidate().getId()).isEqualTo(fullMatchCandidate.getId());
    }

    @Test
    void 신청_인원수만큼_점수_높은_순서대로_매칭_결과가_저장된다() {
        // given
        var applicant = saveMemberWithTags(
                "202312345@sangmyung.kr", Gender.MALE,
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );
        var score3Candidate = saveCandidateWithTags(
                "202212007@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );
        var score2Candidate = saveCandidateWithTags(
                "202212008@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.MODERATE_CONTACT
        );
        var score1Candidate = saveCandidateWithTags(
                "202212009@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(2)
                        .idealTypePreference(IdealTypePreference.of(
                                Set.of(PersonalityTag.ACTIVE), Set.of(FaceTypeTag.PUPPY), Set.of(DatingStyleTag.FREQUENT_CONTACT), Set.of()
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingExecutor.execute(application);

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
        var applicant = saveMemberWithTags(
                "202312345@sangmyung.kr", Gender.MALE,
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );
        saveCandidateWithTags(
                "202212010@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(5)
                        .idealTypePreference(IdealTypePreference.of(
                                Set.of(PersonalityTag.ACTIVE), Set.of(FaceTypeTag.PUPPY), Set.of(DatingStyleTag.EXPRESSIVE), Set.of()
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingExecutor.execute(application);

        // then
        assertThat(matchingResultJpaRepository.findAll()).hasSize(1);
    }

    @Test
    void 신청_인원수보다_많은_후보자_중_상위만_선택된다() {
        // given
        var applicant = saveMemberWithTags(
                "202312345@sangmyung.kr", Gender.MALE,
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.EXPRESSIVE
        );
        var score3Candidate = saveCandidateWithTags(
                "202212011@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.FREQUENT_CONTACT
        );
        var score2Candidate1 = saveCandidateWithTags(
                "202212012@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.MODERATE_CONTACT
        );
        var score2Candidate2 = saveCandidateWithTags(
                "202212013@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.PUPPY, DatingStyleTag.MODERATE_CONTACT
        );
        var score1Candidate = saveCandidateWithTags(
                "202212014@sangmyung.kr",
                PersonalityTag.ACTIVE, FaceTypeTag.CAT, DatingStyleTag.MODERATE_CONTACT
        );

        var application = matchingManager.apply(
                NewMatching.builder()
                        .matchingType(MatchingType.IDEAL)
                        .applicationCount(2)
                        .idealTypePreference(IdealTypePreference.of(
                                Set.of(PersonalityTag.ACTIVE), Set.of(FaceTypeTag.PUPPY), Set.of(DatingStyleTag.FREQUENT_CONTACT), Set.of()
                        ))
                        .build(),
                applicant.getId()
        );

        // when
        matchingExecutor.execute(application);

        // then
        var results = matchingResultJpaRepository.findAll();
        assertThat(results).hasSize(2);
        var candidateIds = results.stream()
                .map(r -> r.getCandidate().getId())
                .toList();
        assertThat(candidateIds).contains(score3Candidate.getId());
        assertThat(candidateIds).containsAnyOf(score2Candidate1.getId(), score2Candidate2.getId());
        assertThat(candidateIds).doesNotContain(score1Candidate.getId());
    }

    // Helper methods

    private Member saveMemberWithTags(
            String email, Gender gender,
            PersonalityTag personalityTag, FaceTypeTag faceTypeTag, DatingStyleTag datingStyleTag
    ) {
        Member member = memberJpaRepository.save(Member.create(
                email, "password123!", ENCODER,
                "홍길동", gender, Mbti.ISTP, Department.SOFTWARE,
                "insta_id", "안녕하세요", "착한 사람"
        ));
        memberProfileTagJpaRepository.save(
                MemberProfileTag.create(member, personalityTag, faceTypeTag, datingStyleTag)
        );
        return member;
    }

    private Member saveCandidateWithTags(
            String email,
            PersonalityTag personalityTag, FaceTypeTag faceTypeTag, DatingStyleTag datingStyleTag
    ) {
        Member candidate = memberJpaRepository.save(Member.create(
                email, "password123!", ENCODER,
                "김미영", Gender.FEMALE, Mbti.ENFP, Department.ELECTRONICS_ENGINEERING,
                email.substring(0, email.indexOf('@')), "안녕하세요", "좋은 사람"
        ));
        candidate.updateRole(Role.ROLE_CANDIDATE);
        memberProfileTagJpaRepository.save(
                MemberProfileTag.create(candidate, personalityTag, faceTypeTag, datingStyleTag)
        );
        return candidate;
    }

}
