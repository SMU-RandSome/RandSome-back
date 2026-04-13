package org.smu.randsome.randsomeback.domain.coupon.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "UK_CUPON_EVENT_MEMBER",
                columnNames = {"coupon_event_id", "member_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Coupon extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus couponStatus;

    @Version
    private Long version;

    private LocalDateTime usedAt;

    private LocalDateTime expiredAt;

    public static Coupon issue(CouponEvent couponEvent, Member member) {
        Coupon coupon = new Coupon();

        coupon.couponEvent = requireNonNull(couponEvent);
        coupon.member = requireNonNull(member);
        coupon.couponStatus = CouponStatus.AVAILABLE;
        coupon.expiredAt = couponEvent.getCouponExpiresAt();

        return coupon;
    }

    public void use() {
        if (!isAvailable()) {
            throw new CoreException(ErrorType.COUPON_NOT_USABLE);
        }
        this.couponStatus = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void expire() {
        if (couponStatus != CouponStatus.AVAILABLE) {
            return;
        }
        this.couponStatus = CouponStatus.EXPIRED;
    }

    public boolean isAvailable() {
        return couponStatus == CouponStatus.AVAILABLE;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

}