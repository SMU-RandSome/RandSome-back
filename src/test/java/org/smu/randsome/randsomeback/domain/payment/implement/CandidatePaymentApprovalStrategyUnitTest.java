package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.context.ApplicationEventPublisher;

class CandidatePaymentApprovalStrategyUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidatePaymentApprovalStrategy candidatePaymentApprovalStrategy;

    @Mock
    CandidateManager candidateManager;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void approve_호출_시_candidateManager_approve를_호출하고_이벤트를_발행한다() {
        // given
        var referenceId = 1L;
        var now = TestDateTimeUtils.now();
        var nickname = "여성#XYZ98765";

        var candidateRegistration = mock(CandidateRegistration.class);
        var member = mock(Member.class);
        given(candidateManager.approve(eq(referenceId), any(LocalDateTime.class))).willReturn(candidateRegistration);
        given(candidateRegistration.getMember()).willReturn(member);
        given(member.getNickname()).willReturn(nickname);

        // when
        candidatePaymentApprovalStrategy.approve(referenceId, now);

        // then
        verify(candidateManager).approve(eq(referenceId), any(LocalDateTime.class));
        verify(eventPublisher).publishEvent(new CandidateRegistrationApprovedEvent(nickname));
    }

    @Test
    void reject_호출_시_candidateManager_reject를_호출한다() {
        // given
        var referenceId = 1L;
        var reason = "거절 사유";
        var now = TestDateTimeUtils.now();

        // when
        candidatePaymentApprovalStrategy.reject(referenceId, reason, now);

        // then
        verify(candidateManager).reject(eq(referenceId), eq(reason), any(LocalDateTime.class));
    }

}
