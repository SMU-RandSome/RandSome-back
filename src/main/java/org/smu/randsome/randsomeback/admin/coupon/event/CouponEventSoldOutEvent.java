package org.smu.randsome.randsomeback.admin.coupon.event;

/**
 * 쿠폰 이벤트 재고 소진 이벤트 <br>
 * 쿠폰 이벤트의 재고가 조기 소진될 때 발생하는 이벤트로, 소진된 쿠폰 이벤트의 ID를 포함합니다. <br>
 * 커밋 후 Redis stock 키 삭제에 사용됩니다.
 */
public record CouponEventSoldOutEvent(
        Long couponEventId
) {

}