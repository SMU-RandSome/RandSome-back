package org.smu.randsome.randsomeback.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.terms.repository.TermsAgreementJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.dto.command.TicketHistorySearchCondition;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryRepository;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@RequiredArgsConstructor
class MemberServiceIntegrationTest extends IntegrationTestSupport {

    final MemberService memberService;

    final MemberJpaRepository memberJpaRepository;
    final TicketJpaRepository ticketJpaRepository;
    final TicketHistoryRepository ticketHistoryRepository;
    final TermsAgreementJpaRepository termsAgreementJpaRepository;

    @MockitoBean
    MemberValidator memberValidator;

    @AfterEach
    void tearDown() {
        termsAgreementJpaRepository.deleteAll();
        ticketHistoryRepository.deleteAll();
        ticketJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    void 회원가입_성공시_저장되는_값들을_검증한다() {
        // given
        doNothing()
                .when(memberValidator)
                .validateSignUpToken("email.verification.token", MemberFixture.createCredentials().email());
        // when
        Long memberId = memberService.create(
                "email.verification.token",
                MemberFixture.createCredentials(),
                MemberFixture.createBasicInfo(),
                MemberFixture.createMemberSocialProfile(),
                MemberFixture.createTagsInfo()
        );

        // then
        assertThat(memberId).isNotNull();
        assertThat(memberJpaRepository.findById(memberId)).isPresent();
        assertThat(ticketJpaRepository.findAllByMemberIdAndStatus(memberId, EntityStatus.ACTIVE)).hasSize(2);
        assertThat(ticketHistoryRepository.findHistories(
                memberId,
                new TicketHistorySearchCondition(null, null, null, 10))
        ).hasSize(2);
    }

}