package org.smu.randsome.randsomeback.domain.ticket.event;

import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

public record TicketHistoryRegisterEvent(
        Long memberId,
        TicketType ticketType,
        TicketActionType actionType,
        TicketSource source,
        int amount,
        String description
) {

}
