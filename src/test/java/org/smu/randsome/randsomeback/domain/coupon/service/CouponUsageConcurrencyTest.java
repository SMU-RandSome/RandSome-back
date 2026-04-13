package org.smu.randsome.randsomeback.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketHistoryJpaRepository;
import org.smu.randsome.randsomeback.domain.ticket.repository.TicketJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * 쿠폰 사용 동시성 테스트
 *
 * 주의: @Transactional을 붙이지 않는다.
 * 각 스레드가 독립된 트랜잭션을 시작해야 낙관적 락 충돌이 발생한다.
 * @Transactional이 있으면 내부 호출이 같은 트랜잭션에 참여해 충돌이 일어나지 않는다.
 */
@RequiredArgsConstructor
class CouponUsageConcurrencyTest extends IntegrationTestSupport {

    final CouponService couponService;
    final CouponRepository couponRepository;
    final CouponEventJpaRepository couponEventJpaRepository;
    final MemberJpaRepository memberJpaRepository;
    final TicketJpaRepository ticketJpaRepository;
    final TicketHistoryJpaRepository ticketHistoryJpaRepository;

    @AfterEach
    void tearDown() {
        ticketHistoryJpaRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();
        ticketJpaRepository.deleteAllInBatch();
        couponEventJpaRepository.deleteAllInBatch();
        memberJpaRepository.deleteAllInBatch();
    }

    @Test
    void 동일_쿠폰_동시_사용_시_한_번만_성공한다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, member));
        ticketJpaRepository.save(Ticket.create(member, CuponFixture.REWARD_TICKET_TYPE, 0));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger lockConflictCount = new AtomicInteger(0);
        List<Exception> unexpectedErrors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    couponService.useCoupon(coupon.getId(), member.getId());
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    // @Version 충돌: 낙관적 락이 정상 동작한 것
                    lockConflictCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedErrors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(unexpectedErrors)
                .as("예상치 못한 예외 발생: %s", unexpectedErrors)
                .isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(lockConflictCount.get()).isEqualTo(1);
    }

    @Test
    void 동일_쿠폰_동시_사용_시_티켓은_정확히_한_번_지급된다() throws InterruptedException {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, member));
        int initialQuantity = 5;
        ticketJpaRepository.save(Ticket.create(member, CuponFixture.REWARD_TICKET_TYPE, initialQuantity));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> unexpectedErrors = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    couponService.useCoupon(coupon.getId(), member.getId());
                } catch (ObjectOptimisticLockingFailureException ignored) {
                    // 낙관적 락 충돌 — 정상 동작
                } catch (Exception e) {
                    unexpectedErrors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(unexpectedErrors)
                .as("예상치 못한 예외 발생: %s", unexpectedErrors)
                .isEmpty();

        // 쿠폰 상태는 USED 한 번만
        var resultCoupon = couponRepository.findByIdAndStatusWithEvent(
                coupon.getId(), EntityStatus.ACTIVE).orElseThrow();
        assertThat(resultCoupon.getCouponStatus()).isEqualTo(CouponStatus.USED);

        // 티켓은 rewardAmount만큼만 증가 (이중 지급 없음)
        var resultTicket = ticketJpaRepository.findByMemberIdAndTicketTypeAndStatus(
                member.getId(), CuponFixture.REWARD_TICKET_TYPE, EntityStatus.ACTIVE).orElseThrow();
        assertThat(resultTicket.getQuantityValue())
                .isEqualTo(initialQuantity + CuponFixture.REWARD_TICKET_QUANTITY);
    }

}
