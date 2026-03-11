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
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class CandidateManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CandidateManager candidateManager;

    @Mock
    CandidateJpaRepository candidateJpaRepository;

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
        var registrationId = 1L;
        var member = mock(Member.class);
        var registration = CandidateRegistration.apply(member);
        given(candidateJpaRepository.findByIdAndStatusWithMember(registrationId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(registration));

        // when
        candidateManager.approve(registrationId, TestDateTimeUtils.now());

        // then
        assertThat(registration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(registration.getApprovedAt()).isNotNull();
        verify(member).updateRole(Role.ROLE_CANDIDATE);
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

}