package org.smu.randsome.randsomeback.domain.ticket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
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
    @TestMember
    void 티켓_이력_조회에_성공하면_200과_이력목록을_반환한다() {
        // given
        var member = MemberFixture.create();
        var history = TicketHistory.register(member, TicketType.RANDOM, TicketActionType.USE, TicketSource.MATCHING, 3, null);
        given(ticketService.findMyHistory(any(), any())).willReturn(CursorSlice.of(List.of(history), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/tickets/history"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.hasNext", v -> v.assertThat().isEqualTo(false))
                .hasPathSatisfying("$.data.items[0].ticketType", v -> v.assertThat().isEqualTo("RANDOM"))
                .hasPathSatisfying("$.data.items[0].actionType", v -> v.assertThat().isEqualTo("USE"))
                .hasPathSatisfying("$.data.items[0].amount", v -> v.assertThat().isEqualTo(3));
    }

    @Test
    @TestMember
    void 티켓_유형_필터로_이력을_조회한다() {
        // given
        given(ticketService.findMyHistory(any(), any())).willReturn(CursorSlice.of(List.of(), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/tickets/history?ticketType=RANDOM"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

    @Test
    @TestMember
    void 커서와_크기로_다음_페이지를_조회한다() {
        // given
        given(ticketService.findMyHistory(any(), any())).willReturn(CursorSlice.of(List.of(), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/tickets/history?cursor=10&size=5&sortType=OLDEST"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

    @Test
    @TestMember
    void 티켓_이력_조회_시_크기가_0이면_400을_반환한다() {
        // when & then
        assertThat(mvcTester.get().uri("/v1/tickets/history?size=0"))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("ERROR"));
    }

}