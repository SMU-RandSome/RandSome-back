package org.smu.randsome.randsomeback.domain.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Schema(description = "쿠폰 항목")
public record CouponItem(
        @Schema(description = "쿠폰 ID", example = "1")
        Long id,

        @Schema(description = "이벤트 이름", example = "선착순 랜덤 매칭 티켓 증정")
        String eventName,

        @Schema(description = "쿠폰 상태", example = "AVAILABLE")
        CouponStatus status,

        @Schema(description = "보상 티켓 타입")
        TicketType ticketType,

        @Schema(description = "쿠폰 만료 시각")
        LocalDateTime expiredAt,

        @Schema(description = "보상 티켓 수량", example = "1")
        int rewardTicketAmount
) {

    public static CouponItem from(Coupon coupon) {
        return new CouponItem(
                coupon.getId(),
                coupon.getCouponEvent().getName(),
                coupon.getCouponStatus(),
                coupon.getCouponEvent().getRewardTicketType(),
                coupon.getExpiredAt(),
                coupon.getCouponEvent().getRewardTicketAmount()
        );
    }

}