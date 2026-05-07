package org.smu.randsome.randsomeback.admin.ticket.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TicketAdminService {

    private final TicketHandler ticketHandler;

    @Transactional
    public void earnTicket(Long memberId, TicketType ticketType, int amount, String reason) {
        String description = resolveDescription(reason);
        ticketHandler.issueForAdmin(memberId, ticketType, amount, description);
    }

    @Transactional
    public void deductTicket(Long memberId, TicketType ticketType, int amount, String reason) {
        String description = resolveDescription(reason);
        ticketHandler.deductForAdmin(memberId, ticketType, amount, description);
    }

    private String resolveDescription(String reason) {
        return (reason != null && !reason.isBlank())
                ? reason
                : TicketSource.ADMIN.getDescription();
    }

}