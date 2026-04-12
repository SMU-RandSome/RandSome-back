package org.smu.randsome.randsomeback.admin.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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

        @Schema(description = "쿠폰 발급 수량", example = "100")
        int totalQuantity

) {
    public static List<CouponEventPreviewItem> from(List<CouponEvent> events) {
        return events.stream()
                .map(event -> CouponEventPreviewItem.builder()
                        .id(event.getId())
                        .name(event.getName())
                        .eventType(event.getType())
                        .status(event.getEventStatus())
                        .totalQuantity(event.getTotalQuantity())
                        .build()
                )
                .toList();
    }

}