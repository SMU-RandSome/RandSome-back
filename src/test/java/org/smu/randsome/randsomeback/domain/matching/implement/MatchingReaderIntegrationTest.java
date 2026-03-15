package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingReaderIntegrationTest extends IntegrationTestSupport {

    final MatchingReader matchingReader;
    final MemberJpaRepository memberJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;

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

}