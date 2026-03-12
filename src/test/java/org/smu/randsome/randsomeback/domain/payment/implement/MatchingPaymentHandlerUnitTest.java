package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class MatchingPaymentHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingPaymentHandler matchingPaymentHandler;

    @Mock
    MatchingManager matchingManager;

    @Test
    void approve_호출_시_matchingManager_approve를_호출한다() {
        // given
        var referenceId = 1L;
        var now = TestDateTimeUtils.now();

        // when
        matchingPaymentHandler.approve(referenceId, now);

        // then
        verify(matchingManager).approve(referenceId, now);
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