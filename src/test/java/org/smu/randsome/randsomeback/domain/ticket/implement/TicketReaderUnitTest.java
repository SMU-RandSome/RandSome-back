package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class TicketReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    TicketReader ticketReader;

    @Mock
    TicketJpaRepository ticketJpaRepository;

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