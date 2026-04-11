package org.smu.randsome.randsomeback.domain.ticket.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class TicketManager {

    private final TicketJpaRepository ticketJpaRepository;

    @Transactional
    public List<Ticket> create(Member member) {
        Ticket randomTicket = Ticket.create(member, TicketType.RANDOM, TicketType.RANDOM.getDefaultQuantity());
        Ticket idealTicket = Ticket.create(member, TicketType.IDEAL, TicketType.IDEAL.getDefaultQuantity());

        return ticketJpaRepository.saveAll(List.of(randomTicket, idealTicket));
    }

}