package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.event.TicketHistoryRegisterEvent;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;

class TicketHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketHandler ticketHandler;

    @Mock
    TicketManager ticketManager;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    MemberReader memberReader;

    @Test
    void 티켓을_생성한다() {
        // given
        Member member = MemberFixture.create();
        List<Ticket> tickets = List.of(
                Ticket.create(member, TicketType.RANDOM, TicketType.RANDOM.getDefaultQuantity()),
                Ticket.create(member, TicketType.IDEAL, TicketType.IDEAL.getDefaultQuantity())
        );
        given(ticketManager.create(member)).willReturn(tickets);

        // when
        ticketHandler.issue(member);

        // then
        verify(ticketManager).create(member);
    }

    @Test
    void 티켓_생성_후_티켓_타입별로_히스토리_이벤트를_발행한다() {
        // given
        Member member = MemberFixture.create();
        List<Ticket> tickets = List.of(
                Ticket.create(member, TicketType.RANDOM, TicketType.RANDOM.getDefaultQuantity()),
                Ticket.create(member, TicketType.IDEAL, TicketType.IDEAL.getDefaultQuantity())
        );
        given(ticketManager.create(member)).willReturn(tickets);

        // when
        ticketHandler.issue(member);

        // then
        verify(eventPublisher, times(TicketType.values().length)).publishEvent(isA(TicketHistoryRegisterEvent.class));
    }

    @Test
    void 매칭_신청_시_티켓을_차감한다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        // when
        ticketHandler.deduct(memberId, newMatching);

        // then
        verify(ticketManager).use(memberId, TicketType.RANDOM, 2);
    }

    @Test
    void 티켓_차감_후_USE_액션으로_히스토리_이벤트를_발행한다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        // when
        ticketHandler.deduct(memberId, newMatching);

        // then
        ArgumentCaptor<TicketHistoryRegisterEvent> captor = ArgumentCaptor.forClass(TicketHistoryRegisterEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TicketHistoryRegisterEvent event = captor.getValue();
        assertThat(event).extracting(
                TicketHistoryRegisterEvent::memberId,
                TicketHistoryRegisterEvent::ticketType,
                TicketHistoryRegisterEvent::actionType,
                TicketHistoryRegisterEvent::source,
                TicketHistoryRegisterEvent::amount
        ).containsExactly(
                memberId,
                TicketType.RANDOM,
                TicketActionType.USE,
                TicketSource.MATCHING,
                2
        );
    }

    @Test
    void IDEAL_매칭_신청_시_IDEAL_티켓을_차감한다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.IDEAL)
                .applicationCount(1)
                .build();

        // when
        ticketHandler.deduct(memberId, newMatching);

        // then
        verify(ticketManager).use(memberId, TicketType.IDEAL, 1);
    }

    @Test
    void 티켓이_부족하면_히스토리_이벤트를_발행하지_않는다() {
        // given
        var memberId = 1L;
        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(5)
                .build();
        willThrow(new CoreException(ErrorType.NOT_ENOUGH_TICKETS))
                .given(ticketManager).use(memberId, TicketType.RANDOM, 5);

        // when & then
        assertThatThrownBy(() -> ticketHandler.deduct(memberId, newMatching))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ENOUGH_TICKETS.getMessage());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 쿠폰_보상으로_티켓을_지급하면_earn을_호출하고_COUPON_source_이벤트를_발행한다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.RANDOM;
        int amount = 3;

        // when
        ticketHandler.issueForCoupon(memberId, ticketType, amount);

        // then
        verify(ticketManager).earn(memberId, ticketType, amount);

        ArgumentCaptor<TicketHistoryRegisterEvent> captor = ArgumentCaptor.forClass(TicketHistoryRegisterEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TicketHistoryRegisterEvent event = captor.getValue();
        assertThat(event).extracting(
                TicketHistoryRegisterEvent::memberId,
                TicketHistoryRegisterEvent::ticketType,
                TicketHistoryRegisterEvent::actionType,
                TicketHistoryRegisterEvent::source,
                TicketHistoryRegisterEvent::amount
        ).containsExactly(
                memberId,
                ticketType,
                TicketActionType.EARN,
                TicketSource.COUPON,
                amount
        );
    }

    @Test
    void 쿠폰_티켓_지급_실패_시_이벤트를_발행하지_않는다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.RANDOM;
        int amount = 3;

        willThrow(new CoreException(ErrorType.NOT_FOUND_TICKET))
                .given(ticketManager).earn(memberId, ticketType, amount);

        // when & then
        assertThatThrownBy(() -> ticketHandler.issueForCoupon(memberId, ticketType, amount))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_TICKET.getMessage());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 완전_매칭_시_refundForPartialMatch는_환불하지_않는다() {
        // given
        var memberId = 1L;
        int requestedCount = 3;
        int matchedCount = 3;

        // when
        ticketHandler.refundForPartialMatch(memberId, MatchingType.RANDOM, requestedCount, matchedCount);

        // then
        verify(ticketManager, never()).refund(any(), any(), any(Integer.class));
        verify(eventPublisher, never()).publishEvent(any(TicketHistoryRegisterEvent.class));
    }

    @Test
    void 부분_매칭_시_차액만큼_PARTIAL_MATCH_REFUND_source로_환불한다() {
        // given
        var memberId = 1L;
        int requestedCount = 5;
        int matchedCount = 3;
        int expectedRefund = 2;

        // when
        ticketHandler.refundForPartialMatch(memberId, MatchingType.RANDOM, requestedCount, matchedCount);

        // then
        verify(ticketManager).refund(memberId, TicketType.RANDOM, expectedRefund);

        ArgumentCaptor<TicketHistoryRegisterEvent> captor = ArgumentCaptor.forClass(TicketHistoryRegisterEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TicketHistoryRegisterEvent event = captor.getValue();
        assertThat(event).extracting(
                TicketHistoryRegisterEvent::memberId,
                TicketHistoryRegisterEvent::ticketType,
                TicketHistoryRegisterEvent::actionType,
                TicketHistoryRegisterEvent::source,
                TicketHistoryRegisterEvent::amount
        ).containsExactly(
                memberId,
                TicketType.RANDOM,
                TicketActionType.REFUND,
                TicketSource.PARTIAL_MATCH_REFUND,
                expectedRefund
        );
    }

    @Test
    void 매칭_결과_없을_시_전체를_NO_MATCH_REFUND_source로_환불한다() {
        // given
        var memberId = 1L;
        int requestedCount = 3;
        int matchedCount = 0;

        // when
        ticketHandler.refundForPartialMatch(memberId, MatchingType.RANDOM, requestedCount, matchedCount);

        // then
        verify(ticketManager).refund(memberId, TicketType.RANDOM, requestedCount);

        ArgumentCaptor<TicketHistoryRegisterEvent> captor = ArgumentCaptor.forClass(TicketHistoryRegisterEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().source()).isEqualTo(TicketSource.NO_MATCH_REFUND);
    }

    @Test
    void IDEAL_부분_매칭_시_IDEAL_티켓을_환불한다() {
        // given
        var memberId = 1L;
        int requestedCount = 2;
        int matchedCount = 1;

        // when
        ticketHandler.refundForPartialMatch(memberId, MatchingType.IDEAL, requestedCount, matchedCount);

        // then
        verify(ticketManager).refund(memberId, TicketType.IDEAL, 1);
    }

    @Test
    void 출석_보상으로_티켓을_지급하면_티켓을_생성하고_히스토리_이벤트를_발행한다() {
        // given
        Long memberId = 1L;
        int expectedAmount = 1;
        TicketType expectedType = TicketType.RANDOM;

        // when
        ticketHandler.issueForAttendance(memberId);

        // then
        // 1. TicketManager의 earn 메서드가 올바른 인자로 호출되었는지 확인
        verify(ticketManager).earn(memberId, expectedType, expectedAmount);

        // 2. TicketHistoryRegisterEvent가 올바른 데이터와 함께 발행되었는지 확인
        verify(eventPublisher).publishEvent(any(TicketHistoryRegisterEvent.class));

        // 상세 이벤트 값 검증 (ArgumentCaptor를 사용할 수도 있지만, 간단하게 호출 여부만 확인하거나 필드 직접 검증)
        verify(eventPublisher).publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                expectedType,
                TicketActionType.EARN,
                TicketSource.ATTENDANCE,
                expectedAmount,
                TicketSource.ATTENDANCE.getDescription()
        ));
    }

}