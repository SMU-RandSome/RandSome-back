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

    @Column(nullable = false)
    private LocalDateTime couponExpiresAt;

    public static CouponEvent create(
            String name,
            String description,
            CouponEventType type,
            int totalQuantity,
            TicketType rewardTicketType,
            int rewardTicketAmount,
            LocalDateTime startsAt,
            LocalDateTime expiresAt,
            LocalDateTime couponExpiresAt
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
        event.couponExpiresAt = requireNonNull(couponExpiresAt);

        return event;
    }

    public void activate(LocalDateTime now) {
        if (eventStatus != CouponEventStatus.DRAFT) {
            throw new CoreException(ErrorType.COUPON_EVENT_INVALID_STATUS);
        }
        if (!now.isBefore(expiresAt)) {
            throw new CoreException(ErrorType.COUPON_EVENT_ALREADY_EXPIRED);
        }
        this.eventStatus = CouponEventStatus.ACTIVE;
    }

    public void soldOut() {
        if (eventStatus != CouponEventStatus.ACTIVE) {
            throw new CoreException(ErrorType.COUPON_EVENT_INVALID_STATUS);
        }
        this.eventStatus = CouponEventStatus.SOLD_OUT;
    }

    public void end() {
        if (eventStatus != CouponEventStatus.ACTIVE) {
            throw new CoreException(ErrorType.COUPON_EVENT_INVALID_STATUS);
        }
        this.eventStatus = CouponEventStatus.ENDED;
    }

    public void update(
            String name,
            String description,
            CouponEventType type,
            int totalQuantity,
            TicketType rewardTicketType,
            int rewardTicketAmount,
            LocalDateTime startsAt,
            LocalDateTime expiresAt,
            LocalDateTime couponExpiresAt
    ) {
        if (eventStatus != CouponEventStatus.DRAFT) {
            throw new CoreException(ErrorType.COUPON_EVENT_INVALID_STATUS);
        }

        if (startsAt.isAfter(expiresAt) || expiresAt.isAfter(couponExpiresAt)) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.name = requireNonNull(name);
        this.description = description;
        this.type = requireNonNull(type);
        this.totalQuantity = totalQuantity;
        this.rewardTicketType = requireNonNull(rewardTicketType);
        this.rewardTicketAmount = rewardTicketAmount;
        this.startsAt = requireNonNull(startsAt);
        this.expiresAt = requireNonNull(expiresAt);
        this.couponExpiresAt = requireNonNull(couponExpiresAt);
    }

    public boolean isIssuable(LocalDateTime now) {
        return eventStatus == CouponEventStatus.ACTIVE
                && !now.isBefore(startsAt)
                && now.isBefore(expiresAt);
    }

}