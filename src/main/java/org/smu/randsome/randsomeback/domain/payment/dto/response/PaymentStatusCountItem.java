package org.smu.randsome.randsomeback.domain.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;

@Schema(description = "결제 상태별 건수 항목")
public record PaymentStatusCountItem(
        @Schema(description = "결제 상태")
        PaymentStatus paymentStatus,

        @Schema(description = "건수", example = "5")
        long count
) {

}