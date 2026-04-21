package org.smu.randsome.randsomeback.domain.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventDetailItem;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventPreviewItem;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;

@Tag(name = "쿠폰 이벤트 API", description = "쿠폰 이벤트 조회 및 쿠폰 발급 관련 API")
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

    @Operation(summary = "쿠폰 이벤트 상세 조회",
            description = """
                    ## 쿠폰 이벤트 단건 상세 정보를 조회합니다.
                    """
    )
    public abstract ApiResponse<CouponEventDetailItem> findCouponEvent(Long couponEventId);

    @Operation(summary = "쿠폰 이벤트 목록 조회",
            description = """
                    ## 모든 쿠폰 이벤트의 목록을 조회합니다.
                    각 쿠폰 이벤트에 대한 ID, 이름, 타입, 상태, 총 수량, 리워드 티켓 타입과 수량, 시작 및 종료 시각 등의 정보를 포함한 리스트를 반환합니다.
                    """
    )
    public abstract ApiResponse<List<CouponEventPreviewItem>> findCouponEvents();

}