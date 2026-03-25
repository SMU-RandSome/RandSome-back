package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationApprovedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.context.ApplicationEventPublisher;

class MatchingPaymentApprovalStrategyUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingPaymentApprovalStrategy matchingPaymentApprovalStrategy;

    @Mock
    MatchingManager matchingManager;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void approve_호출_시_matchingManager_approve를_호출하고_이벤트를_발행한다() {
        // given
        var referenceId = 1L;
        var now = TestDateTimeUtils.now();
        var nickname = "남성#ABC12345";
        var requestCount = 3;

        var matchingApplication = mock(MatchingApplication.class);
        var member = mock(Member.class);
        given(matchingManager.approve(referenceId, now)).willReturn(matchingApplication);
        given(matchingApplication.getMember()).willReturn(member);
        given(member.getNickname()).willReturn(nickname);
        given(matchingApplication.getApplicationCount()).willReturn(requestCount);

        // when
        matchingPaymentApprovalStrategy.approve(referenceId, now);

        // then
        verify(matchingManager).approve(referenceId, now);
        verify(eventPublisher).publishEvent(new MatchingApplicationApprovedEvent(nickname, requestCount));
    }

    @Test
    void reject_호출_시_matchingManager_reject를_호출한다() {
        // given
        var referenceId = 1L;
        var reason = "거절 사유";
        var now = TestDateTimeUtils.now();

        // when
        matchingPaymentApprovalStrategy.reject(referenceId, reason, now);

        // then
        verify(matchingManager).reject(referenceId, reason, now);
    }

}
