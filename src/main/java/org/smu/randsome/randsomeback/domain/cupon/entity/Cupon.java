package org.smu.randsome.randsomeback.domain.cupon.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.cupon.enums.CouponStatus;
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
public class Cupon extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus couponStatus;

    private LocalDateTime usedAt;

    public static Cupon issue(CouponEvent couponEvent, Member member) {
        Cupon cupon = new Cupon();

        cupon.couponEvent = requireNonNull(couponEvent);
        cupon.member = requireNonNull(member);
        cupon.couponStatus = CouponStatus.AVAILABLE;

        return cupon;
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