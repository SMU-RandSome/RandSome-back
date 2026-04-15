package org.smu.randsome.randsomeback.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingManager;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class MatchingServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    MatchingService matchingService;

    @Mock
    MatchingManager matchingManager;

    @Mock
    MatchingReader matchingReader;

    @Mock
    TicketHandler ticketHandler;

    @Test
    void 매칭_신청에_성공한다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        var application = mock(MatchingApplication.class);
        given(matchingManager.apply(newMatching, memberId)).willReturn(application);

        // when
        matchingService.apply(newMatching, memberId);

        // then
        verify(ticketHandler).deduct(memberId, newMatching);
        verify(matchingManager).apply(newMatching, memberId);
        verify(matchingManager).executeMatching(eq(application), any());
    }

    @Test
    void 티켓이_부족하면_매칭_신청이_생성되지_않는다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(5)
                .build();
        willThrow(new CoreException(ErrorType.NOT_ENOUGH_TICKETS))
                .given(ticketHandler).deduct(memberId, newMatching);

        // when & then
        assertThatThrownBy(() -> matchingService.apply(newMatching, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ENOUGH_TICKETS.getMessage());

        verify(matchingManager, never()).apply(any(), anyLong());
    }

    @Test
    void 티켓_차감_순서는_매칭_신청_생성보다_먼저다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        var application = mock(MatchingApplication.class);
        given(matchingManager.apply(newMatching, memberId)).willReturn(application);

        var order = org.mockito.Mockito.inOrder(ticketHandler, matchingManager);

        // when
        matchingService.apply(newMatching, memberId);

        // then
        order.verify(ticketHandler).deduct(memberId, newMatching);
        order.verify(matchingManager).apply(newMatching, memberId);
        order.verify(matchingManager).executeMatching(eq(application), any());
    }

    @Test
    void 매칭_신청_생성_실패시_예외가_전파된다() {
        // given
        var memberId = 1L;
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

        verify(ticketHandler).deduct(memberId, newMatching);
        verify(matchingManager, never()).executeMatching(any(), any());
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
    void 매칭_신청_내역_목록을_조회한다() {
        // given
        var memberId = 1L;
        var matchingApplication = mock(MatchingApplication.class);
        given(matchingReader.findMatchings(memberId))
                .willReturn(List.of(matchingApplication));

        // when
        var result = matchingService.findMatchings(memberId);

        // then
        assertThat(result).hasSize(1);
        verify(matchingReader).findMatchings(memberId);
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
    void 매칭_신청_취소에_성공한다() {
        // given
        var applicationId = 1L;
        var memberId = 1L;

        // when
        matchingService.cancel(applicationId, memberId);

        // then
        verify(matchingManager).cancel(applicationId, memberId);
    }

    @Test
    void 매칭_신청_취소_실패시_예외가_전파된다() {
        // given
        var applicationId = 1L;
        var memberId = 1L;
        doThrow(new CoreException(ErrorType.NOT_FOUND_MATCHING))
                .when(matchingManager).cancel(applicationId, memberId);

        // when & then
        assertThatThrownBy(() -> matchingService.cancel(applicationId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MATCHING.getMessage());
    }

    @Test
    void 후보자_노출_횟수를_조회한다() {
        // given
        var memberId = 1L;
        given(matchingReader.countExposures(memberId)).willReturn(5L);

        // when
        long result = matchingService.getExposureCount(memberId);

        // then
        assertThat(result).isEqualTo(5L);
        verify(matchingReader).countExposures(memberId);
    }

}