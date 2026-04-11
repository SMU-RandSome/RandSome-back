package org.smu.randsome.randsomeback.domain.ticket.dto.command;

import jakarta.validation.constraints.Positive;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketHistorySortType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

public record TicketHistorySearchCondition(
        TicketType ticketType,           // null = 전체 유형
        TicketHistorySortType sortType,
        Long cursor,                     // null = 첫 페이지
        @Positive
        int size
) {
}