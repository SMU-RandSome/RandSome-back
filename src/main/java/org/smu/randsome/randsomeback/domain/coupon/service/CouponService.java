package org.smu.randsome.randsomeback.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponReader;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponReader couponReader;

    public CursorSlice<Coupon> findCoupons(Long memberId, CouponSearchCondition condition) {
        return couponReader.findCoupons(memberId, condition);
    }

}