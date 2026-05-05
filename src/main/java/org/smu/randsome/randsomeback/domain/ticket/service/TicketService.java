package org.smu.randsome.randsomeback.domain.ticket.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketReader;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TicketService {

    private final TicketReader ticketReader;

    /**
     * 회원의 티켓 수량 내역을 조회한다.
     *
     * @param memberId 회원 식별자
     *
     *
     */
    public List<Ticket> findMyTickets(Long memberId) {
        return ticketReader.findMyTickets(memberId);
    }

    /**
     * 회원의 티켓 변동 내역을 조회한다.
     *
     * @param memberId  회원 식별자
     * @param condition 조회 조건 (예: 날짜 범위, 티켓 유형 등)
     * @return 회원의 티켓 사용 내역 리스트
     *
     */
    public CursorSlice<TicketHistory> findMyHistory(Long memberId, TicketHistorySearchCondition condition) {
        return ticketReader.findMyHistory(memberId, condition);
    }

}