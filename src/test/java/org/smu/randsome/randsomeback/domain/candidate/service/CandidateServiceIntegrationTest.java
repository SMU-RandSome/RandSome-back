package org.smu.randsome.randsomeback.domain.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
class CandidateServiceIntegrationTest extends IntegrationTestSupport {

    final CandidateService candidateService;
    final MemberJpaRepository memberJpaRepository;
    final CandidateJpaRepository candidateJpaRepository;
    final PlatformTransactionManager transactionManager;
    final EntityManagerFactory entityManagerFactory;
    final DataSource dataSource;

    @MockitoBean
    PaymentManager paymentManager;

    @AfterEach
    void tearDown() {
        candidateJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    /**
     * 같은 멤버로 동시에 두 번 신청할 때 Pessimistic Lock(SELECT FOR UPDATE)이
     * check-then-insert 구간을 직렬화하여 정확히 하나의 신청만 저장됨을 검증한다.
     */
    @Test
    void 동시에_같은_멤버가_두_번_신청하면_하나만_성공한다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());

        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    candidateService.apply(member.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
        assertThat(candidateJpaRepository.count()).isEqualTo(1);
    }

    /**
     * Thread A가 member 행의 X락을 보유하는 동안 Thread B가 timeout=0(NOWAIT)으로
     * 동일 행에 락 획득을 시도하면 LockTimeoutException이 발생함을 검증한다.
     *
     * EntityManagerFactory로 독립된 커넥션을 생성해야 서로 다른 트랜잭션이 락을 경쟁한다.
     */
    @Test
    void 락을_보유한_행에_NOWAIT으로_재획득_시도하면_예외가_발생한다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var txTemplate = new TransactionTemplate(transactionManager);

        CountDownLatch lockAcquiredLatch = new CountDownLatch(1);
        AtomicReference<Throwable> caughtException = new AtomicReference<>();

        // Thread A: Spring Data Repository로 X락을 획득하고 2초간 유지
        Thread threadA = new Thread(() ->
            txTemplate.execute(status -> {
                memberJpaRepository.findByIdAndStatusWithLock(member.getId(), EntityStatus.ACTIVE);
                lockAcquiredLatch.countDown();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            })
        );

        // Thread B: JDBC 네이티브 SQL로 FOR UPDATE NOWAIT 시도
        // Hibernate가 H2에 NOWAIT 힌트를 전달하지 않으므로 JDBC로 직접 실행
        Thread threadB = new Thread(() -> {
            try {
                lockAcquiredLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try (var conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                try (var ps = conn.prepareStatement(
                        "SELECT id FROM member WHERE id = ? FOR UPDATE NOWAIT")) {
                    ps.setLong(1, member.getId());
                    ps.executeQuery();
                    conn.commit();
                } catch (SQLException lockEx) {
                    caughtException.set(lockEx);
                    conn.rollback();
                }
            } catch (Exception e) {
                caughtException.set(e);
            }
        });

        // when
        threadA.start();
        threadB.start();
        threadA.join(10_000);
        threadB.join(10_000);
        assertThat(threadA.isAlive()).isFalse();
        assertThat(threadB.isAlive()).isFalse();

        // then
        assertThat(caughtException.get())
                .isNotNull()
                .isInstanceOf(SQLException.class);
    }

}