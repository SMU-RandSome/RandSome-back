package org.smu.randsome.randsomeback.domain.ticket.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class TicketReader {

    private final TicketJpaRepository ticketJpaRepository;

    @Transactional(readOnly = true)
    public Ticket findByMemberAndType(Long memberId, TicketType ticketType) {
        return ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(memberId, ticketType, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_TICKET));
    }

}