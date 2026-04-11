package org.smu.randsome.randsomeback.domain.ticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class TicketControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 티켓_잔고_조회에_성공하면_200과_수량을_반환한다() {
        // given
        var member = MemberFixture.create();
        given(ticketService.findMyTickets(any())).willReturn(List.of(
                Ticket.create(member, TicketType.RANDOM, 3),
                Ticket.create(member, TicketType.IDEAL, 1)
        ));

        // when & then
        assertThat(mvcTester.get().uri("/v1/tickets/balance"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.randomTicketCount", v -> v.assertThat().isEqualTo(3))
                .hasPathSatisfying("$.data.idealTicketCount", v -> v.assertThat().isEqualTo(1));
    }

    @Test
    void 인증되지_않은_사용자는_403을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/tickets/balance"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

}