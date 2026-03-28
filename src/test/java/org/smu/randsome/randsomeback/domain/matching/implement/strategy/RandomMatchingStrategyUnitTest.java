package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class RandomMatchingStrategyUnitTest extends UnitTestSupport {

    RandomMatchingStrategy strategy;

    @Mock
    MemberReader memberReader;

    @BeforeEach
    void setUp() {
        strategy = new RandomMatchingStrategy(memberReader);
    }

    @Test
    void 후보_조회_시_신청_인원수의_5배를_요청한다() {
        // given
        int applicationCount = 3;
        var applicant = mock(Member.class);
        given(applicant.getGender()).willReturn(Gender.MALE);

        var application = MatchingApplication.apply(applicant, MatchingType.RANDOM, applicationCount);
        given(memberReader.findCandidatesByGender(Gender.FEMALE, applicationCount * 5))
                .willReturn(List.of());

        // when
        strategy.execute(application);

        // then
        verify(memberReader).findCandidatesByGender(Gender.FEMALE, applicationCount * 5);
    }

    @Test
    void 후보가_신청_인원수보다_많아도_신청_인원수만큼만_결과를_반환한다() {
        // given
        int applicationCount = 2;
        var applicant = mock(Member.class);
        given(applicant.getGender()).willReturn(Gender.MALE);

        var application = MatchingApplication.apply(applicant, MatchingType.RANDOM, applicationCount);

        List<Member> candidates = IntStream.range(0, 10)
                .mapToObj(i -> MemberFixture.createCandidate("20221200" + i + "@sangmyung.kr", Gender.FEMALE))
                .toList();
        given(memberReader.findCandidatesByGender(Gender.FEMALE, applicationCount * 5))
                .willReturn(candidates);

        // when
        List<MatchingResult> results = strategy.execute(application);

        // then
        assertThat(results).hasSize(applicationCount);
    }
}