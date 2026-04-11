package org.smu.randsome.randsomeback.domain.ticket.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryRepository;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class TicketReader {

    private final TicketJpaRepository ticketJpaRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Transactional(readOnly = true)
    public Ticket findByMemberAndType(Long memberId, TicketType ticketType) {
        return ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(memberId, ticketType, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_TICKET));
    }

    @Transactional(readOnly = true)
    public List<Ticket> findMyTickets(Long memberId) {
        return ticketJpaRepository.findAllByMemberIdAndStatus(memberId, EntityStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public CursorSlice<TicketHistory> findMyHistory(Long memberId, TicketHistorySearchCondition condition) {
        List<TicketHistory> results = ticketHistoryRepository.findHistories(memberId, condition);

        boolean hasNext = results.size() > condition.size();
        List<TicketHistory> items = hasNext ? results.subList(0, condition.size()) : results;
        Long nextCursor = hasNext ? items.getLast().getId() : null;

        return CursorSlice.of(items, nextCursor, hasNext);
    }

}