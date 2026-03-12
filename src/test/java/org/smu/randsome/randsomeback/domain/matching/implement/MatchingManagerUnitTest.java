package org.smu.randsome.randsomeback.domain.matching.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.service.command.NewMatching;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class MatchingManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingManager matchingManager;

    @Mock
    MatchingJpaRepository matchingJpaRepository;

    @Mock
    MemberReader memberReader;

    @Test
    void 매칭_신청을_저장하고_반환한다() {
        // given
        var memberId = 1L;
        var member = mock(Member.class);
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        given(memberReader.find(memberId)).willReturn(member);
        given(matchingJpaRepository.save(any(MatchingApplication.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        MatchingApplication result = matchingManager.apply(newMatching, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getMatchingType()).isEqualTo(MatchingType.RANDOM);
        assertThat(result.getApplicationCount()).isEqualTo(3);
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void 회원이_없으면_예외가_발생한다() {
        // given
        var memberId = 999L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();
        given(memberReader.find(memberId))
                .willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> matchingManager.apply(newMatching, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

}
