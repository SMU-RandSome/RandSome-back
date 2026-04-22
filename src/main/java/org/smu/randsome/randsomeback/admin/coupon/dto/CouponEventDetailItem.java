package org.smu.randsome.randsomeback.admin.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Schema(description = "쿠폰 이벤트 상세 항목")
@Builder
public record CouponEventDetailItem(
        @Schema(description = "쿠폰 이벤트 ID", example = "1")
        Long id,

        @Schema(description = "쿠폰 이벤트 이름", example = "여름맞이 할인 이벤트")
        String name,

        @Schema(description = "쿠폰 이벤트 설명", example = "선착순 100명에게 랜덤 티켓을 드립니다.")
        String description,

        @Schema(description = "쿠폰 이벤트 유형")
        CouponEventType eventType,

        @Schema(description = "쿠폰 이벤트 상태")
        CouponEventStatus status,

        @Schema(description = "쿠폰 발급 수량", example = "100")
        int totalQuantity,

        @Schema(description = "지급할 티켓 타입")
        TicketType rewardTicketType,

        @Schema(description = "쿠폰 1개당 지급되는 티켓 수량", example = "1")
        int rewardTicketAmount,

        @Schema(description = "회원이 해당 이벤트에 대해 쿠폰을 발급받을 수 있는지 여부", example = "true")
        boolean isIssuable,

        @Schema(description = "이벤트 시작 시각", example = "2026-05-01T10:00:00")
        LocalDateTime startsAt,

        @Schema(description = "이벤트 종료 시각", example = "2026-05-01T18:00:00")
        LocalDateTime expiresAt
) {
    public static CouponEventDetailItem of(CouponEvent event, boolean isIssuable) {
        return CouponEventDetailItem.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventType(event.getType())
                .status(event.getEventStatus())
                .totalQuantity(event.getTotalQuantity())
                .rewardTicketType(event.getRewardTicketType())
                .rewardTicketAmount(event.getRewardTicketAmount())
                .isIssuable(isIssuable)
                .startsAt(event.getStartsAt())
                .expiresAt(event.getExpiresAt())
                .build();
    }

}