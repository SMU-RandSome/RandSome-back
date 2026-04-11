package org.smu.randsome.randsomeback.domain.ticket.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.ticket.dto.response.TicketBalanceResponse;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.service.TicketService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TicketController extends TicketControllerDocs {

    private final TicketService ticketService;

    @Override
    @GetMapping("/v1/tickets/balance")
    public ApiResponse<TicketBalanceResponse> getTicketBalance(@LoginMember Long memberId) {
        List<Ticket> tickets = ticketService.findMyTickets(memberId);

        return ApiResponse.success(TicketBalanceResponse.from(tickets));
    }

}