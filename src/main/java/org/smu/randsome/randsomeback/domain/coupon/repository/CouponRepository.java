package org.smu.randsome.randsomeback.domain.coupon.repository;

import org.smu.randsome.randsomeback.global.entity.EntityStatus;

public interface CouponRepository extends CouponJpaRepository, CouponQueryDslRepository {

    boolean existsByCouponEventIdAndMemberIdAndStatus(Long couponEventId, Long memberId, EntityStatus status);

}