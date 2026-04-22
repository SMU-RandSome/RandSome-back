package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;
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
class CouponReaderIntegrationTest extends IntegrationTestSupport {

    private final CouponReader couponReader;
    private final CouponRepository couponRepository;
    private final CouponEventJpaRepository couponEventJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Test
    void findWithEvent_쿠폰_조회_시_CouponEvent를_함께_로딩한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var coupon = couponRepository.save(Coupon.issue(event, member));

        // when
        Coupon result = couponReader.findWithEvent(coupon.getId());

        // then — 트랜잭션 내에서 LazyInitializationException 없이 couponEvent 필드에 접근 가능
        assertThatNoException().isThrownBy(() -> {
            CouponEvent loadedEvent = result.getCouponEvent();
            assertThat(loadedEvent.getId()).isEqualTo(event.getId());
            assertThat(loadedEvent.getRewardTicketType()).isEqualTo(CuponFixture.REWARD_TICKET_TYPE);
            assertThat(loadedEvent.getRewardTicketAmount()).isEqualTo(CuponFixture.REWARD_TICKET_QUANTITY);
        });
    }

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
        usedCoupon.use(TestDateTimeUtils.now());
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
        usedCoupon.use(TestDateTimeUtils.now());
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

    @Test
    void 만료_대상_쿠폰을_정상적으로_조회한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var now = TestDateTimeUtils.now();

        // 1. 만료된 이벤트에 속한 쿠폰 (만료 대상)
        var expiredEvent1 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(expiredEvent1, "couponExpiresAt", now.minusDays(1));
        couponEventJpaRepository.save(expiredEvent1);

        var expirableCoupon = Coupon.issue(expiredEvent1, member);
        couponRepository.save(expirableCoupon);

        // 2. 아직 만료되지 않은 쿠폰 (조회 제외)
        var activeEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(activeEvent, "couponExpiresAt", now.plusDays(1));
        couponEventJpaRepository.save(activeEvent);

        var activeCoupon = Coupon.issue(activeEvent, member);
        couponRepository.save(activeCoupon);

        // 3. 이미 만료 처리된 쿠폰 (조회 제외)
        var expiredEvent2 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(expiredEvent2, "couponExpiresAt", now.minusDays(1));
        couponEventJpaRepository.save(expiredEvent2);

        var alreadyExpiredCoupon = Coupon.issue(expiredEvent2, member);
        alreadyExpiredCoupon.expire();
        couponRepository.save(alreadyExpiredCoupon);

        // 4. 사용 완료된 쿠폰 (조회 제외)
        var expiredEvent3 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(expiredEvent3, "couponExpiresAt", now.minusDays(1));
        couponEventJpaRepository.save(expiredEvent3);

        var usedCoupon = Coupon.issue(expiredEvent3, member);
        usedCoupon.use(now.minusDays(2));
        couponRepository.save(usedCoupon);

        // when
        List<Coupon> expirableCoupons = couponReader.findExpirable(now);

        // then
        assertThat(expirableCoupons).hasSize(1);
        assertThat(expirableCoupons.getFirst().getId()).isEqualTo(expirableCoupon.getId());
    }

}