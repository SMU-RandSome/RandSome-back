package org.smu.randsome.randsomeback.global.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheKeys {

    public static final String ANNOUNCEMENTS = "announcements";

    // Coupon Redis keys
    public static String couponStock(Long eventId) {
        return "coupon:event:" + eventId + ":stock";
    }

    public static String couponMemberLock(Long eventId, Long memberId) {
        return "coupon:event:" + eventId + ":member:" + memberId;
    }

}