package org.smu.randsome.randsomeback.admin.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.payment.controller.dto.request.PaymentRejectRequest;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;

@Tag(name = "관리자 결제 관리 API", description = "관리자용 결제 관련 API 문서")
public abstract class PaymentAdminControllerDocs {


    @Operation(
            summary = "결제 확정",
            description = """
                    #### 결제 승인 API입니다.
                    - paymentId에 해당하는 결제를 최종 확정 처리합니다.
                    - 결제가 승인되면 해당 결제의 상태가 '확인'으로 변경됩니다.
                    - 관련 신청서의 상태도 '승인' 으로 변경됩니다.
                    - 후보자 등록 신청일 경우 회원의 role이 후보자로 변경됩니다.
                    - 매칭 신청 승인일 경우 매칭이 진행됩니다.
                    - 결제 승인 후에는 환불이 불가능하므로 신중하게 사용해야 합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.NOT_FOUND_PAYMENT,
            ErrorType.NOT_ALLOW_ALREADY_CONFIRMED_PAYMENT,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<?>> confirm(
            @Parameter(
                    example = "1",
                    description = "승인할 결제의 ID",
                    in = ParameterIn.PATH
            )
            Long paymentId
    );

    @Operation(
            summary = "결제 거절",
            description = """
                    #### 결제 거절 API입니다.
                    - paymentId에 해당하는 결제를 거절 처리합니다.
                    - 결제 상태가 '거절'로 변경됩니다.
                    - 연결된 신청 엔티티 상태도 거절로 변경됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.NOT_FOUND_PAYMENT,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<?>> reject(
            @Parameter(
                    example = "1",
                    description = "거절할 결제의 ID",
                    in = ParameterIn.PATH
            )
            Long paymentId,
            PaymentRejectRequest request
    );

}