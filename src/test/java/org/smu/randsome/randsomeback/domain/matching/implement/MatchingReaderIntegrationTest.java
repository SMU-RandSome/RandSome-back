package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingReaderIntegrationTest extends IntegrationTestSupport {

    final MatchingReader matchingReader;
    final MemberJpaRepository memberJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;
    final MatchingResultJpaRepository matchingResultJpaRepository;

    @Test
    void PENDING_상태의_신청_목록을_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var pending = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 2));
        var approved = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        approved.approve(TestDateTimeUtils.now());

        // when
        List<MatchingApplication> result = matchingReader.findByMemberAndStatus(member.getId(), ApplicationStatus.PENDING);

        // then
        assertThat(result).hasSize(1)
                .extracting(MatchingApplication::getId)
                .containsExactly(pending.getId());
    }

    @Test
    void APPROVED_상태의_신청_목록을_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var pending = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 2));
        var approved = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        approved.approve(TestDateTimeUtils.now());

        // when
        List<MatchingApplication> result = matchingReader.findByMemberAndStatus(member.getId(), ApplicationStatus.APPROVED);

        // then
        assertThat(result).hasSize(1)
                .extracting(
                        MatchingApplication::getId,
                        MatchingApplication::getApplicationStatus,
                        MatchingApplication::getApplicationCount
                )
                .containsExactly(
                        tuple(approved.getId(), ApplicationStatus.APPROVED, approved.getApplicationCount())
                );
    }

    @Test
    void REJECTED_상태의_신청_목록을_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 2));
        var rejected = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        rejected.reject(TestDateTimeUtils.now(), "서류 미비");

        // when
        List<MatchingApplication> result = matchingReader.findByMemberAndStatus(member.getId(), ApplicationStatus.REJECTED);

        // then
        assertThat(result).hasSize(1)
                .extracting(
                        MatchingApplication::getId,
                        MatchingApplication::getApplicationStatus,
                        MatchingApplication::getRejectedReason
                )
                .containsExactly(
                        tuple(rejected.getId(), ApplicationStatus.REJECTED, "서류 미비")
                );
    }

    @Test
    void 다른_회원의_신청은_조회되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var other = memberJpaRepository.save(MemberFixture.createWithGender("202399999@sangmyung.kr", Gender.FEMALE));
        matchingJpaRepository.save(MatchingApplication.apply(other, MatchingType.RANDOM, 2));

        // when
        List<MatchingApplication> result = matchingReader.findByMemberAndStatus(member.getId(), ApplicationStatus.PENDING);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 승인된_신청의_매칭_결과_목록을_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202399001@sangmyung.kr", Gender.FEMALE));
        var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        application.approve(TestDateTimeUtils.now());
        var result1 = matchingResultJpaRepository.save(MatchingResult.create(application, candidate));

        // when
        List<MatchingResult> result = matchingReader.findApprovedByApplication(application.getId(), member.getId());

        // then
        assertThat(result).hasSize(1)
                .extracting(MatchingResult::getId)
                .containsExactly(result1.getId());
    }

    @Test
    void PENDING_상태의_신청에_대해_매칭_결과_조회시_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var application = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));

        // when & then
        assertThatThrownBy(() -> matchingReader.findApprovedByApplication(application.getId(), member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_APPROVED_MATCHING.getMessage());
    }

    @Test
    void 다른_회원의_승인된_신청을_조회하면_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var other = memberJpaRepository.save(MemberFixture.createWithGender("202399002@sangmyung.kr", Gender.FEMALE));
        var application = matchingJpaRepository.save(MatchingApplication.apply(other, MatchingType.RANDOM, 1));
        application.approve(TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> matchingReader.findApprovedByApplication(application.getId(), member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_APPROVED_MATCHING.getMessage());
    }

    @Test
    void 후보자로_노출된_횟수를_조회한다() {
        // given
        var applicant1 = memberJpaRepository.save(MemberFixture.create());
        var applicant2 = memberJpaRepository.save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var applicant3 = memberJpaRepository.save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.MALE));
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202310003@sangmyung.kr", Gender.FEMALE));

        var application1 = matchingJpaRepository.save(MatchingApplication.apply(applicant1, MatchingType.RANDOM, 1));
        application1.approve(TestDateTimeUtils.now());
        var application2 = matchingJpaRepository.save(MatchingApplication.apply(applicant2, MatchingType.RANDOM, 1));
        application2.approve(TestDateTimeUtils.now());
        var application3 = matchingJpaRepository.save(MatchingApplication.apply(applicant3, MatchingType.RANDOM, 1));
        application3.approve(TestDateTimeUtils.now());

        matchingResultJpaRepository.save(MatchingResult.create(application1, candidate));
        matchingResultJpaRepository.save(MatchingResult.create(application2, candidate));
        matchingResultJpaRepository.save(MatchingResult.create(application3, candidate));

        // when
        long count = matchingReader.countExposures(candidate.getId());

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void 삭제된_매칭_결과는_노출_횟수에_포함되지_않는다() {
        // given
        var applicant1 = memberJpaRepository.save(MemberFixture.create());
        var applicant2 = memberJpaRepository.save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202310003@sangmyung.kr", Gender.FEMALE));

        var application1 = matchingJpaRepository.save(MatchingApplication.apply(applicant1, MatchingType.RANDOM, 1));
        application1.approve(TestDateTimeUtils.now());
        var application2 = matchingJpaRepository.save(MatchingApplication.apply(applicant2, MatchingType.RANDOM, 1));
        application2.approve(TestDateTimeUtils.now());

        matchingResultJpaRepository.save(MatchingResult.create(application1, candidate));
        var deletedResult = matchingResultJpaRepository.save(MatchingResult.create(application2, candidate));
        deletedResult.delete();

        // when
        long count = matchingReader.countExposures(candidate.getId());

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void 다른_후보자의_노출_횟수는_포함되지_않는다() {
        // given
        var applicant = memberJpaRepository.save(MemberFixture.create());
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202310003@sangmyung.kr", Gender.FEMALE));
        var otherCandidate = memberJpaRepository.save(MemberFixture.createWithGender("202310004@sangmyung.kr", Gender.FEMALE));

        var application = matchingJpaRepository.save(MatchingApplication.apply(applicant, MatchingType.RANDOM, 1));
        application.approve(TestDateTimeUtils.now());

        matchingResultJpaRepository.save(MatchingResult.create(application, candidate));
        matchingResultJpaRepository.save(MatchingResult.create(application, otherCandidate));

        // when
        long count = matchingReader.countExposures(candidate.getId());

        // then
        assertThat(count).isEqualTo(1L);
    }

}