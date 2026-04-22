package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class CouponReader {

    private final CouponRepository couponRepository;

    public Coupon findWithEvent(Long couponId) {
        return couponRepository.findByIdAndStatusWithEvent(couponId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON));
    }

    public CursorSlice<Coupon> findCoupons(Long memberId, CouponSearchCondition condition) {
        List<Coupon> coupons = couponRepository.findByMemberAndFilter(memberId, condition);

        boolean hasNext = coupons.size() > condition.size();
        List<Coupon> items = hasNext ? coupons.subList(0, condition.size()) : coupons;
        Long nextCursor = hasNext ? items.getLast().getId() : null;

        return CursorSlice.of(items, nextCursor, hasNext);
    }

    public List<Coupon> findExpirable(LocalDateTime now) {
        return couponRepository.findAllExpirable(CouponStatus.AVAILABLE, now, EntityStatus.ACTIVE);
    }

    public boolean hasIssuedCoupon(Long couponEventId, Long memberId) {
        return couponRepository.existsByCouponEventIdAndMemberIdAndStatus(couponEventId, memberId, EntityStatus.ACTIVE);
    }

}