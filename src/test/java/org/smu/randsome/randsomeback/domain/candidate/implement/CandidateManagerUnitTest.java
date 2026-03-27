package org.smu.randsome.randsomeback.domain.candidate.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.test.util.ReflectionTestUtils;

class CandidateManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateManager candidateManager;

    @Mock
    CandidateJpaRepository candidateJpaRepository;

    @Mock
    MemberManager memberManager;

    @Mock
    MemberReader memberReader;

    @Test
    void 후보자_지원을_저장하고_반환한다() {
        // given
        var memberId = 1L;
        var member = mock(Member.class);
        given(memberReader.find(memberId)).willReturn(member);
        given(candidateJpaRepository.save(any(CandidateRegistration.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CandidateRegistration result = candidateManager.apply(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMember()).isEqualTo(member);
        assertThat(result.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
    }

    @Test
    void 후보자_등록을_승인한다() {
        // given
        var memberId = 2L;
        var member = MemberFixture.create();
        ReflectionTestUtils.setField(member, "id", memberId);

        var registrationId = 1L;
        var registration = CandidateRegistration.apply(member);

        given(candidateJpaRepository.findByIdAndStatusWithMember(registrationId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(registration));

        // when
        candidateManager.approve(registrationId, TestDateTimeUtils.now());

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(registration.getApprovedAt()).isNotNull();
        verify(memberManager).updateRole(member, Role.ROLE_CANDIDATE);
    }

    @Test
    void 후보자_등록을_거절한다() {
        // given
        var registrationId = 1L;
        var reason = "자격 미달";
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        given(candidateJpaRepository.findByIdAndStatus(registrationId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(registration));

        // when
        candidateManager.reject(registrationId, reason, TestDateTimeUtils.now());

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(registration.getRejectedReason()).isEqualTo(reason);
    }

    @Test
    void 후보자_등록이_없으면_NOT_FOUND_CANDIDATE를_던진다() {
        // given
        var registrationId = 999L;
        given(candidateJpaRepository.findByIdAndStatusWithMember(registrationId, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> candidateManager.approve(registrationId, TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE.getMessage());
    }

    @Test
    void 후보자_등록을_철회하면_WITHDRAWN_상태로_변경되고_역할이_MEMBER로_변경된다() {
        // given
        var memberId = 1L;
        var member = MemberFixture.create();
        ReflectionTestUtils.setField(member, "id", memberId);
        var registration = CandidateRegistration.apply(member);
        registration.approve(TestDateTimeUtils.now());

        given(candidateJpaRepository.existsByMemberIdAndStatus(memberId, EntityStatus.ACTIVE))
                .willReturn(true);
        given(candidateJpaRepository.findByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.APPROVED,
                EntityStatus.ACTIVE
        )).willReturn(Optional.of(registration));

        // when
        candidateManager.withdraw(memberId);

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.WITHDRAWN);
        assertThat(registration.getWithdrawnAt()).isNotNull();
        verify(memberManager).updateRole(member, Role.ROLE_MEMBER);
    }

    @Test
    void 철회할_활성_후보자_신청이_없으면_NOT_FOUND_CANDIDATE를_던진다() {
        // given
        var memberId = 999L;
        given(candidateJpaRepository.existsByMemberIdAndStatus(memberId, EntityStatus.ACTIVE))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> candidateManager.withdraw(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE.getMessage());
    }

    @Test
    void 활성화된_신청은_있지만_승인된_신청이_없으면_NOT_ALLOW_WITHDRAW_NON_APPROVED를_던진다() {
        // given
        var memberId = 2L;
        given(candidateJpaRepository.existsByMemberIdAndStatus(memberId, EntityStatus.ACTIVE))
                .willReturn(true);
        given(candidateJpaRepository.findByMemberIdAndRegistrationStatusAndStatus(
                memberId,
                RegistrationStatus.APPROVED,
                EntityStatus.ACTIVE
        )).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> candidateManager.withdraw(memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED.getMessage());
    }

}