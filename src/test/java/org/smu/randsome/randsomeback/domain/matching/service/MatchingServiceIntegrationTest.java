package org.smu.randsome.randsomeback.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class MatchingServiceIntegrationTest extends IntegrationTestSupport {

    final MatchingService matchingService;
    final MemberJpaRepository memberJpaRepository;
    final TicketJpaRepository ticketJpaRepository;
    final TicketHistoryJpaRepository ticketHistoryJpaRepository;

    // ===== 완전 매칭 =====

    @Test
    void 완전_매칭_시_신청_인원만큼_매칭되고_환불_없이_종료된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create()); // MALE, SOFTWARE
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 5));
        for (int i = 1; i <= 3; i++) {
            memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                    "20221200" + i + "@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));
        }

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        MatchingApplication result = matchingService.apply(newMatching, member.getId());

        // then
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.SUCCESS);
        assertThat(result.getMatchedCount()).isEqualTo(3);

        Ticket ticket = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(
                member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE).orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(2); // 5 - 3 = 2
    }

    @Test
    void 완전_매칭_시_USE_이력_1건만_생성된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 5));
        for (int i = 1; i <= 2; i++) {
            memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                    "20221300" + i + "@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));
        }

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(2)
                .build();

        // when
        matchingService.apply(newMatching, member.getId());

        // then
        List<TicketHistory> histories = ticketHistoryJpaRepository.findAll();
        assertThat(histories).hasSize(1);
        assertThat(histories.getFirst().getActionType()).isEqualTo(TicketActionType.USE);
        assertThat(histories.getFirst().getSource()).isEqualTo(TicketSource.MATCHING);
    }

    // ===== 부분 매칭 =====

    @Test
    void 부분_매칭_시_실제_매칭_수만큼_matchedCount가_저장된다() {
        // given: 신청 3명, 후보자 1명
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 5));
        memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202213001@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        MatchingApplication result = matchingService.apply(newMatching, member.getId());

        // then
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.SUCCESS);
        assertThat(result.getMatchedCount()).isEqualTo(1);
        assertThat(result.getApplicationCount()).isEqualTo(3);
    }

    @Test
    void 부분_매칭_시_차액만큼_티켓이_환불된다() {
        // given: 신청 3명, 후보자 1명 → 환불 2장
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 5));
        memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202214001@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        matchingService.apply(newMatching, member.getId());

        // then: 5 - 3(차감) + 2(환불) = 4
        Ticket ticket = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(
                member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE).orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(4);
    }

    @Test
    void 부분_매칭_시_USE와_PARTIAL_MATCH_REFUND_이력_2건이_생성된다() {
        // given: 신청 3명, 후보자 1명
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 5));
        memberJpaRepository.save(MemberFixture.createCandidateWithDepartment(
                "202215001@sangmyung.kr", Gender.FEMALE, MemberFixture.OTHER_DEPARTMENT));

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        matchingService.apply(newMatching, member.getId());

        // then
        List<TicketHistory> histories = ticketHistoryJpaRepository.findAll();
        assertThat(histories).hasSize(2);
        assertThat(histories).extracting(TicketHistory::getActionType)
                .containsExactlyInAnyOrder(TicketActionType.USE, TicketActionType.REFUND);
        assertThat(histories).extracting(TicketHistory::getSource)
                .containsExactlyInAnyOrder(TicketSource.MATCHING, TicketSource.PARTIAL_MATCH_REFUND);
    }

    // ===== 매칭 없음 =====

    @Test
    void 후보자가_없으면_티켓이_전액_환불된다() {
        // given: 신청 3명, 후보자 0명
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 5));

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        MatchingApplication result = matchingService.apply(newMatching, member.getId());

        // then
        assertThat(result.getMatchedCount()).isZero();
        assertThat(result.getApplicationStatus()).isEqualTo(ApplicationStatus.SUCCESS);

        Ticket ticket = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(
                member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE).orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(5); // 5 - 3 + 3 = 5 (전액 환불)
    }

    @Test
    void 후보자_없을_때_USE와_NO_MATCH_REFUND_이력_2건이_생성된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 3));

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when
        matchingService.apply(newMatching, member.getId());

        // then
        List<TicketHistory> histories = ticketHistoryJpaRepository.findAll();
        assertThat(histories).hasSize(2);
        assertThat(histories).extracting(TicketHistory::getActionType)
                .containsExactlyInAnyOrder(TicketActionType.USE, TicketActionType.REFUND);
        assertThat(histories).extracting(TicketHistory::getSource)
                .containsExactlyInAnyOrder(TicketSource.MATCHING, TicketSource.NO_MATCH_REFUND);
    }

    // ===== 트랜잭션 롤백 =====

    @Test
    void 티켓이_부족하면_매칭_신청이_생성되지_않는다() {
        // given: 티켓 0장
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketJpaRepository.save(Ticket.create(member, TicketType.RANDOM, 0));

        var newMatching = NewMatching.builder()
                .matchingType(MatchingType.RANDOM)
                .applicationCount(3)
                .build();

        // when & then
        assertThatThrownBy(() -> matchingService.apply(newMatching, member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ENOUGH_TICKETS.getMessage());
    }

}