package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketHistorySortType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryRepository;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.test.util.ReflectionTestUtils;

class TicketReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketReader ticketReader;

    @Mock
    TicketJpaRepository ticketJpaRepository;

    @Mock
    TicketHistoryRepository ticketHistoryRepository;

    @Test
    void 회원과_타입으로_티켓을_조회한다() {
        // given
        var memberId = 1L;
        var ticketType = TicketType.RANDOM;
        var ticket = Ticket.create(MemberFixture.create(), ticketType, 3);
        given(ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(memberId, ticketType, EntityStatus.ACTIVE))
                .willReturn(Optional.of(ticket));

        // when
        Ticket result = ticketReader.findByMemberAndType(memberId, ticketType);

        // then
        assertThat(result.getTicketType()).isEqualTo(ticketType);
        assertThat(result.getQuantityValue()).isEqualTo(3);
    }

    @Test
    void 회원의_모든_티켓_목록을_조회한다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        var tickets = List.of(
                Ticket.create(member, TicketType.RANDOM, 3),
                Ticket.create(member, TicketType.IDEAL, 1)
        );
        given(ticketJpaRepository.findAllByMemberIdAndStatus(memberId, EntityStatus.ACTIVE))
                .willReturn(tickets);

        // when
        List<Ticket> result = ticketReader.findMyTickets(memberId);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 마지막_페이지면_hasNext가_false이고_nextCursor가_null이다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, 20);
        var histories = List.of(
                TicketHistory.register(member, TicketType.RANDOM, TicketActionType.USE, TicketSource.MATCHING, 3, null),
                TicketHistory.register(member, TicketType.IDEAL, TicketActionType.EARN, TicketSource.ATTENDANCE, 1, null)
        );
        given(ticketHistoryRepository.findHistories(memberId, condition)).willReturn(histories);

        // when
        CursorSlice<TicketHistory> result = ticketReader.findMyHistory(memberId, condition);

        // then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.items()).hasSize(2);
    }

    @Test
    void 다음_페이지가_있으면_hasNext가_true이고_nextCursor가_마지막_항목의_id이다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        int size = 2;
        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, size);

        var histories = new ArrayList<TicketHistory>();
        for (long i = 1; i <= size + 1; i++) {
            var history = TicketHistory.register(member, TicketType.RANDOM, TicketActionType.USE, TicketSource.MATCHING, 3, null);
            ReflectionTestUtils.setField(history, "id", i);
            histories.add(history);
        }
        given(ticketHistoryRepository.findHistories(memberId, condition)).willReturn(histories);

        // when
        CursorSlice<TicketHistory> result = ticketReader.findMyHistory(memberId, condition);

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.items()).hasSize(size);
        assertThat(result.nextCursor()).isEqualTo(2L);
    }

    @Test
    void 티켓이_없으면_NOT_FOUND_TICKET_예외가_발생한다() {
        // given
        var memberId = 1L;
        var ticketType = TicketType.IDEAL;
        given(ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(memberId, ticketType, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ticketReader.findByMemberAndType(memberId, ticketType))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_TICKET.getMessage());
    }

}