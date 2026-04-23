package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingExecutorIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MatchingExecutor matchingExecutor;
    final MemberJpaRepository memberJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;

    @Test
    void 후보자가_없으면_FAILED_상태와_완료_시각과_매칭_수가_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();
        var application = matchingManager.apply(newMatching, member.getId());

        // when
        matchingExecutor.execute(application);

        // then: 후보자 없으므로 matchedCount=0, 상태는 FAILED
        var result = matchingJpaRepository.findById(application.getId()).orElseThrow();
        assertThat(result).extracting(
                MatchingApplication::getApplicationStatus,
                MatchingApplication::getMatchedCount
        ).containsExactly(
                ApplicationStatus.FAILED,
                0
        );
    }

}
