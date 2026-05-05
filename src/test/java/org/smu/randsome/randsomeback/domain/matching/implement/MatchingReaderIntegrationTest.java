package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
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
    void 회원의_매칭_신청_목록을_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var otherMember = memberJpaRepository.save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));

        matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.IDEAL, 2));
        matchingJpaRepository.save(MatchingApplication.apply(otherMember, MatchingType.RANDOM, 1));

        // when
        var result = matchingReader.findMatchings(member.getId());

        // then
        assertThat(result).hasSize(2)
                .allMatch(app -> app.getMember().getId().equals(member.getId()));
    }

    @Test
    void 삭제된_매칭_신청은_조회에_포함되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        var activeApp = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.RANDOM, 1));
        var deletedApp = matchingJpaRepository.save(MatchingApplication.apply(member, MatchingType.IDEAL, 2));
        deletedApp.delete();

        // when
        var result = matchingReader.findMatchings(member.getId());

        // then
        assertThat(result).hasSize(1)
                .extracting(MatchingApplication::getId)
                .contains(activeApp.getId());
    }

    @Test
    void 신청의_매칭_결과를_조회한다() {
        // given
        var applicant = memberJpaRepository.save(MemberFixture.create());
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202310003@sangmyung.kr", Gender.FEMALE));

        var application = matchingJpaRepository.save(MatchingApplication.apply(applicant, MatchingType.RANDOM, 1));
        application.complete(TestDateTimeUtils.now(), 3);

        matchingResultJpaRepository.save(MatchingResult.create(application, candidate));

        // when
        var result = matchingReader.findApplication(application.getId(), applicant.getId());

        // then
        assertThat(result).hasSize(1)
                .allMatch(r -> r.getCandidate().getId().equals(candidate.getId()));
    }

    @Test
    void 매칭_실패한_신청_조회시_예외가_발생한다() {
        // given
        var applicant = memberJpaRepository.save(MemberFixture.create());
        var application = matchingJpaRepository.save(MatchingApplication.apply(applicant, MatchingType.RANDOM, 1));

        // when & then
        assertThatThrownBy(() -> matchingReader.findApplication(application.getId(), applicant.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING_RESULT.getMessage());
    }

    @Test
    void 다른_회원의_신청은_조회할_수_없다() {
        // given
        var applicant = memberJpaRepository.save(MemberFixture.create());
        var otherMember = memberJpaRepository.save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202310003@sangmyung.kr", Gender.FEMALE));

        var application = matchingJpaRepository.save(MatchingApplication.apply(applicant, MatchingType.RANDOM, 1));
        application.complete(TestDateTimeUtils.now(), 3);
        matchingResultJpaRepository.save(MatchingResult.create(application, candidate));

        // when & then
        assertThatThrownBy(() -> matchingReader.findApplication(application.getId(), otherMember.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void 후보자로_노출된_횟수를_조회한다() {
        // given
        var applicant1 = memberJpaRepository.save(MemberFixture.create());
        var applicant2 = memberJpaRepository.save(MemberFixture.createWithGender("202310001@sangmyung.kr", Gender.MALE));
        var applicant3 = memberJpaRepository.save(MemberFixture.createWithGender("202310002@sangmyung.kr", Gender.MALE));
        var candidate = memberJpaRepository.save(MemberFixture.createWithGender("202310003@sangmyung.kr", Gender.FEMALE));

        var application1 = matchingJpaRepository.save(MatchingApplication.apply(applicant1, MatchingType.RANDOM, 1));
        application1.complete(TestDateTimeUtils.now(), 3);
        var application2 = matchingJpaRepository.save(MatchingApplication.apply(applicant2, MatchingType.RANDOM, 1));
        application2.complete(TestDateTimeUtils.now(), 3);
        var application3 = matchingJpaRepository.save(MatchingApplication.apply(applicant3, MatchingType.RANDOM, 1));
        application3.complete(TestDateTimeUtils.now(), 3);

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
        application1.complete(TestDateTimeUtils.now(), 3);
        var application2 = matchingJpaRepository.save(MatchingApplication.apply(applicant2, MatchingType.RANDOM, 1));
        application2.complete(TestDateTimeUtils.now(), 3);

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
        application.complete(TestDateTimeUtils.now(), 3);

        matchingResultJpaRepository.save(MatchingResult.create(application, candidate));
        matchingResultJpaRepository.save(MatchingResult.create(application, otherCandidate));

        // when
        long count = matchingReader.countExposures(candidate.getId());

        // then
        assertThat(count).isEqualTo(1L);
    }

}