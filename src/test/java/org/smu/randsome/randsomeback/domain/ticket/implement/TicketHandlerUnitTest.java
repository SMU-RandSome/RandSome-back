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

}