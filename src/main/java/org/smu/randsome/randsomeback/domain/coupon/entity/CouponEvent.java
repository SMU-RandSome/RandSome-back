package org.smu.randsome.randsomeback.domain.coupon.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CouponEvent extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponEventType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponEventStatus eventStatus;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketType rewardTicketType;

    @Column(nullable = false)
    private int rewardTicketAmount; // 쿠폰 당 발급되는 티켓 수량

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static CouponEvent create(
            String name,
            String description,
            CouponEventType type,
            int totalQuantity,
            TicketType rewardTicketType,
            int rewardTicketAmount,
            LocalDateTime startsAt,
            LocalDateTime expiresAt
    ) {
        CouponEvent event = new CouponEvent();

        event.name = requireNonNull(name);
        event.description = description;
        event.type = requireNonNull(type);
        event.eventStatus = CouponEventStatus.DRAFT;
        event.totalQuantity = totalQuantity;
        event.rewardTicketType = requireNonNull(rewardTicketType);
        event.rewardTicketAmount = rewardTicketAmount;
        event.startsAt = requireNonNull(startsAt);
        event.expiresAt = requireNonNull(expiresAt);

        return event;
    }

    public void activate() {
        if (eventStatus != CouponEventStatus.DRAFT) {
            throw new CoreException(ErrorType.COUPON_EVENT_INVALID_STATUS);
        }
        this.eventStatus = CouponEventStatus.ACTIVE;
    }

    public void end() {
        if (eventStatus != CouponEventStatus.ACTIVE) {
            throw new CoreException(ErrorType.COUPON_EVENT_INVALID_STATUS);
        }
        this.eventStatus = CouponEventStatus.ENDED;
    }

    public boolean isIssuable(LocalDateTime now) {
        return eventStatus == CouponEventStatus.ACTIVE
                && !now.isBefore(startsAt)
                && now.isBefore(expiresAt);
    }

}
