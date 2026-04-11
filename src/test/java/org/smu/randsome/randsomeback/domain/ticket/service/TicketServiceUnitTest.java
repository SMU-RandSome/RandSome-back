package org.smu.randsome.randsomeback.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketReader;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class TicketServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketService ticketService;

    @Mock
    TicketReader ticketReader;

    @Test
    void 회원의_티켓_목록을_조회한다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        var tickets = List.of(
                Ticket.create(member, TicketType.RANDOM, 3),
                Ticket.create(member, TicketType.IDEAL, 1)
        );
        given(ticketReader.findMyTickets(memberId)).willReturn(tickets);

        // when
        List<Ticket> result = ticketService.findMyTickets(memberId);

        // then
        verify(ticketReader).findMyTickets(memberId);
        assertThat(result).hasSize(2);
    }

}