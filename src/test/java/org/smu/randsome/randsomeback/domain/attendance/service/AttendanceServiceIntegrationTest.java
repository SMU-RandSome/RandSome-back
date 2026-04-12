package org.smu.randsome.randsomeback.domain.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.attendance.repository.AttendanceJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryRepository;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class AttendanceServiceIntegrationTest extends IntegrationTestSupport {

    final AttendanceService attendanceService;
    final MemberJpaRepository memberJpaRepository;
    final AttendanceJpaRepository attendanceJpaRepository;
    final TicketJpaRepository ticketJpaRepository;
    final TicketHandler ticketHandler;
    final TicketHistoryRepository ticketHistoryRepository;

    @Test
    void 회원은_하루에_한_번_출석_체크를_하고_티켓_보상을_받을_수_있다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHandler.issue(member);
        var memberId = member.getId();

        // when
        int beforeQuantity = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(memberId, TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow().getQuantityValue();
        attendanceService.attend(memberId);

        // then
        // 1. 출석 기록 확인
        assertThat(attendanceJpaRepository.findAllByMemberIdAndStatus(memberId, EntityStatus.ACTIVE)).hasSize(1);

        // 2. 티켓 지급 확인 (출석 보상 1장 지급)
        Ticket ticket = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(memberId, TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(ticket.getQuantityValue()).isEqualTo(beforeQuantity + 1);

        // 3. 티켓 히스토리 기록 확인 (동기 처리 확인)
        List<TicketHistory> histories = ticketHistoryRepository.findHistories(memberId, new TicketHistorySearchCondition(null, null, null, 10));
        assertThat(histories).hasSize(3).extracting(
                TicketHistory::getTicketType, TicketHistory::getActionType, TicketHistory::getSource
        ).containsExactly(
                tuple(TicketType.RANDOM, TicketActionType.EARN, TicketSource.JOIN),
                tuple(TicketType.IDEAL, TicketActionType.EARN, TicketSource.JOIN),
                tuple(TicketType.RANDOM, TicketActionType.EARN, TicketSource.ATTENDANCE)
        );
    }

    @Test
    void 이미_오늘_출석한_회원이_다시_출석_체크를_시도하면_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHandler.issue(member);
        var memberId = member.getId();

        // when
        attendanceService.attend(memberId);

        // when & then
        assertThatThrownBy(() -> attendanceService.attend(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_ATTENDANCE.getMessage());
    }

}