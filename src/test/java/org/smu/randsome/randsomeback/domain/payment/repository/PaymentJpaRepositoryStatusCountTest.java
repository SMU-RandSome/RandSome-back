package org.smu.randsome.randsomeback.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
class PaymentJpaRepositoryStatusCountTest extends IntegrationTestSupport {

    final PaymentJpaRepository paymentJpaRepository;
    final MemberJpaRepository memberJpaRepository;

    @Test
    void 결제_상태별_건수를_정확히_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        paymentJpaRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 1L, 1));
        paymentJpaRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 2L, 1));

        var completed1 = Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 3L, 1);
        completed1.confirm(LocalDateTime.now());
        paymentJpaRepository.save(completed1);

        var completed2 = Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 4L, 1);
        completed2.confirm(LocalDateTime.now());
        paymentJpaRepository.save(completed2);

        var rejected = Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 5L, 1);
        rejected.reject(LocalDateTime.now());
        paymentJpaRepository.save(rejected);

        // when
        List<PaymentStatusCountItem> result =
                paymentJpaRepository.countByPaymentStatusAndStatus(EntityStatus.ACTIVE);

        // then
        assertThat(result)
                .extracting(PaymentStatusCountItem::paymentStatus, PaymentStatusCountItem::count)
                .containsExactlyInAnyOrder(
                        tuple(PaymentStatus.PENDING, 2L),
                        tuple(PaymentStatus.COMPLETED, 2L),
                        tuple(PaymentStatus.REJECTED, 1L)
                );
    }

    @Test
    void 소프트_삭제된_결제는_카운트에_포함되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        paymentJpaRepository.save(Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 1L, 1));

        var deleted = paymentJpaRepository.save(
                Payment.register(member, PaymentType.CANDIDATE_REGISTRATION, 2L, 1));
        deleted.delete();

        // when
        List<PaymentStatusCountItem> result =
                paymentJpaRepository.countByPaymentStatusAndStatus(EntityStatus.ACTIVE);

        // then
        assertThat(result)
                .extracting(PaymentStatusCountItem::paymentStatus, PaymentStatusCountItem::count)
                .containsExactly(tuple(PaymentStatus.PENDING, 1L));
    }

    @Test
    void 결제_내역이_없으면_빈_리스트를_반환한다() {
        // when
        List<PaymentStatusCountItem> result =
                paymentJpaRepository.countByPaymentStatusAndStatus(EntityStatus.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

}
