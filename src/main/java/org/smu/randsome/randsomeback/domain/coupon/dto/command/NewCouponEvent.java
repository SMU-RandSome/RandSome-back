package org.smu.randsome.randsomeback.domain.coupon.dto.command;

import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Builder
public record NewCouponEvent(
        String name,
        String description,
        CouponEventType type,
        int totalQuantity,
        TicketType rewardTicketType,
        int rewardTicketAmount,
        LocalDateTime startsAt,
        LocalDateTime expiresAt
) {
}
