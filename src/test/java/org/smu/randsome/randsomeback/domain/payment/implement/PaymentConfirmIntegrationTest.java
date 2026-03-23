package org.smu.randsome.randsomeback.domain.payment.implement;

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
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class PaymentConfirmIntegrationTest extends IntegrationTestSupport {

    final PaymentManager paymentManager;
    final MemberJpaRepository memberJpaRepository;
    final CandidateJpaRepository candidateJpaRepository;
    final PaymentJpaRepository paymentJpaRepository;

    @Test
    void 결제_확인_시_결제_상태와_후보자_등록_상태_및_회원_권한이_함께_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                registration.getId(),
                1
        ));

        // when
        paymentManager.approve(payment.getId());

        // then
        var resultPayment = paymentJpaRepository.findById(payment.getId()).orElseThrow();
        var resultRegistration = candidateJpaRepository.findById(registration.getId()).orElseThrow();
        var resultMember = memberJpaRepository.findById(member.getId()).orElseThrow();

        assertThat(resultPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(resultPayment.getConfirmedAt()).isNotNull();
        assertThat(resultRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(resultRegistration.getApprovedAt()).isNotNull();
        assertThat(resultMember.getRole()).isEqualTo(Role.ROLE_CANDIDATE);
    }

    @Test
    void 결제_거절_시_결제_상태와_후보자_등록_상태가_함께_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                registration.getId(),
                1
        ));
        var reason = "자격 미달";

        // when
        paymentManager.reject(payment.getId(), reason);

        // then
        var resultPayment = paymentJpaRepository.findById(payment.getId()).orElseThrow();
        var resultRegistration = candidateJpaRepository.findById(registration.getId()).orElseThrow();

        assertThat(resultPayment.getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(resultPayment.getRejectedAt()).isNotNull();
        assertThat(resultRegistration.getRegistrationStatus()).isEqualTo(RegistrationStatus.REJECTED);
        assertThat(resultRegistration.getRejectedReason()).isEqualTo(reason);
    }

    @Test
    void 이미_확정된_결제를_거절하면_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                registration.getId(),
                1
        ));
        paymentManager.approve(payment.getId());

        // when & then
        assertThatThrownBy(() -> paymentManager.reject(payment.getId(), "거절 사유"))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_ALLOW_ALREADY_CONFIRMED_PAYMENT.getMessage());
    }

    @Test
    void 후보자_등록이_없으면_결제_확인_시_예외가_발생한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var nonExistentRegistrationId = 999L;
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                nonExistentRegistrationId,
                1
        ));

        // when & then
        // @Transactional 테스트 환경에서 예외 발생 시 트랜잭션이 rollback-only로 마킹되므로
        // DB 상태 검증은 단위 테스트(PaymentManagerUnitTest)에서 핸들러 미호출로 확인한다.
        assertThatThrownBy(() -> paymentManager.approve(payment.getId()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_CANDIDATE.getMessage());
    }

}