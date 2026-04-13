package org.smu.randsome.randsomeback.domain.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.domain.coupon.dto.response.CouponItem;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponFilterType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;

@Tag(name = "쿠폰 API", description = "쿠폰 관련 Docs")
public abstract class CouponControllerDocs {

    @Operation(summary = "쿠폰 목록 조회 - JWT [O]",
            description = """
                    ## 쿠폰 목록 조회 API
                    - 로그인한 회원의 쿠폰 목록을 조회하는 API입니다.
                    - 쿠폰 상태에 따라 필터링하여 조회할 수 있습니다.
                    - 페이지네이션은 커서 기반으로 구현되어 있습니다.
                    - `filter` : 쿠폰 상태 필터 (AVAILABLE, USED, EXPIRED)
                    - `lastCouponId` : 마지막으로 조회한 쿠폰 ID (cursor pagination)
                    - `size` : 한 페이지당 조회할 쿠폰 수
                    """
    )
    public abstract ApiResponse<CursorSlice<CouponItem>> findCoupons(
            Long memberId,
            CouponFilterType filter,
            Long lastCouponId,
            int size
    );

}