package org.smu.randsome.randsomeback.domain.ticket.implement;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

@RequiredArgsConstructor
class TicketManagerIntegrationTest extends IntegrationTestSupport {

    final TicketManager ticketManager;
    final MemberJpaRepository memberJpaRepository;
    final TicketJpaRepository ticketJpaRepository;

    @AfterEach
    void tearDown() {
        ticketJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 티켓_생성_시_RANDOM과_IDEAL_티켓이_기본_수량으로_DB에_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        // when
        ticketManager.create(member);

        // then
        var tickets = ticketJpaRepository.findAllByMemberIdAndStatus(member.getId(), EntityStatus.ACTIVE);
        assertThat(tickets).hasSize(2);
        assertThat(tickets).extracting(t -> t.getTicketType())
                .containsExactlyInAnyOrder(TicketType.RANDOM, TicketType.IDEAL);
        assertThat(tickets).extracting(t -> t.getQuantityValue())
                .containsExactlyInAnyOrder(TicketType.RANDOM.getDefaultQuantity(), TicketType.IDEAL.getDefaultQuantity());
    }

    @Test
    void 티켓_사용_시_수량이_차감되어_DB에_반영된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketManager.create(member);

        // when
        ticketManager.use(member.getId(), TicketType.RANDOM, 1);

        // then
        var ticket = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(TicketType.RANDOM.getDefaultQuantity() - 1);
    }

    @Test
    void 티켓_적립_시_수량이_증가하여_DB에_반영된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketManager.create(member);

        // when
        ticketManager.earn(member.getId(), TicketType.RANDOM, 2);

        // then
        var ticket = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(TicketType.RANDOM.getDefaultQuantity() + 2);
    }

    @Test
    void 티켓_환불_시_수량이_복구되어_DB에_반영된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketManager.create(member);
        ticketManager.use(member.getId(), TicketType.RANDOM, 2);

        // when
        ticketManager.refund(member.getId(), TicketType.RANDOM, 1);

        // then
        var ticket = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(TicketType.RANDOM.getDefaultQuantity() - 1);
    }

}
