package org.smu.randsome.randsomeback.domain.ticket.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.domain.ticket.dto.response.TicketBalanceResponse;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class TicketBalanceResponseTest {

    @Test
    void RANDOM과_IDEAL_티켓이_있으면_각각의_수량을_반환한다() {
        // given
        var member = MemberFixture.create();
        var tickets = List.of(
                Ticket.create(member, TicketType.RANDOM, 3),
                Ticket.create(member, TicketType.IDEAL, 1)
        );

        // when
        TicketBalanceResponse response = TicketBalanceResponse.from(tickets);

        // then
        assertThat(response.randomTicketCount()).isEqualTo(3);
        assertThat(response.idealTicketCount()).isEqualTo(1);
    }

    @Test
    void RANDOM_티켓이_없으면_randomTicketCount는_0이다() {
        // given
        var member = MemberFixture.create();
        var tickets = List.of(Ticket.create(member, TicketType.IDEAL, 1));

        // when
        TicketBalanceResponse response = TicketBalanceResponse.from(tickets);

        // then
        assertThat(response.randomTicketCount()).isZero();
        assertThat(response.idealTicketCount()).isEqualTo(1);
    }

    @Test
    void IDEAL_티켓이_없으면_idealTicketCount는_0이다() {
        // given
        var member = MemberFixture.create();
        var tickets = List.of(Ticket.create(member, TicketType.RANDOM, 3));

        // when
        TicketBalanceResponse response = TicketBalanceResponse.from(tickets);

        // then
        assertThat(response.randomTicketCount()).isEqualTo(3);
        assertThat(response.idealTicketCount()).isZero();
    }

    @Test
    void 티켓이_없으면_모든_수량이_0이다() {
        // given & when
        TicketBalanceResponse response = TicketBalanceResponse.from(List.of());

        // then
        assertThat(response.randomTicketCount()).isZero();
        assertThat(response.idealTicketCount()).isZero();
    }

}