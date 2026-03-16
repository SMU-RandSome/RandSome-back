package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.feed.FeedManager;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class MatchingPaymentHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingPaymentHandler matchingPaymentHandler;

    @Mock
    MatchingManager matchingManager;

    @Mock
    FeedManager feedManager;

    @Test
    void approve_호출_시_matchingManager_approve와_feedManager_recordMatchRequest를_호출한다() {
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
        matchingPaymentHandler.approve(referenceId, now);

        // then
        verify(matchingManager).approve(referenceId, now);
        verify(feedManager).recordMatchRequest(nickname, requestCount);
    }

    @Test
    void reject_호출_시_matchingManager_reject를_호출한다() {
        // given
        var referenceId = 1L;
        var reason = "거절 사유";
        var now = TestDateTimeUtils.now();

        // when
        matchingPaymentHandler.reject(referenceId, reason, now);

        // then
        verify(matchingManager).reject(referenceId, reason, now);
    }

}