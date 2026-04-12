package org.smu.randsome.randsomeback.admin.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.smu.randsome.randsomeback.admin.coupon.dto.CouponEventPreviewItem;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventRegisterRequest;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 쿠폰 이벤트 API", description = "관리자 쿠폰 이벤트 관련 API")
public abstract class CouponEventAdminControllerDocs {

    @Operation(summary = "쿠폰 이벤트 생성",
            description = """
                    ## 새로운 쿠폰 이벤트를 생성합니다.
                    이벤트 이름, 설명, 타입, 총 수량, 리워드 티켓 타입과 수량, 시작 및 종료 시각을 포함한 정보를 요청 본문으로 전달해야 합니다.
                    성공적으로 생성된 경우, 생성된 쿠폰 이벤트의 ID를 반환합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "쿠폰 이벤트가 성공적으로 생성되었습니다.")
    public abstract ApiResponse<Long> registerCouponEvent(
            @RequestBody @Valid CouponEventRegisterRequest request
    );

    @Operation(summary = "쿠폰 이벤트 목록 조회",
            description = """
                    ## 모든 쿠폰 이벤트의 목록을 조회합니다.
                    각 쿠폰 이벤트에 대한 ID, 이름, 타입, 상태, 총 수량, 리워드 티켓 타입과 수량, 시작 및 종료 시각 등의 정보를 포함한 리스트를 반환합니다.
                    """
    )
    public abstract ApiResponse<List<CouponEventPreviewItem>> findCouponEvents();

}