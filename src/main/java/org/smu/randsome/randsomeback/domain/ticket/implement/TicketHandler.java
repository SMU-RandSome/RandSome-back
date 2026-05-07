package org.smu.randsome.randsomeback.domain.ticket.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
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
    private static final int CANDIDATE_APPROVAL_REWARD_RANDOM = 3;
    private static final int CANDIDATE_APPROVAL_REWARD_IDEAL = 3;

    private final TicketManager ticketManager;
    private final MemberReader memberReader;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 회원 가입 시 초기 티켓을 지급한다.
     * @param member 가입한 회원 정보
     *
     * */
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

    /**
     * 관리자에 의한 티켓 지급
     * @param memberId    회원 식별자
     * @param ticketType  지급할 티켓 종류
     * @param amount      지급할 티켓 수량
     * @param description 지급 사유
     **/
    public void issueForAdmin(Long memberId, TicketType ticketType, int amount, String description) {
        // 회원이 존재하는지 확인 (예외 발생 시 티켓 지급 중단)
        memberReader.find(memberId);

        ticketManager.earn(memberId, ticketType, amount);

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                ticketType,
                TicketActionType.EARN,
                TicketSource.ADMIN,
                amount,
                description
        ));

        log.info("[TicketHandler] 관리자에 의한 티켓 지급 완료 - memberId={}, ticketType={}, amount={}", memberId, ticketType, amount);
    }

    /**
     * 관리자에 의한 티켓 차감
     * @param memberId    회원 식별자
     * @param ticketType  차감할 티켓 종류
     * @param amount      차감할 티켓 수량
     * @param description 차감 사유
     **/
    public void deductForAdmin(Long memberId, TicketType ticketType, int amount, String description) {
        memberReader.find(memberId);

        ticketManager.use(memberId, ticketType, amount);

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                ticketType,
                TicketActionType.USE,
                TicketSource.ADMIN,
                amount,
                description
        ));

        log.info("[TicketHandler] 관리자에 의한 티켓 차감 완료 - memberId={}, ticketType={}, amount={}", memberId, ticketType, amount);
    }

    /**
     * 부분 매칭으로 인한 티켓 환불을 처리한다.
     * <br/>요청 인원보다 매칭 인원이 적을 경우에만 차액만큼 환불한다.
     *
     * @param memberId       회원 식별자
     * @param matchingType   매칭 타입 (환불할 티켓 종류 결정에 사용)
     * @param requestedCount 신청 인원 수
     * @param matchedCount   실제 매칭된 인원 수
     */
    public void refundForPartialMatch(Long memberId, MatchingType matchingType, int requestedCount, int matchedCount) {
        int refundedTickets = requestedCount - matchedCount;
        if (refundedTickets <= 0) {
            return;
        }
        TicketSource ticketSource = matchedCount == 0 ? TicketSource.NO_MATCH_REFUND : TicketSource.PARTIAL_MATCH_REFUND;
        refund(memberId, TicketType.from(matchingType), refundedTickets, ticketSource);
    }

    /**
     * 후보자 등록 승인 보상으로 티켓을 지급한다.
     * @param memberId 회원 식별자
     */
    public void issueForCandidateApproval(Long memberId) {
        ticketManager.earn(memberId, TicketType.RANDOM, CANDIDATE_APPROVAL_REWARD_RANDOM);
        ticketManager.earn(memberId, TicketType.IDEAL, CANDIDATE_APPROVAL_REWARD_IDEAL);

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                TicketType.RANDOM,
                TicketActionType.EARN,
                TicketSource.CANDIDATE_APPROVAL,
                CANDIDATE_APPROVAL_REWARD_RANDOM,
                TicketSource.CANDIDATE_APPROVAL.getDescription()
        ));
        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                TicketType.IDEAL,
                TicketActionType.EARN,
                TicketSource.CANDIDATE_APPROVAL,
                CANDIDATE_APPROVAL_REWARD_IDEAL,
                TicketSource.CANDIDATE_APPROVAL.getDescription()
        ));

        log.info("[TicketHandler] 후보자 승인 보상 티켓 지급 완료 - memberId={}", memberId);
    }

    /**
     * 후보자 철회 시 보상 티켓을 차감한다.
     * 잔액이 보상 수량보다 적으면 남은 만큼만 차감한다.
     * @param memberId 회원 식별자
     */
    public void deductForCandidateWithdrawal(Long memberId) {
        deductTicketForWithdrawal(memberId, TicketType.RANDOM, CANDIDATE_APPROVAL_REWARD_RANDOM);
        deductTicketForWithdrawal(memberId, TicketType.IDEAL, CANDIDATE_APPROVAL_REWARD_IDEAL);

        log.info("[TicketHandler] 후보자 철회 티켓 차감 완료 - memberId={}", memberId);
    }

    private void deductTicketForWithdrawal(Long memberId, TicketType ticketType, int rewardAmount) {
        int deducted = ticketManager.useUpTo(memberId, ticketType, rewardAmount);

        if (deducted <= 0) {
            return;
        }

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                ticketType,
                TicketActionType.USE,
                TicketSource.CANDIDATE_WITHDRAWAL,
                deducted,
                TicketSource.CANDIDATE_WITHDRAWAL.getDescription()
        ));
    }

    private void refund(Long memberId, TicketType ticketType, int amount, TicketSource ticketSource) {
        ticketManager.refund(memberId, ticketType, amount);

        eventPublisher.publishEvent(new TicketHistoryRegisterEvent(
                memberId,
                ticketType,
                TicketActionType.REFUND,
                ticketSource,
                amount,
                ticketSource.getDescription()
        ));

        log.info("[TicketHandler] 매칭 티켓 환불 완료 - memberId={}, ticketType={}, amount={}", memberId, ticketType, amount);
    }

}