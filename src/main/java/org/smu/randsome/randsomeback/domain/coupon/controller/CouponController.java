package org.smu.randsome.randsomeback.domain.coupon.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.dto.response.CouponItem;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;
import org.smu.randsome.randsomeback.domain.coupon.service.CouponService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponController extends CouponControllerDocs {

    private final CouponService couponService;

    @Override
    @PostMapping("/v1/coupons/{couponId}/use")
    public ApiResponse<?> useCoupon(
            @LoginMember Long memberId,
            @PathVariable Long couponId
    ) {
        couponService.useCoupon(couponId, memberId);

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/coupons")
    public ApiResponse<CursorSlice<CouponItem>> findCoupons(
            @LoginMember Long memberId,
            @RequestParam(defaultValue = "ALL") CouponFilterType filter,
            @RequestParam(required = false) Long lastCouponId,
            @RequestParam(defaultValue = "20") @Positive @Max(30) int size
    ) {
        CursorSlice<Coupon> coupons = couponService.findCoupons(memberId, new CouponSearchCondition(filter, lastCouponId, size));

        CursorSlice<CouponItem> response = CursorSlice.of(
                coupons.items().stream()
                        .map(CouponItem::from)
                        .toList(),
                coupons.nextCursor(),
                coupons.hasNext()
        );

        return ApiResponse.success(response);
    }

}