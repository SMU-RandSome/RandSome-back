package org.smu.randsome.randsomeback.admin.ticket.service;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;

@ExtendWith(MockitoExtension.class)
class TicketAdminServiceUnitTest {

    @Mock
    TicketHandler ticketHandler;

    @InjectMocks
    TicketAdminService ticketAdminService;

    @Test
    void 사유와_함께_티켓을_지급한다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.RANDOM;
        int amount = 5;
        String reason = "이벤트 보상 지급";

        // when
        ticketAdminService.earnTicket(memberId, ticketType, amount, reason);

        // then
        then(ticketHandler).should().issueForAdmin(memberId, ticketType, amount, reason);
    }

    @Test
    void 사유가_null이면_기본_설명으로_티켓을_지급한다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.IDEAL;
        int amount = 3;

        // when
        ticketAdminService.earnTicket(memberId, ticketType, amount, null);

        // then
        then(ticketHandler).should().issueForAdmin(memberId, ticketType, amount, TicketSource.ADMIN.getDescription());
    }

    @Test
    void 사유가_빈_문자열이면_기본_설명으로_티켓을_지급한다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.RANDOM;
        int amount = 2;

        // when
        ticketAdminService.earnTicket(memberId, ticketType, amount, "   ");

        // then
        then(ticketHandler).should().issueForAdmin(memberId, ticketType, amount, TicketSource.ADMIN.getDescription());
    }

    @Test
    void 사유와_함께_티켓을_차감한다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.RANDOM;
        int amount = 3;
        String reason = "부정 사용 제재";

        // when
        ticketAdminService.deductTicket(memberId, ticketType, amount, reason);

        // then
        then(ticketHandler).should().deductForAdmin(memberId, ticketType, amount, reason);
    }

    @Test
    void 사유가_null이면_기본_설명으로_티켓을_차감한다() {
        // given
        Long memberId = 1L;
        TicketType ticketType = TicketType.IDEAL;
        int amount = 1;

        // when
        ticketAdminService.deductTicket(memberId, ticketType, amount, null);

        // then
        then(ticketHandler).should().deductForAdmin(memberId, ticketType, amount, TicketSource.ADMIN.getDescription());
    }
}
