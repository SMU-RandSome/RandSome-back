package org.smu.randsome.randsomeback.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingAppliedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;

class MatchingServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingService matchingService;

    @Mock
    MatchingManager matchingManager;

    @Mock
    MatchingReader matchingReader;

    @Mock
    PaymentManager paymentManager;

    @Mock
    ApplicationEventPublisher eventPublisher;

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

        ArgumentCaptor<MatchingAppliedEvent> captor = ArgumentCaptor.forClass(MatchingAppliedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().matchingApplicationId()).isEqualTo(10L);
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

    @Test
    void 상태별_신청_내역을_조회한다() {
        // given
        var memberId = 1L;
        var application = mock(MatchingApplication.class);
        given(matchingReader.findByMemberAndStatus(memberId, ApplicationStatus.PENDING))
                .willReturn(List.of(application));
        given(application.getMatchingType()).willReturn(MatchingType.RANDOM);
        given(application.getApplicationStatus()).willReturn(ApplicationStatus.PENDING);

        // when
        List<MatchingApplication> result = matchingService.getMyApplications(memberId, ApplicationStatus.PENDING);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).extracting(
                MatchingApplication::getMatchingType,
                MatchingApplication::getApplicationStatus
        ).containsExactly(
                application.getMatchingType(),
                application.getApplicationStatus()
        );

        verify(matchingReader).findByMemberAndStatus(memberId, ApplicationStatus.PENDING);
    }

    @Test
    void 승인된_신청의_매칭_결과를_조회한다() {
        // given
        var applicationId = 1L;
        var memberId = 1L;
        var matchingResult = mock(MatchingResult.class);
        given(matchingReader.findApprovedByApplication(applicationId, memberId))
                .willReturn(List.of(matchingResult));

        // when
        var result = matchingService.getApprovedApplication(applicationId, memberId);

        // then
        assertThat(result).hasSize(1);
        verify(matchingReader).findApprovedByApplication(applicationId, memberId);
    }

    @Test
    void 승인되지_않은_신청_조회시_예외가_전파된다() {
        // given
        var applicationId = 1L;
        var memberId = 1L;
        willThrow(new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING))
                .given(matchingReader).findApprovedByApplication(applicationId, memberId);

        // when & then
        assertThatThrownBy(() -> matchingService.getApprovedApplication(applicationId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING.getMessage());
    }

    @Test
    void 매칭_신청_철회에_성공한다() {
        // given
        var applicationId = 1L;
        var memberId = 1L;

        // when
        matchingService.withdraw(applicationId, memberId);

        // then
        verify(matchingManager).withdraw(applicationId, memberId);
    }

}