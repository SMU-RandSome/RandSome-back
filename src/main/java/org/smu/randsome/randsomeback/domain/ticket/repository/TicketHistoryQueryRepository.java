package org.smu.randsome.randsomeback.domain.ticket.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;

public interface TicketHistoryQueryRepository {

    /**
     * 회원의 티켓 변동 내역을 조회하는 메서드입니다.
     *
     * @param memberId  조회할 회원의 ID입니다.
     * @param condition 조회 조건을 담은 객체입니다. (예: 날짜 범위, 티켓 유형 등)
     * @return 회원의 티켓 사용 내역 리스트입니다.
     */
    List<TicketHistory> findHistories(Long memberId, TicketHistorySearchCondition condition);

}