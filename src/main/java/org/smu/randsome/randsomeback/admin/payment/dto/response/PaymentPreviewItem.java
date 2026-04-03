package org.smu.randsome.randsomeback.admin.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithDetails;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;

@Schema(description = "결제 내역 미리보기 정보 DTO")
@Builder(access = AccessLevel.PRIVATE)
public record PaymentPreviewItem(
        @Schema(description = "결제 ID", example = "101")
        Long paymentId,

        @Schema(description = "회원 실명", example = "홍길동")
        String memberName,

        @Schema(description = "결제 유형")
        PaymentType paymentType,

        @Schema(description = "결제 상태")
        PaymentStatus paymentStatus,

        @Schema(description = "결제 금액", example = "9900.00")
        BigDecimal amount,

        @Schema(description = "신청 인원 수 (매칭 신청 결제에만 존재)", example = "2")
        Integer applicationCount,

        @Schema(description = "거절 사유 (결제 상태가 REJECTED일 때만 값 존재)", example = "결제 정보가 유효하지 않습니다.")
        String rejectedReason,

        @Schema(description = "결제 신청 시각")
        LocalDateTime applyAt
) {

    public static PaymentPreviewItem from(PaymentWithDetails paymentWithDetails) {
        Payment payment = paymentWithDetails.payment();
        return PaymentPreviewItem.builder()
                .paymentId(payment.getId())
                .memberName(payment.getMember().getLegalName())
                .paymentType(payment.getPaymentType())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .applicationCount(paymentWithDetails.applicationCount())
                .rejectedReason(paymentWithDetails.rejectedReason())
                .applyAt(payment.getCreatedAt())
                .build();
    }

}
