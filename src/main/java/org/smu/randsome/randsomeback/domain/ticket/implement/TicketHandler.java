package org.smu.randsome.randsomeback.domain.ticket.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.event.TicketHistoryRegisterEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TicketHandler {

    private final TicketManager ticketManager;
    private final ApplicationEventPublisher eventPublisher;

    public void issue(Member member) {
        List<Ticket> tickets = ticketManager.create(member);

        tickets.forEach(ticket -> eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                member.getId(),
                ticket.getTicketType(),
                TicketActionType.EARN,
                TicketSource.JOIN,
                ticket.getQuantityValue(),
                TicketSource.JOIN.getDescription()
        )));

        log.info("[TicketHandler] 티켓 생성 완료 - memberId={}", member.getId());
    }

}