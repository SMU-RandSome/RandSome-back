package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponReader {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public CursorSlice<Coupon> findCoupons(Long memberId, CouponSearchCondition condition) {
        List<Coupon> coupons = couponRepository.findByMemberAndFilter(memberId, condition);

        boolean hasNext = coupons.size() > condition.size();
        List<Coupon> items = hasNext ? coupons.subList(0, condition.size()) : coupons;
        Long nextCursor = hasNext ? items.getLast().getId() : null;

        return CursorSlice.of(items, nextCursor, hasNext);
    }

}