package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.service.command.NewMatching;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class MatchingManagerIntegrationTest extends IntegrationTestSupport {

    final MatchingManager matchingManager;
    final MemberJpaRepository memberJpaRepository;
    final MatchingJpaRepository matchingJpaRepository;

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
        assertThat(saved.getMember().getId()).isEqualTo(member.getId());
        assertThat(saved.getMatchingType()).isEqualTo(MatchingType.RANDOM);
        assertThat(saved.getApplicationCount()).isEqualTo(3);
        assertThat(saved.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(saved.getApprovedAt()).isNull();
        assertThat(saved.getRejectedAt()).isNull();
        assertThat(saved.getWithdrawnAt()).isNull();
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

}
