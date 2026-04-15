package org.smu.randsome.randsomeback.domain.candidate.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class CandidateManagerIntegrationTest extends IntegrationTestSupport {

    final CandidateManager candidateManager;
    final MemberJpaRepository memberJpaRepository;
    final CandidateJpaRepository candidateJpaRepository;

    @Test
    void 후보자_지원을_저장하면_PENDING_상태로_DB에_저장된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        // when
        var result = candidateManager.apply(member.getId());

        // then
        var saved = candidateJpaRepository.findById(result.getId()).orElseThrow();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
    }

    @Test
    void 후보자_등록을_승인하면_등록_상태와_승인_시각_및_회원_역할이_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));

        // when
        candidateManager.approve(registration.getId());

        // then
        var resultRegistration = candidateJpaRepository.findById(registration.getId()).orElseThrow();
        var resultMember = memberJpaRepository.findById(member.getId()).orElseThrow();

        assertThat(resultRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(resultMember.getRole()).isEqualTo(Role.ROLE_CANDIDATE);
    }

    @Test
    void 존재하지_않는_등록을_승인하면_NOT_FOUND_CANDIDATE를_던진다() {
        // given
        var nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> candidateManager.approve(nonExistentId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION.getMessage());
    }

    @Test
    void 후보자_등록을_거절하면_등록_상태와_거절_사유_및_거절_시각이_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var reason = "자격 미달";
        var rejectedAt = TestDateTimeUtils.now();

        // when
        candidateManager.reject(registration.getId(), reason);

        // then
        var resultRegistration = candidateJpaRepository.findById(registration.getId()).orElseThrow();

        assertThat(resultRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(resultRegistration.getRejectedReason()).isEqualTo(reason);
    }

    @Test
    void 존재하지_않는_등록을_거절하면_NOT_FOUND_CANDIDATE를_던진다() {
        // given
        var nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> candidateManager.reject(nonExistentId, "사유"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION.getMessage());
    }

    @Test
    void 후보자_등록을_철회하면_등록_상태와_철회_시각_및_회원_역할이_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var approvedAt = TestDateTimeUtils.now();
        candidateManager.approve(registration.getId());

        // when
        candidateManager.withdraw(member.getId());

        // then
        var resultRegistration = candidateJpaRepository.findById(registration.getId()).orElseThrow();
        var resultMember = memberJpaRepository.findById(member.getId()).orElseThrow();

        assertThat(resultRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.WITHDRAWN);
        assertThat(resultRegistration.getWithdrawnAt()).isNotNull();
        assertThat(resultMember.getRole()).isEqualTo(Role.ROLE_MEMBER);
    }

    @Test
    void 활성_신청이_없으면_철회_시_NOT_FOUND_CANDIDATE를_던진다() {
        // given
        var nonExistentMemberId = 999L;

        // when & then
        assertThatThrownBy(() -> candidateManager.withdraw(nonExistentMemberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE.getMessage());
    }

    @Test
    void 활성_신청은_있지만_승인되지_않은_상태면_철회_시_NOT_ALLOW_WITHDRAW_NON_APPROVED를_던진다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        candidateJpaRepository.save(CandidateRegistration.apply(member)); // PENDING 상태

        // when & then
        assertThatThrownBy(() -> candidateManager.withdraw(member.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED.getMessage());
    }

}