package org.smu.randsome.randsomeback.domain.cupon.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.domain.cupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class CuponTest {

    CouponEvent event;
    Member member;
    Cupon cupon;

    @BeforeEach
    void setUp() {
        event = CuponFixture.createCuponEvent();
        event.activate();
        member = MemberFixture.create();
        cupon = Cupon.issue(event, member);
    }

    @Test
    void 쿠폰을_발급한다() {
        // then
        assertThat(cupon).extracting(
                Cupon::getCouponEvent,
                Cupon::getMember,
                Cupon::getCouponStatus
        ).containsExactly(
                event,
                member,
                CouponStatus.AVAILABLE
        );
    }

    @Test
    void 쿠폰을_만료시킨다() {
        // when
        cupon.expire();

        // then
        assertThat(cupon.getCouponStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

    @Test
    void 쿠폰을_사용한다() {
        // given

        // when
        cupon.use();

        // then
        assertThat(cupon.getCouponStatus()).isEqualTo(CouponStatus.USED);

        assertThatThrownBy(() -> cupon.use())
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }
}