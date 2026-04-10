package org.smu.randsome.randsomeback.domain.ticket.repository;

import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketHistoryJpaRepository extends JpaRepository<TicketHistory, Long> {

}