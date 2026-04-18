package org.smu.randsome.randsomeback.domain.coupon.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponReader;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;

class CouponServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponManager couponManager;

    @Mock
    private CouponReader couponReader;

    @Mock
    private TicketHandler ticketHandler;

    @Test
    void 쿠폰_사용_시_CouponManager와_TicketHandler에_각각_위임한다() {
        // given
        var couponId = 1L;
        var memberId = 42L;
        var member = MemberFixture.create();
        var coupon = Coupon.issue(CuponFixture.createActiveCuponEvent(), member);

        given(couponManager.useCoupon(couponId, memberId)).willReturn(coupon);

        // when
        couponService.useCoupon(couponId, memberId);

        // then
        verify(couponManager).useCoupon(couponId, memberId);
        verify(ticketHandler).issueForCoupon(
                memberId,
                CuponFixture.REWARD_TICKET_TYPE,
                CuponFixture.REWARD_TICKET_QUANTITY
        );
    }

}