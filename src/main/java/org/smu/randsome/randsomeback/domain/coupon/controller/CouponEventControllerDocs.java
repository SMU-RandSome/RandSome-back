package org.smu.randsome.randsomeback.domain.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;

public abstract class CouponEventControllerDocs {

    @Operation(summary = "쿠폰 발급 - JWT [O]",
            description = """
                    ### 이벤트에 해당하는 쿠폰을 발급합니다.
                    - 이벤트는 사전에 관리자에 의해 등록되어 있어야 하며, 유효한 기간 내에 있어야 합니다.
                    - 이미 발급된 쿠폰이 있는 경우 중복 발급되지 않습니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.ALREADY_ISSUED_COUPON,
            ErrorType.COUPON_EVENT_NOT_ACTIVE,
            ErrorType.COUPON_EVENT_INVALID_STATUS,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<Long> issue(
            Long eventId,
            Long memberId
    );

}