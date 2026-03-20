package org.smu.randsome.randsomeback.admin.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;

@Schema(description = "결제 상태별 건수 응답")
public record PaymentStatusStatisticsResponse(
        @Schema(description = "대기 건수", example = "3")
        long pendingCount,    // 대기

        @Schema(description = "처리 완료 건수 (승인 + 거절 합산)", example = "7")
        long processedCount   // 승인 + 거절 합산
) {

    public static PaymentStatusStatisticsResponse from(List<PaymentStatusCountItem> items) {
        long pendingCount = items.stream()
                .filter(item -> item.paymentStatus().isPending())
                .mapToLong(PaymentStatusCountItem::count)
                .sum();

        long processedCount = items.stream()
                .filter(item -> !item.paymentStatus().isPending())
                .mapToLong(PaymentStatusCountItem::count)
                .sum();

        return new PaymentStatusStatisticsResponse(pendingCount, processedCount);
    }

}