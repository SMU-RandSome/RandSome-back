package org.smu.randsome.randsomeback.domain.ticket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketHistorySortType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class TicketHistoryQueryRepositoryIntegrationTest extends IntegrationTestSupport {

    final TicketHistoryRepository ticketHistoryRepository;
    final MemberJpaRepository memberJpaRepository;

    // ===== 정렬 =====

    @Test
    void 최신순으로_조회하면_ID_내림차순으로_반환된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var h1 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h2 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h3 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then
        assertThat(result)
                .extracting(TicketHistory::getId)
                .containsExactly(h3.getId(), h2.getId(), h1.getId());
    }

    @Test
    void 오래된순으로_조회하면_ID_오름차순으로_반환된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var h1 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h2 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h3 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.OLDEST, null, 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then
        assertThat(result)
                .extracting(TicketHistory::getId)
                .containsExactly(h1.getId(), h2.getId(), h3.getId());
    }

    // ===== 커서 =====

    @Test
    void 최신순_커서가_주어지면_해당_ID_미만의_이력만_반환된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var h1 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h2 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h3 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, h3.getId(), 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then: h3 미만 → h2, h1
        assertThat(result)
                .extracting(TicketHistory::getId)
                .containsExactly(h2.getId(), h1.getId());
    }

    @Test
    void 오래된순_커서가_주어지면_해당_ID_초과의_이력만_반환된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var h1 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h2 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var h3 = ticketHistoryRepository.save(history(member, TicketType.RANDOM));

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.OLDEST, h1.getId(), 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then: h1 초과 → h2, h3
        assertThat(result)
                .extracting(TicketHistory::getId)
                .containsExactly(h2.getId(), h3.getId());
    }

    // ===== 티켓 유형 필터 =====

    @Test
    void 티켓_유형으로_필터링하면_해당_유형의_이력만_반환된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        ticketHistoryRepository.save(history(member, TicketType.IDEAL));

        var condition = new TicketHistorySearchCondition(TicketType.RANDOM, TicketHistorySortType.LATEST, null, 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TicketHistory::getTicketType)
                .containsOnly(TicketType.RANDOM);
    }

    @Test
    void 티켓_유형이_null이면_전체_유형을_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        ticketHistoryRepository.save(history(member, TicketType.IDEAL));

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TicketHistory::getTicketType)
                .containsExactlyInAnyOrder(TicketType.RANDOM, TicketType.IDEAL);
    }

    // ===== size+1 fetch =====

    @Test
    void size보다_1개_더_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        for (int i = 0; i < 5; i++) {
            ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        }

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, 3);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then: limit = 3 + 1 = 4
        assertThat(result).hasSize(4);
    }

    // ===== 소프트 삭제 =====

    @Test
    void 소프트_삭제된_이력은_조회되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var active = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        var deleted = ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        deleted.delete();

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(active.getId());
    }

    // ===== 회원 격리 =====

    @Test
    void 다른_회원의_이력은_조회되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var other  = memberJpaRepository.save(MemberFixture.createWithLegalName("202300001@sangmyung.kr", "김철수"));

        ticketHistoryRepository.save(history(member, TicketType.RANDOM));
        ticketHistoryRepository.save(history(other, TicketType.RANDOM));

        var condition = new TicketHistorySearchCondition(null, TicketHistorySortType.LATEST, null, 10);

        // when
        List<TicketHistory> result = ticketHistoryRepository.findHistories(member.getId(), condition);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMember().getId()).isEqualTo(member.getId());
    }

    private TicketHistory history(org.smu.randsome.randsomeback.domain.member.entity.Member member, TicketType ticketType) {
        return TicketHistory.register(member, ticketType, TicketActionType.USE, TicketSource.MATCHING, 3, null);
    }

}