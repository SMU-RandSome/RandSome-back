package org.smu.randsome.randsomeback.domain.candidate.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class CandidateServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateService candidateService;

    @Mock
    CandidateValidator candidateValidator;

    @Mock
    CandidateManager candidateManager;

    @Mock
    PaymentManager paymentManager;

    @Test
    void 후보자_지원에_성공한다() {
        // given
        var memberId = 1L;
        var registration = mock(CandidateRegistration.class);
        given(candidateManager.apply(memberId)).willReturn(registration);

        // when
        candidateService.apply(memberId);

        // then
        verify(candidateValidator).validateApply(memberId);
        verify(candidateManager).apply(memberId);
        verify(paymentManager).register(registration);
    }

    @Test
    void 이미_등록된_후보자이면_예외가_발생한다() {
        // given
        var memberId = 1L;
        willThrow(new CoreException(ErrorType.DUPLICATE_CANDIDATE))
                .given(candidateValidator).validateApply(memberId);

        // when & then
        assertThatThrownBy(() -> candidateService.apply(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_CANDIDATE.getMessage());
    }

}