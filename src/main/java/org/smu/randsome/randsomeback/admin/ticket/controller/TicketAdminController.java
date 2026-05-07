package org.smu.randsome.randsomeback.admin.ticket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.ticket.dto.request.TicketDeductRequest;
import org.smu.randsome.randsomeback.admin.ticket.dto.request.TicketEarnRequest;
import org.smu.randsome.randsomeback.admin.ticket.service.TicketAdminService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TicketAdminController extends TicketAdminControllerDocs {

    private final TicketAdminService ticketAdminService;

    @Override
    @PostMapping("/v1/admin/tickets/earn")
    public ApiResponse<?> earnTicket(@RequestBody @Valid TicketEarnRequest request) {
        ticketAdminService.earnTicket(
                request.memberId(),
                request.ticketType(),
                request.amount(),
                request.reason()
        );

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/admin/tickets/deduct")
    public ApiResponse<?> deductTicket(@RequestBody @Valid TicketDeductRequest request) {
        ticketAdminService.deductTicket(
                request.memberId(),
                request.ticketType(),
                request.amount(),
                request.reason()
        );

        return ApiResponse.success();
    }
}
