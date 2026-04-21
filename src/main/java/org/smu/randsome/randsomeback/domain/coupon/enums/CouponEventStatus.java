package org.smu.randsome.randsomeback.domain.coupon.enums;

public enum CouponEventStatus {

    DRAFT,     // 초안 상태, 아직 활성화되지 않은 이벤트
    ACTIVE,    // 활성화된 이벤트
    SOLD_OUT,  // 재고 조기 소진
    ENDED,     // 시간 만료로 종료된 이벤트
    ;
}