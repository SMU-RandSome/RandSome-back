package org.smu.randsome.randsomeback.admin.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;

@Schema(description = "쿠폰 이벤트 미리보기 항목")
@Builder
public record CouponEventPreviewItem(
        @Schema(description = "쿠폰 이벤트 ID", example = "1")
        Long id,

        @Schema(description = "쿠폰 이벤트 이름", example = "여름맞이 할인 이벤트")
        String name,

        @Schema(description = "쿠폰 이벤트 유형")
        CouponEventType eventType,

        @Schema(description = "쿠폰 이벤트 상태")
        CouponEventStatus status,

        @Schema(description = "쿠폰 총 발급 수량", example = "100")
        int totalQuantity,

        @Schema(description = "남은 수량", example = "42")
        long remainingQuantity,

        @Schema(description = "이벤트 시작 시각", example = "2026-05-01T10:00:00")
        LocalDateTime startsAt,

        @Schema(description = "이벤트 종료 시각", example = "2026-05-01T18:00:00")
        LocalDateTime expiresAt
) {
    public static List<CouponEventPreviewItem> of(List<CouponEvent> events, Map<Long, Long> stockMap) {
        return events.stream()
                .map(event -> CouponEventPreviewItem.builder()
                        .id(event.getId())
                        .name(event.getName())
                        .eventType(event.getType())
                        .status(event.getEventStatus())
                        .totalQuantity(event.getTotalQuantity())
                        .remainingQuantity(stockMap.getOrDefault(event.getId(), 0L))
                        .startsAt(event.getStartsAt())
                        .expiresAt(event.getExpiresAt())
                        .build()
                )
                .toList();
    }

}