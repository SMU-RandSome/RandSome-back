package org.smu.randsome.randsomeback.admin.qr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.qr.implement.QrTokenProvider;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class QrAdminServiceIntegrationTest extends IntegrationTestSupport {

    final QrAdminService qrAdminService;
    final QrTokenProvider qrTokenProvider;
    final MemberJpaRepository memberJpaRepository;
    final TicketJpaRepository ticketJpaRepository;
    final TicketHistoryRepository ticketHistoryRepository;
    final TicketHandler ticketHandler;
    final StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        Set<String> keys = stringRedisTemplate.keys("qr:*");
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    void QR_인증_후_IDEAL_티켓이_1장_증가하고_ADMIN_출처의_히스토리가_기록된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHandler.issue(member);

        var created = qrTokenProvider.createToken(member.getId());

        Ticket idealTicket = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.IDEAL, EntityStatus.ACTIVE)
                .orElseThrow();
        int beforeQuantity = idealTicket.getQuantityValue();

        // when
        qrAdminService.verifyQrAndIssueTicket(created.token(), TicketType.IDEAL);

        // then — 티켓 수량 확인
        Ticket updatedTicket = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.IDEAL, EntityStatus.ACTIVE)
                .orElseThrow();
        assertThat(updatedTicket.getQuantityValue()).isEqualTo(beforeQuantity + TicketType.IDEAL.getDefaultQuantity());

        // then — 히스토리 확인 (JOIN 2개 + ADMIN QR 1개)
        List<TicketHistory> histories = ticketHistoryRepository.findHistories(
                member.getId(), new TicketHistorySearchCondition(null, null, null, 10));
        assertThat(histories).hasSize(3)
                .extracting(TicketHistory::getTicketType, TicketHistory::getActionType, TicketHistory::getSource)
                .contains(tuple(TicketType.IDEAL, TicketActionType.EARN, TicketSource.ADMIN));
    }

    @Test
    void QR_인증_후_RANDOM_티켓이_3장_증가한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHandler.issue(member);

        var created = qrTokenProvider.createToken(member.getId());

        int beforeQuantity = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow().getQuantityValue();

        // when
        qrAdminService.verifyQrAndIssueTicket(created.token(), TicketType.RANDOM);

        // then
        int afterQuantity = ticketJpaRepository
                .findByMemberIdAndTicketTypeAndStatus(member.getId(), TicketType.RANDOM, EntityStatus.ACTIVE)
                .orElseThrow().getQuantityValue();
        assertThat(afterQuantity).isEqualTo(beforeQuantity + TicketType.RANDOM.getDefaultQuantity());
    }

    @Test
    void 이미_사용된_QR토큰으로_재시도하면_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        ticketHandler.issue(member);

        var created = qrTokenProvider.createToken(member.getId());
        qrAdminService.verifyQrAndIssueTicket(created.token(), TicketType.IDEAL);

        // when & then
        assertThatThrownBy(() -> qrAdminService.verifyQrAndIssueTicket(created.token(), TicketType.IDEAL))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.QR_TOKEN_ALREADY_USED.getMessage());
    }

    @Test
    void 유효하지_않은_QR토큰이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> qrAdminService.verifyQrAndIssueTicket("invalid.token", TicketType.IDEAL))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_QR_TOKEN.getMessage());
    }

    @Test
    void 존재하지_않는_회원의_QR토큰이면_예외가_발생한다() {
        // given — DB에 없는 memberId로 토큰 생성
        var created = qrTokenProvider.createToken(999999L);

        // when & then
        assertThatThrownBy(() -> qrAdminService.verifyQrAndIssueTicket(created.token(), TicketType.IDEAL))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

}
