package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * CouponManager.issueCoupon 발급 및 보상 로직 통합 테스트.
 *
 * <p>@Transactional을 클래스에 붙이지 않는다.
 * issueCoupon이 독립 트랜잭션을 시작하고 완료해야
 * TransactionSynchronization.afterCompletion(STATUS_ROLLED_BACK)이 테스트 내에서 발동한다.
 */
@RequiredArgsConstructor
class CouponManagerIssuanceIntegrationTest extends IntegrationTestSupport {

    final CouponManager couponManager;
    final CouponRepository couponRepository;
    final CouponEventJpaRepository couponEventJpaRepository;
    final MemberJpaRepository memberJpaRepository;
    final RedisRepository redisRepository;
    final StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void tearDown() {
        couponRepository.deleteAllInBatch();
        couponEventJpaRepository.deleteAllInBatch();
        memberJpaRepository.deleteAllInBatch();
        Objects.requireNonNull(stringRedisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    // ── 정상 발급 ─────────────────────────────────────────────────────

    @Test
    void 발급_성공_시_DB에_쿠폰이_저장되고_Redis_stock이_감소한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        initStock(event.getId(), 10);

        // when
        Long couponId = couponManager.issueCoupon(event.getId(), member.getId(), LocalDateTime.now());

        // then
        assertThat(couponRepository.findById(couponId)).isPresent();
        assertThat(getStock(event.getId())).isEqualTo("9");
        assertThat(getMemberLock(event.getId(), member.getId())).isNotNull();
    }

    // ── Redis lock 차단 (TTL 유효 기간 내 중복) ───────────────────────

    @Test
    void 발급_성공_직후_동일_멤버가_재시도하면_Redis_lock이_차단하고_stock은_변하지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        initStock(event.getId(), 10);

        couponManager.issueCoupon(event.getId(), member.getId(), LocalDateTime.now()); // 1차 발급 성공

        // when: 2차 시도 — Redis lock이 살아 있는 동안
        assertThatThrownBy(() -> couponManager.issueCoupon(event.getId(), member.getId(), LocalDateTime.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ALREADY_ISSUED_COUPON.getMessage());

        // then: lock 차단으로 decrementStock이 호출되지 않음 — stock은 1차 발급 후 그대로(9)
        assertThat(getStock(event.getId())).isEqualTo("9");
        assertThat(getMemberLock(event.getId(), member.getId())).isNotNull();
    }

    // ── TransactionSynchronization 보상 (DB unique constraint 위반) ───

    /**
     * 시나리오: TTL이 만료돼 Redis lock이 사라진 상태에서 재시도.
     * DB에 이미 쿠폰이 존재하므로 saveAndFlush가 DataIntegrityViolationException을 던진다.
     * 트랜잭션이 롤백되면 afterCompletion(STATUS_ROLLED_BACK)이 발동해
     * 감소했던 stock을 복원하고 새로 설정된 lock을 해제한다.
     */
    @Test
    void TTL_만료_후_재시도_시_DB_unique_constraint_위반으로_ALREADY_ISSUED_예외_발생_후_Redis_stock이_복원된다() {
        // given: DB에 이미 쿠폰이 있고, Redis lock은 없는 상태(TTL 만료 시뮬레이션)
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        couponRepository.save(Coupon.issue(event, member)); // 이미 발급됨
        initStock(event.getId(), 10);                       // lock 없음

        // when
        assertThatThrownBy(() -> couponManager.issueCoupon(event.getId(), member.getId(), LocalDateTime.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ALREADY_ISSUED_COUPON.getMessage());

        // then: afterCompletion(STATUS_ROLLED_BACK) 보상 — stock 복원, lock 해제
        assertThat(getStock(event.getId())).isEqualTo("10");
        assertThat(getMemberLock(event.getId(), member.getId())).isNull();
    }

    // ── 즉시 보상 (재고 소진) ─────────────────────────────────────────

    /**
     * 시나리오: Redis stock이 0인 상태에서 발급 시도.
     * decrementStockOrThrow 내부에서 stock이 음수가 되면 즉시 compensate()를 호출한다.
     * TransactionSynchronization은 등록되지 않으며 보상은 트랜잭션과 무관하게 즉시 실행된다.
     */
    @Test
    void 재고_소진_시_COUPON_SOLD_OUT_예외_발생_후_즉시_보상으로_stock이_복원되고_lock이_해제된다() {
        // given: stock = 0
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        initStock(event.getId(), 0);

        // when
        assertThatThrownBy(() -> couponManager.issueCoupon(event.getId(), member.getId(), LocalDateTime.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_SOLD_OUT.getMessage());

        // then: 즉시 compensate() — stock 0→-1→0(복원), lock 획득 후 즉시 해제
        assertThat(getStock(event.getId())).isEqualTo("0");
        assertThat(getMemberLock(event.getId(), member.getId())).isNull();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    private void initStock(Long eventId, int quantity) {
        redisRepository.put(CacheKeys.couponStock(eventId), String.valueOf(quantity), Duration.ofHours(1));
    }

    private String getStock(Long eventId) {
        return redisRepository.get(CacheKeys.couponStock(eventId));
    }

    private String getMemberLock(Long eventId, Long memberId) {
        return redisRepository.get(CacheKeys.couponMemberLock(eventId, memberId));
    }
}
