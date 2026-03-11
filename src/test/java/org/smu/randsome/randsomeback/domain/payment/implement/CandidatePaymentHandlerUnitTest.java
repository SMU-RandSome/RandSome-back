package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class CandidatePaymentHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidatePaymentHandler candidatePaymentHandler;

    @Mock
    CandidateManager candidateManager;

    @Test
    void approve_호출_시_candidateManager_approve를_호출한다() {
        // given
        var referenceId = 1L;
        var now = TestDateTimeUtils.now();

        // when
        candidatePaymentHandler.approve(referenceId, now);

        // then
        verify(candidateManager).approve(eq(referenceId), any(LocalDateTime.class));
    }

    @Test
    void reject_호출_시_candidateManager_reject를_호출한다() {
        // given
        var referenceId = 1L;
        var reason = "거절 사유";
        var now = TestDateTimeUtils.now();

        // when
        candidatePaymentHandler.reject(referenceId, reason, now);

        // then
        verify(candidateManager).reject(eq(referenceId), eq(reason), any(LocalDateTime.class));
    }

}