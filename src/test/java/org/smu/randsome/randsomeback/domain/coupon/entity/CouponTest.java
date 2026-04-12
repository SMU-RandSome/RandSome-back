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

class CouponTest {

    CouponEvent event;
    Member member;
    Coupon coupon;

    @BeforeEach
    void setUp() {
        event = CuponFixture.createCuponEvent();
        event.activate();
        member = MemberFixture.create();
        coupon = Coupon.issue(event, member);
    }

    @Test
    void 쿠폰을_발급한다() {
        // then
        assertThat(coupon).extracting(
                Coupon::getCouponEvent,
                Coupon::getMember,
                Coupon::getCouponStatus
        ).containsExactly(
                event,
                member,
                CouponStatus.AVAILABLE
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
    void 쿠폰을_사용한다() {
        // given

        // when
        coupon.use();

        // then
        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.USED);

        assertThatThrownBy(() -> coupon.use())
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }
}