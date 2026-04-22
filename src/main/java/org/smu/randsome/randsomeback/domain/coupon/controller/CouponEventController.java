package org.smu.randsome.randsomeback.domain.coupon.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventDetailItem;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventPreviewItem;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.service.CouponEventService;
import org.smu.randsome.randsomeback.domain.coupon.service.CouponService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponEventController extends CouponEventControllerDocs {

    private final CouponEventService couponEventService;
    private final CouponService couponService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/coupon-events/{couponEventId}/issue")
    public ApiResponse<Long> issue(
            @PathVariable Long couponEventId,
            @LoginMember Long memberId
    ) {
        Long couponId = couponEventService.publishCouponFromEvent(couponEventId, memberId);

        return ApiResponse.success(couponId);
    }

    @Override
    @GetMapping("/v1/coupon-events")
    public ApiResponse<List<CouponEventPreviewItem>> findCouponEvents() {
        List<CouponEvent> couponEvents = couponEventService.findCouponEvents();

        return ApiResponse.success(CouponEventPreviewItem.from(couponEvents));
    }

    @Override
    @GetMapping("/v1/coupon-events/{couponEventId}")
    public ApiResponse<CouponEventDetailItem> findCouponEvent(
            @PathVariable Long couponEventId,
            @LoginMember Long memberId
    ) {
        CouponEvent event = couponEventService.findCouponEvent(couponEventId);
        boolean isIssuable = couponService.isIssuable(couponEventId, memberId);

        return ApiResponse.success(CouponEventDetailItem.of(event, isIssuable));
    }

}