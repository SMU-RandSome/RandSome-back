package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

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

class TicketManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketManager ticketManager;

    @Mock
    TicketJpaRepository ticketJpaRepository;

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

}