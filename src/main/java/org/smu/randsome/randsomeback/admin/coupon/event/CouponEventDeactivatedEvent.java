package org.smu.randsome.randsomeback.admin.coupon.event;

/**
 * 쿠폰 이벤트 비활성화 이벤트 <br>
 * 쿠폰 이벤트가 종료될 때 발생하는 이벤트로, 비활성화될 쿠폰 이벤트의 ID를 포함합니다. <br>
 * */
public record CouponEventDeactivatedEvent(
        Long couponEventId
) {

}