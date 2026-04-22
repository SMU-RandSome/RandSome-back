package org.smu.randsome.randsomeback.domain.coupon.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

class CouponTest {

    CouponEvent event;
    Member member;
    Coupon coupon;

    @BeforeEach
    void setUp() {
        event = CuponFixture.createCuponEvent();
        event.activate(TestDateTimeUtils.now());
        member = MemberFixture.create();
        coupon = Coupon.issue(event, member);
    }

    @Test
    void 쿠폰을_발급한다() {
        // then
        assertThat(coupon).extracting(
                Coupon::getCouponEvent,
                Coupon::getMember,
                Coupon::getCouponStatus,
                Coupon::getExpiredAt
        ).containsExactly(
                event,
                member,
                CouponStatus.AVAILABLE,
                event.getCouponExpiresAt()
        );
    }

    @Test
    void 쿠폰을_만료시킨다() {
        // when
        coupon.expire();

        // then
        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

    @Test
    void 쿠폰을_사용하면_상태가_USED로_변경된다() {
        // when
        coupon.use(TestDateTimeUtils.now());

        // then
        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    void 이미_사용된_쿠폰을_사용하면_예외가_발생한다() {
        // given
        coupon.use(TestDateTimeUtils.now());

        // when & then
        assertThatThrownBy(() -> coupon.use(TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }

    @Test
    void 쿠폰상태가_변경되지_않았을떄_만료된_쿠폰을_사용하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> coupon.use(CuponFixture.COUPON_EXPIRED_AT.plusSeconds(1)))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }

    @Test
    void 쿠폰_상태가_만료_상태일떄_쿠폰_사용_시_예외가_발생한다() {
        // given
        coupon.expire();
        // when & then
        assertThatThrownBy(() -> coupon.use(TestDateTimeUtils.now()))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }

}