package org.smu.randsome.randsomeback.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponReader;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponManager couponManager;
    private final CouponReader couponReader;
    private final TicketHandler ticketHandler;

    @Transactional
    public void useCoupon(Long couponId, Long memberId) {
        Coupon coupon = couponManager.useCoupon(couponId, memberId);

        CouponEvent couponEvent = coupon.getCouponEvent();
        ticketHandler.issueForCoupon(memberId, couponEvent.getRewardTicketType(), couponEvent.getRewardTicketAmount());
    }

    public CursorSlice<Coupon> findCoupons(Long memberId, CouponSearchCondition condition) {
        return couponReader.findCoupons(memberId, condition);
    }

    public boolean isIssuable(Long couponEventId, Long memberId) {
        return !couponReader.hasIssuedCoupon(couponEventId, memberId);
    }

}