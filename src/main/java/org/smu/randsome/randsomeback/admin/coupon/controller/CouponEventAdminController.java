package org.smu.randsome.randsomeback.admin.coupon.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventPreviewItem;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventRegisterRequest;
import org.smu.randsome.randsomeback.admin.coupon.service.CouponEventAdminService;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponEventAdminController extends CouponEventAdminControllerDocs {

    private final CouponEventAdminService couponEventAdminService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/admin/coupon-events")
    public ApiResponse<Long> registerCouponEvent(
            @RequestBody @Valid CouponEventRegisterRequest request
    ) {
        Long couponEventId = couponEventAdminService.registerCouponEvent(request.toNewCouponEvent());

        return ApiResponse.success(couponEventId);
    }

    @Override
    @GetMapping("/v1/admin/coupon-events")
    public ApiResponse<List<CouponEventPreviewItem>> findCouponEvents() {
        List<CouponEvent> couponEvents = couponEventAdminService.findCouponEvents();

        return ApiResponse.success(CouponEventPreviewItem.from(couponEvents));
    }

}