package org.smu.randsome.randsomeback.admin.payment.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "결제 거절 요청 DTO",
        description = "관리자가 결제를 거절할 때 요청하는 DTO입니다."
)
public record PaymentRejectRequest(
        @Schema(description = "결제 거절 사유", example = "결제 정보가 유효하지 않습니다.")
        @NotBlank(message = "거절 사유를 입력해주세요.")
        String rejectedReason
) {

}