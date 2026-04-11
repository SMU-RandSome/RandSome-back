package org.smu.randsome.randsomeback.domain.ticket.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.dto.response.TicketBalanceResponse;
import org.smu.randsome.randsomeback.domain.ticket.dto.response.TicketHistoryItem;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketHistorySortType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.service.TicketService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated
public class TicketController extends TicketControllerDocs {

    private final TicketService ticketService;

    @Override
    @GetMapping("/v1/tickets/balance")
    public ApiResponse<TicketBalanceResponse> getTicketBalance(@LoginMember Long memberId) {
        List<Ticket> tickets = ticketService.findMyTickets(memberId);

        return ApiResponse.success(TicketBalanceResponse.from(tickets));
    }

    @Override
    @GetMapping("/v1/tickets/history")
    public ApiResponse<CursorSlice<TicketHistoryItem>> findMyTicketsHistory(
            @RequestParam(required = false) TicketType ticketType,
            @RequestParam(defaultValue = "LATEST") TicketHistorySortType sortType,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @LoginMember Long memberId
    ) {
        TicketHistorySearchCondition condition = new TicketHistorySearchCondition(ticketType, sortType, cursor, size);
        CursorSlice<TicketHistory> slice = ticketService.findMyHistory(memberId, condition);

        return ApiResponse.success(CursorSlice.of(
                slice.items().stream()
                        .map(TicketHistoryItem::from)
                        .toList(),
                slice.nextCursor(),
                slice.hasNext()
        ));
    }

}