package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class CouponReaderIntegrationTest extends IntegrationTestSupport {

    private final CouponReader couponReader;
    private final CouponRepository couponRepository;
    private final CouponEventJpaRepository couponEventJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Test
    void 회원의_모든_쿠폰을_최신순으로_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event1 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event2 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event3 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());

        var coupon1 = couponRepository.save(Coupon.issue(event1, member));
        var coupon2 = couponRepository.save(Coupon.issue(event2, member));
        var coupon3 = couponRepository.save(Coupon.issue(event3, member));

        CouponSearchCondition condition = new CouponSearchCondition(CouponFilterType.ALL, null, 10);

        // when
        var result = couponReader.findCoupons(member.getId(), condition);

        // then
        assertThat(result.items()).hasSize(3);
        assertThat(result.items()).extracting(Coupon::getId)
                .containsExactly(coupon3.getId(), coupon2.getId(), coupon1.getId());
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 사용_가능한_쿠폰만_필터링하여_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event1 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event2 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());

        var availableCoupon = couponRepository.save(Coupon.issue(event1, member));
        var usedCoupon = couponRepository.save(Coupon.issue(event2, member));
        usedCoupon.use();
        couponRepository.save(usedCoupon);

        CouponSearchCondition condition = new CouponSearchCondition(CouponFilterType.AVAILABLE, null, 10);

        // when
        var result = couponReader.findCoupons(member.getId(), condition);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().getId()).isEqualTo(availableCoupon.getId());
    }

    @Test
    void 사용_완료_또는_만료된_쿠폰만_필터링하여_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event1 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event2 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event3 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());

        var availableCoupon = couponRepository.save(Coupon.issue(event1, member));
        var usedCoupon = couponRepository.save(Coupon.issue(event2, member));
        usedCoupon.use();
        couponRepository.save(usedCoupon);

        var expiredCoupon = couponRepository.save(Coupon.issue(event3, member));
        expiredCoupon.expire();
        couponRepository.save(expiredCoupon);

        CouponSearchCondition condition = new CouponSearchCondition(CouponFilterType.USED_OR_EXPIRED, null, 10);

        // when
        var result = couponReader.findCoupons(member.getId(), condition);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).extracting(Coupon::getId)
                .containsExactly(expiredCoupon.getId(), usedCoupon.getId());
    }

    @Test
    void 커서_기반_페이징이_정상적으로_동작한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event1 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event2 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());
        var event3 = couponEventJpaRepository.save(CuponFixture.createCuponEvent());

        var coupon1 = couponRepository.save(Coupon.issue(event1, member));
        var coupon2 = couponRepository.save(Coupon.issue(event2, member));
        var coupon3 = couponRepository.save(Coupon.issue(event3, member));

        CouponSearchCondition condition = new CouponSearchCondition(CouponFilterType.ALL, coupon3.getId(), 1);

        // when
        var result = couponReader.findCoupons(member.getId(), condition);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().getId()).isEqualTo(coupon2.getId());
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(coupon2.getId());
    }

}