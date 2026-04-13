package org.smu.randsome.randsomeback.domain.ticket.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.QTicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketHistorySortType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TicketHistoryQueryRepositoryImpl implements TicketHistoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    private static final QTicketHistory ticketHistory = QTicketHistory.ticketHistory;

    @Override
    public List<TicketHistory> findHistories(Long memberId, TicketHistorySearchCondition condition) {
        return queryFactory.selectFrom(ticketHistory)
                .where(
                        ticketHistory.member.id.eq(memberId),
                        ticketHistory.status.eq(EntityStatus.ACTIVE),
                        ticketTypeEq(condition.ticketType()),
                        cursorCondition(condition.cursor(), condition.sortType())
                )
                .orderBy(orderBy(condition.sortType()))
                .limit(condition.size() + 1L)
                .fetch();
    }

    private BooleanExpression ticketTypeEq(TicketType ticketType) {
        return ticketType != null ? ticketHistory.ticketType.eq(ticketType) : null;
    }

    private BooleanExpression cursorCondition(Long cursor, TicketHistorySortType sortType) {
        if (cursor == null) {
            return null;
        }
        return sortType == TicketHistorySortType.LATEST
                ? ticketHistory.id.lt(cursor)
                : ticketHistory.id.gt(cursor);
    }

    private OrderSpecifier<Long> orderBy(TicketHistorySortType sortType) {
        return sortType == TicketHistorySortType.LATEST
                ? ticketHistory.id.desc()
                : ticketHistory.id.asc();
    }

}