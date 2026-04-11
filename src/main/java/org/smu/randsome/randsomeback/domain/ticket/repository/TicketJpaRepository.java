package org.smu.randsome.randsomeback.domain.ticket.repository;

import java.util.Optional;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketJpaRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByMemberIdAndTicketTypeAndStatus(Long memberId, TicketType ticketType, EntityStatus active);

}