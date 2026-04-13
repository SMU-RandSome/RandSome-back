package org.smu.randsome.randsomeback.domain.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.service.CouponEventService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponEventController extends CouponEventControllerDocs {

    private final CouponEventService couponEventService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/coupon-events/{couponEventId}/issue")
    public ApiResponse<Long> issue(
            @PathVariable Long couponEventId,
            @LoginMember Long memberId
    ) {
        Long couponId= couponEventService.publishCouponFromEvent(couponEventId, memberId);

        return ApiResponse.success(couponId);
    }

}