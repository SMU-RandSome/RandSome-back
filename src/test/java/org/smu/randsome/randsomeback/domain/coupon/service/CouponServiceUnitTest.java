package org.smu.randsome.randsomeback.domain.coupon.service;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponReader;

class CouponServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponReader couponReader;

    @Test
    void 쿠폰_목록_조회_시_Reader에게_위임한다() {
        // given
        Long memberId = 1L;
        CouponSearchCondition condition = new CouponSearchCondition(CouponFilterType.ALL, null, 20);

        // when
        couponService.findCoupons(memberId, condition);

        // then
        then(couponReader).should().findCoupons(memberId, condition);
    }

}