package org.smu.randsome.randsomeback.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.service.command.NewMatching;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class MatchingServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingService matchingService;

    @Mock
    MatchingManager matchingManager;

    @Mock
    PaymentManager paymentManager;

    @Test
    void 매칭_신청에_성공한다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        var member = mock(Member.class);
        var application = mock(MatchingApplication.class);
        given(application.getMember()).willReturn(member);
        given(application.getMatchingType()).willReturn(MatchingType.RANDOM);
        given(application.getId()).willReturn(10L);
        given(application.getApplicationCount()).willReturn(2);
        given(matchingManager.apply(newMatching, memberId)).willReturn(application);

        // when
        matchingService.apply(newMatching, memberId);

        // then
        verify(matchingManager).apply(newMatching, memberId);
        verify(paymentManager).register(any(Member.class), any(PaymentType.class), anyLong(), anyInt());
    }

    @Test
    void 존재하지_않는_회원이면_예외가_발생한다() {
        // given
        var memberId = 999L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(matchingManager).apply(newMatching, memberId);

        // when & then
        assertThatThrownBy(() -> matchingService.apply(newMatching, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

}
