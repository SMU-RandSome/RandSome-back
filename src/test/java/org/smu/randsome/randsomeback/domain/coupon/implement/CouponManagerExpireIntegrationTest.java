package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class CouponManagerExpireIntegrationTest extends IntegrationTestSupport {

    private final CouponManager couponManager;
    private final CouponRepository couponRepository;
    private final CouponEventJpaRepository couponEventJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Test
    void 벌크_만료_처리_시_AVAILABLE_상태의_만료_대상_쿠폰만_EXPIRED로_변경된다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var now = TestDateTimeUtils.now();

        // 1. 만료 대상 쿠폰 (AVAILABLE + expiredAt < now)
        var expiredEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(expiredEvent, "couponExpiresAt", now.minusDays(1));
        couponEventJpaRepository.save(expiredEvent);
        var expirableCoupon = couponRepository.save(Coupon.issue(expiredEvent, member));

        // 2. 아직 만료되지 않은 쿠폰 (AVAILABLE + expiredAt > now)
        var activeEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(activeEvent, "couponExpiresAt", now.plusDays(1));
        couponEventJpaRepository.save(activeEvent);
        var activeCoupon = couponRepository.save(Coupon.issue(activeEvent, member));

        // 3. 이미 사용된 쿠폰 (USED + expiredAt < now)
        var usedEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(usedEvent, "couponExpiresAt", now.minusDays(1));
        couponEventJpaRepository.save(usedEvent);
        var usedCoupon = Coupon.issue(usedEvent, member);
        usedCoupon.use(now.minusDays(2));
        couponRepository.save(usedCoupon);

        // when
        int expiredCount = couponManager.expireBatch(now);

        // then
        assertThat(expiredCount).isEqualTo(1);

        Coupon reloaded = couponRepository.findById(expirableCoupon.getId()).orElseThrow();
        assertThat(reloaded.getCouponStatus()).isEqualTo(CouponStatus.EXPIRED);
        assertThat(reloaded.getUpdatedAt()).isNotNull();

        Coupon stillActive = couponRepository.findById(activeCoupon.getId()).orElseThrow();
        assertThat(stillActive.getCouponStatus()).isEqualTo(CouponStatus.AVAILABLE);
    }

}
