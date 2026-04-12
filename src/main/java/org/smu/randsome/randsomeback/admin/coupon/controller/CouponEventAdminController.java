package org.smu.randsome.randsomeback.admin.coupon.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventDetailItem;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventPreviewItem;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventRegisterRequest;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventUpdateRequest;
import org.smu.randsome.randsomeback.admin.coupon.service.CouponEventAdminService;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @PatchMapping("/v1/admin/coupon-events/{couponEventId}")
    public ApiResponse<Void> updateCouponEvent(
            @PathVariable Long couponEventId,
            @RequestBody @Valid CouponEventUpdateRequest request
    ) {
        couponEventAdminService.updateCouponEvent(couponEventId, request.toUpdateCouponEvent());

        return ApiResponse.success(null);
    }

    @Override
    @DeleteMapping("/v1/admin/coupon-events/{couponEventId}")
    public ApiResponse<?> deleteCouponEvent(@PathVariable Long couponEventId) {
        couponEventAdminService.deleteCouponEvent(couponEventId);

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/admin/coupon-events/{couponEventId}")
    public ApiResponse<CouponEventDetailItem> findCouponEvent(@PathVariable Long couponEventId) {
        CouponEvent event = couponEventAdminService.findCouponEvent(couponEventId);

        return ApiResponse.success(CouponEventDetailItem.from(event));
    }

    @Override
    @GetMapping("/v1/admin/coupon-events")
    public ApiResponse<List<CouponEventPreviewItem>> findCouponEvents() {
        List<CouponEvent> couponEvents = couponEventAdminService.findCouponEvents();

        return ApiResponse.success(CouponEventPreviewItem.from(couponEvents));
    }

    @Override
    @PostMapping("/v1/admin/coupon-events/{couponEventId}/activate")
    public ApiResponse<?> activateCouponEvent(@PathVariable Long couponEventId) {
        couponEventAdminService.activateCouponEvent(couponEventId);

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/admin/coupon-events/{couponEventId}/deactivate")
    public ApiResponse<?> deactivateCouponEvent(@PathVariable Long couponEventId) {
        couponEventAdminService.deactivateCouponEvent(couponEventId);

        return ApiResponse.success();
    }

}