package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

/**
 * 결제 승인/거절 동시성 테스트
 *
 * 주의: @Transactional을 붙이지 않는다.
 * 각 스레드가 독립된 트랜잭션을 시작해야 낙관적 락 충돌이 발생한다.
 * @Transactional이 있으면 내부 호출이 같은 트랜잭션에 참여해 충돌이 일어나지 않는다.
 */
@RequiredArgsConstructor
class PaymentConcurrencyTest extends IntegrationTestSupport {

    final PaymentManager paymentManager;
    final MemberJpaRepository memberJpaRepository;
    final CandidateJpaRepository candidateJpaRepository;
    final PaymentJpaRepository paymentJpaRepository;

    @AfterEach
    void tearDown() {
        paymentJpaRepository.deleteAllInBatch();
        candidateJpaRepository.deleteAllInBatch();
        memberJpaRepository.deleteAllInBatch();
    }

    @Test
    void 두_관리자가_같은_결제를_동시에_승인하면_한_명만_성공한다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                registration.getId(),
                1
        ));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    paymentManager.approve(payment.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        var resultPayment = paymentJpaRepository.findById(payment.getId()).orElseThrow();
        assertThat(resultPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void 두_관리자가_같은_결제를_동시에_거절하면_한_명만_성공한다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                registration.getId(),
                1
        ));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    paymentManager.reject(payment.getId(), "거절 사유");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        var resultPayment = paymentJpaRepository.findById(payment.getId()).orElseThrow();
        assertThat(resultPayment.getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void 승인과_거절이_동시에_들어오면_하나만_처리되고_결제_상태는_일관성을_유지한다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var registration = candidateJpaRepository.save(CandidateRegistration.apply(member));
        var payment = paymentJpaRepository.save(Payment.register(
                member,
                PaymentType.CANDIDATE_REGISTRATION,
                registration.getId(),
                1
        ));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                startLatch.await();
                paymentManager.approve(payment.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                paymentManager.reject(payment.getId(), "거절 사유");
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then — 성공/실패 합계가 반드시 2
        assertThat(successCount.get() + failCount.get()).isEqualTo(2);
        assertThat(successCount.get()).isEqualTo(1);

        // 결제 상태는 COMPLETED 또는 REJECTED 중 하나여야 하며 PENDING이 아니어야 한다
        var resultPayment = paymentJpaRepository.findById(payment.getId()).orElseThrow();
        assertThat(resultPayment.getPaymentStatus())
                .isIn(PaymentStatus.COMPLETED, PaymentStatus.REJECTED)
                .isNotEqualTo(PaymentStatus.PENDING);
    }

}
