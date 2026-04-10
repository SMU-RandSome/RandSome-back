package org.smu.randsome.randsomeback.domain.ticket.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.event.TicketHistoryRegisterEvent;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryJpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class TicketHistoryHandler {

    private final TicketHistoryJpaRepository ticketHistoryJpaRepository;
    private final MemberReader memberReader;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void recordTicketHistory(TicketHistoryRegisterEvent event) {
        Member member = memberReader.find(event.memberId());
        ticketHistoryJpaRepository.save(TicketHistory.register(
                member,
                event.ticketType(),
                event.actionType(),
                event.source(),
                event.amount(),
                event.description()
        ));

        log.info("[TicketHistoryHandler] 티켓 히스토리 기록 완료 - memberId={}", event.memberId());
    }

}