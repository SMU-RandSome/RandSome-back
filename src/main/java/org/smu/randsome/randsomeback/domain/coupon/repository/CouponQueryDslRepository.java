package org.smu.randsome.randsomeback.domain.coupon.repository;

import java.util.List;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.global.support.response.Cursor;

public interface CouponQueryDslRepository {

    List<Coupon> findByMemberAndFilter(Long memberId, CouponSearchCondition condition);

    List<Coupon> findByCouponEventWithMember(Long couponEventId, Cursor cursor);

}