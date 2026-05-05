package org.smu.randsome.randsomeback.domain.coupon.dto.command;

import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;

public record CouponSearchCondition(
        CouponFilterType filter,
        Long lastCouponId, // cursor (null이면 처음부터 조회)
        int size
) {

}