package org.smu.randsome.randsomeback.domain.ticket.repository;

import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketJpaRepository extends JpaRepository<Ticket, Long> {

}