package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.event.TicketHistoryRegisterEvent;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
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

}
