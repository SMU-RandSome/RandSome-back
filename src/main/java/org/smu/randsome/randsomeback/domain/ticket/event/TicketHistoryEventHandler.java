package org.smu.randsome.randsomeback.domain.ticket.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TicketHistoryEventHandler {

    private final TicketHistoryJpaRepository ticketHistoryJpaRepository;
    private final MemberReader memberReader;

    /**
     * 티켓 히스토리를 기록한다.
     * 
     * <p>사용자가 이력을 즉시 조회하므로, 티켓 지급/차감과 히스토리 기록을 하나의 트랜잭션으로 묶어 강한 일관성을 보장한다.
     * 히스토리 저장 실패 시 메인 비즈니스 로직(티켓 변동)도 함께 롤백되어 데이터 정합성을 유지한다.
     */
    @EventListener
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

        log.info("[TicketHistoryHandler] 티켓 히스토리 기록 완료 - memberId={}, type={}, amount={}", 
                event.memberId(), event.actionType(), event.amount());
    }

}