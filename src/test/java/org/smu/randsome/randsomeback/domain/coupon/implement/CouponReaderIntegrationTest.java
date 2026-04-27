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
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
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
    void 해당_이벤트에서_회원이_발급받은_쿠폰이_있으면_true를_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        couponRepository.save(Coupon.issue(event, member));

        // when
        boolean result = couponReader.hasIssuedCoupon(event.getId(), member.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 해당_이벤트에서_회원이_발급받은_쿠폰이_없으면_false를_반환한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());

        // when
        boolean result = couponReader.hasIssuedCoupon(event.getId(), member.getId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 특정_이벤트에서_쿠폰을_발급받은_회원_목록을_최신순으로_조회한다() {
        // given
        var member1 = memberJpaRepository.save(MemberFixture.createWithLegalName("202310001@sangmyung.kr", "회원1"));
        var member2 = memberJpaRepository.save(MemberFixture.createWithLegalName("202310002@sangmyung.kr", "회원2"));
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());

        var coupon1 = couponRepository.save(Coupon.issue(event, member1));
        var coupon2 = couponRepository.save(Coupon.issue(event, member2));

        // when
        var result = couponReader.findIssuedCoupons(event.getId(), Cursor.of(null, 10));

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).extracting(Coupon::getId)
                .containsExactly(coupon2.getId(), coupon1.getId());
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void 다른_이벤트의_쿠폰은_포함되지_않는다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event1 = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        var event2 = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());

        couponRepository.save(Coupon.issue(event1, member));

        // when
        var result = couponReader.findIssuedCoupons(event2.getId(), Cursor.of(null, 10));

        // then
        assertThat(result.items()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 발급_회원_조회_시_커서_기반_페이지네이션이_동작한다() {
        // given
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        for (int i = 1; i <= 11; i++) {
            var member = memberJpaRepository.save(
                    MemberFixture.createWithLegalName("20231%04d@sangmyung.kr".formatted(i), "회원" + i));
            couponRepository.save(Coupon.issue(event, member));
        }

        // when — 첫 페이지 (size=10)
        var firstPage = couponReader.findIssuedCoupons(event.getId(), Cursor.of(null, 10));

        // then
        assertThat(firstPage.items()).hasSize(10);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isNotNull();

        // when — 두 번째 페이지
        var secondPage = couponReader.findIssuedCoupons(event.getId(), Cursor.of(firstPage.nextCursor(), 10));

        // then
        assertThat(secondPage.items()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void 발급_회원_조회_시_Member를_함께_로딩한다() {
        // given
        var member = memberJpaRepository.save(MemberFixture.create());
        var event = couponEventJpaRepository.save(CuponFixture.createActiveCuponEvent());
        couponRepository.save(Coupon.issue(event, member));

        // when
        var result = couponReader.findIssuedCoupons(event.getId(), Cursor.of(null, 10));

        // then — fetchJoin으로 Member 필드에 LazyInitializationException 없이 접근 가능
        assertThatNoException().isThrownBy(() -> {
            var loadedMember = result.items().getFirst().getMember();
            assertThat(loadedMember.getId()).isEqualTo(member.getId());
            assertThat(loadedMember.getLegalName()).isEqualTo(MemberFixture.DEFAULT_LEGAL_NAME);
        });
    }

}