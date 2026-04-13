package org.smu.randsome.randsomeback.domain.ticket.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
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

    private static final int ATTENDANCE_REWARD_AMOUNT = 1;

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

    /**
     * 쿠폰 사용 보상으로 티켓을 지급한다.
     * @param memberId   회원 식별자
     * @param ticketType 지급할 티켓 종류
     * @param amount     지급할 티켓 수량
     */
    public void issueForCoupon(Long memberId, TicketType ticketType, int amount) {
        ticketManager.earn(memberId, ticketType, amount);

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                ticketType,
                TicketActionType.EARN,
                TicketSource.COUPON,
                amount,
                TicketSource.COUPON.getDescription()
        ));

        log.info("[TicketHandler] 쿠폰 보상 티켓 지급 완료 - memberId={}, ticketType={}, amount={}", memberId, ticketType, amount);
    }

    /**
     * 출석 보상으로 티켓을 지급한다.
     * @param memberId 회원 식별자
     * */
    public void issueForAttendance(Long memberId) {
        TicketType randomType = TicketType.RANDOM;

        ticketManager.earn(memberId, randomType, ATTENDANCE_REWARD_AMOUNT);

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                randomType,
                TicketActionType.EARN,
                TicketSource.ATTENDANCE,
                ATTENDANCE_REWARD_AMOUNT,
                TicketSource.ATTENDANCE.getDescription()
        ));

        log.info("[TicketHandler] 출석 보상 티켓 생성 완료 - memberId={}", memberId);
    }

}