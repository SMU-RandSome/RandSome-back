package org.smu.randsome.randsomeback.admin.statistics.dto.response;

import java.util.List;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;

public record PaymentStatusStatisticsResponse(
        long pendingCount,    // 대기
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