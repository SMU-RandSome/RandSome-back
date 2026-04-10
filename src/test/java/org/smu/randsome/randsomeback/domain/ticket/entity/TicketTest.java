package org.smu.randsome.randsomeback.domain.ticket.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;

class TicketTest {

    @Test
    void 티켓을_생성할_수_있다() {
        // given
        var member = MemberFixture.create();
        var randomType = TicketType.RANDOM;
        var idealType = TicketType.RANDOM;
        int quantity = 10;

        // when
        var randomTicket = Ticket.create(member, randomType, quantity);
        var idealTicket = Ticket.create(member, idealType, quantity);

        // then
        assertThat(randomTicket).isNotNull().extracting(
                Ticket::getMember,
                Ticket::getTicketType,
                Ticket::getQuantity
        ).containsExactly(member, randomType, quantity);

        assertThat(idealTicket).isNotNull().extracting(
                Ticket::getMember,
                Ticket::getTicketType,
                Ticket::getQuantity
        ).containsExactly(member, idealType, quantity);
    }

    @Test
    void 티켓을_적립하면_수량이_증가한다() {
        // given
        Ticket ticket = Ticket.create(MemberFixture.create(), TicketType.RANDOM, 10);

        // when
        ticket.earn(5);

        // then
        assertThat(ticket.getQuantity()).isEqualTo(15);
    }

    @Test
    void 티켓을_사용하면_수량이_감소한다() {
        // given
        Ticket ticket = Ticket.create(MemberFixture.create(), TicketType.RANDOM, 10);

        // when
        ticket.use(4);

        // then
        assertThat(ticket.getQuantity()).isEqualTo(6);
    }

    @Test
    void 티켓을_모두_사용할_수_있다() {
        // given
        Ticket ticket = Ticket.create(MemberFixture.create(), TicketType.RANDOM, 10);

        // when
        ticket.use(10);

        // then
        assertThat(ticket.getQuantity()).isZero();
    }

    @Test
    void 티켓이_부족하면_예외가_발생한다() {
        // given
        Ticket ticket = Ticket.create(MemberFixture.create(), TicketType.RANDOM, 3);

        // when & then
        assertThatThrownBy(() -> ticket.use(5))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 티켓을_사용한_후_다시_적립할_수_있다() {
        // given
        Ticket ticket = Ticket.create(MemberFixture.create(), TicketType.RANDOM, 10);

        // when
        ticket.use(5);
        ticket.earn(3);

        // then
        assertThat(ticket.getQuantity()).isEqualTo(8);
    }

}