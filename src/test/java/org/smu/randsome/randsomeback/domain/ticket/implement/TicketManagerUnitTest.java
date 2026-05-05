package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class TicketManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketManager ticketManager;

    @Mock
    TicketJpaRepository ticketJpaRepository;

    @Mock
    TicketReader ticketReader;

    @Test
    void 회원_가입_시_RANDOM과_IDEAL_티켓이_생성된다() {
        // given
        Member member = MemberFixture.create();
        given(ticketJpaRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        List<Ticket> tickets = ticketManager.create(member);

        // then
        assertThat(tickets).hasSize(2);
        assertThat(tickets).extracting(Ticket::getTicketType)
                .containsExactlyInAnyOrder(TicketType.RANDOM, TicketType.IDEAL);
    }

    @Test
    void 생성된_티켓의_수량은_기본값이다() {
        // given
        Member member = MemberFixture.create();
        given(ticketJpaRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        List<Ticket> tickets = ticketManager.create(member);

        // then
        tickets.forEach(ticket ->
                assertThat(ticket.getQuantityValue()).isEqualTo(ticket.getTicketType().getDefaultQuantity())
        );
    }

    @Test
    void 티켓_차감에_성공한다() {
        // given
        var memberId = 1L;
        var ticketType = TicketType.RANDOM;
        var ticket = Ticket.create(MemberFixture.create(), ticketType, 3);
        given(ticketReader.findByMemberAndType(memberId, ticketType)).willReturn(ticket);

        // when
        ticketManager.use(memberId, ticketType, 1);

        // then
        assertThat(ticket.getQuantityValue()).isEqualTo(2);
    }

    @Test
    void 티켓이_없으면_NOT_FOUND_TICKET_예외가_발생한다() {
        // given
        var memberId = 1L;
        var ticketType = TicketType.RANDOM;
        willThrow(new CoreException(ErrorType.NOT_FOUND_TICKET))
                .given(ticketReader).findByMemberAndType(memberId, ticketType);

        // when & then
        assertThatThrownBy(() -> ticketManager.use(memberId, ticketType, 1))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_TICKET.getMessage());
    }

    @Test
    void 티켓이_부족하면_NOT_ENOUGH_TICKETS_예외가_발생한다() {
        // given
        var memberId = 1L;
        var ticketType = TicketType.RANDOM;
        var ticket = Ticket.create(MemberFixture.create(), ticketType, 1);
        given(ticketReader.findByMemberAndType(memberId, ticketType)).willReturn(ticket);

        // when & then
        assertThatThrownBy(() -> ticketManager.use(memberId, ticketType, 3))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ENOUGH_TICKETS.getMessage());
    }

}