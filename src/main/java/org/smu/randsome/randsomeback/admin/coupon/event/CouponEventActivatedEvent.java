package org.smu.randsome.randsomeback.admin.coupon.event;

import java.time.LocalDateTime;

/**
 * 쿠폰 이벤트 활성화 이벤트 <br>
 * 쿠폰 이벤트가 활성화될 때 발생하는 이벤트로, 활성화된 쿠폰 이벤트의 ID, 총 발급 수량, 활성화 시각, 만료 시간을 포함합니다. <br>
 * activatedAt은 Redis TTL 계산의 기준 시점으로 사용되며, activate() 검증 시점과 동일합니다. <br>
 * */
public record CouponEventActivatedEvent(
        Long couponEventId,
        int totalQuantity,
        LocalDateTime activatedAt,
        LocalDateTime expiresAt
) {

}