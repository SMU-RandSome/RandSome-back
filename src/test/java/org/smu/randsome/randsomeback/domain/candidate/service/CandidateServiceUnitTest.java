package org.smu.randsome.randsomeback.domain.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateManager;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateReader;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateValidator;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;

class CandidateServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateService candidateService;

    @Mock
    CandidateValidator candidateValidator;

    @Mock
    CandidateManager candidateManager;

    @Mock
    CandidateReader candidateReader;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void 후보자_지원에_성공한다() {
        // given
        var memberId = 1L;
        var registration = mock(CandidateRegistration.class);
        given(candidateManager.apply(memberId)).willReturn(registration);
        given(registration.getId()).willReturn(1L);

        // when
        candidateService.apply(memberId);

        // then
        verify(candidateValidator).validateApply(memberId);
        verify(candidateManager).apply(memberId);

        ArgumentCaptor<CandidateAppliedEvent> captor = ArgumentCaptor.forClass(CandidateAppliedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().candidateRegistrationId()).isEqualTo(1L);
    }

    @Test
    void 이미_승인된_후보자이면_예외가_발생한다() {
        // given
        var memberId = 1L;
        willThrow(new CoreException(ErrorType.DUPLICATE_CANDIDATE))
                .given(candidateValidator).validateApply(memberId);

        // when & then
        assertThatThrownBy(() -> candidateService.apply(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_CANDIDATE.getMessage());
    }

    @Test
    void 이미_신청_중인_후보자이면_예외가_발생한다() {
        // given
        var memberId = 1L;
        willThrow(new CoreException(ErrorType.ALREADY_PENDING_CANDIDATE))
                .given(candidateValidator).validateApply(memberId);

        // when & then
        assertThatThrownBy(() -> candidateService.apply(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ALREADY_PENDING_CANDIDATE.getMessage());
    }

    @Test
    void 후보자_철회에_성공한다() {
        // given
        var memberId = 1L;

        // when
        candidateService.withdraw(memberId);

        // then
        verify(candidateManager).withdraw(memberId);
    }

    @Test
    void 승인된_후보자가_없으면_철회_시_예외가_발생한다() {
        // given
        var memberId = 999L;
        willThrow(new CoreException(ErrorType.NOT_FOUND_CANDIDATE))
                .given(candidateManager).withdraw(memberId);

        // when & then
        assertThatThrownBy(() -> candidateService.withdraw(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE.getMessage());
    }

}