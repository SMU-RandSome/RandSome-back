package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.event.TicketHistoryEventHandler;
import org.smu.randsome.randsomeback.domain.ticket.event.TicketHistoryRegisterEvent;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class TicketHistoryEventHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketHistoryEventHandler ticketHistoryEventHandler;

    @Mock
    MemberReader memberReader;

    @Mock
    TicketHistoryJpaRepository ticketHistoryJpaRepository;

    @Test
    void 티켓_히스토리가_기록된다() {
        // given
        var member = MemberFixture.create();
        given(memberReader.find(any(Long.class))).willReturn(member);

        // when
        ticketHistoryEventHandler.recordTicketHistory(new TicketHistoryRegisterEvent(
                1L,
                TicketType.RANDOM,
                TicketActionType.USE,
                TicketSource.ATTENDANCE,
                5,
                "테스트 히스토리"
        ));

        // then
        verify(ticketHistoryJpaRepository).save(any(TicketHistory.class));
    }

}