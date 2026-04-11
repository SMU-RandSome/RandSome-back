package org.smu.randsome.randsomeback.domain.ticket.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
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

    /**
     * 매칭 신청 시 티켓을 차감한다.
     * @param memberId 회원 식별자
     * @param newMatching 매칭 신청 정보
     * */
    public void deduct(Long memberId, NewMatching newMatching) {
        TicketType ticketType = TicketType.from(newMatching.matchingType());
        ticketManager.use(memberId, ticketType, newMatching.applicationCount());

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                ticketType,
                TicketActionType.USE,
                TicketSource.MATCHING,
                newMatching.applicationCount(),
                TicketSource.MATCHING.getDescription()
        ));

        log.info("[TicketHandler] 티켓 차감 완료 - memberId={}, matchingType={}", memberId, newMatching.matchingType());
    }

}