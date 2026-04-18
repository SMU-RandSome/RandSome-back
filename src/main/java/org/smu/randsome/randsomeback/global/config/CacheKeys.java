package org.smu.randsome.randsomeback.global.config;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheKeys {

    public static final String ANNOUNCEMENTS = "announcements";
    private static final String COUPON_EVENT = "coupon:event";
    private static final String ATTENDANCE = "attendance";

    // Coupon Redis keys
    public static String couponStock(Long eventId) {
        return COUPON_EVENT + ":" + eventId + ":stock";
    }

    public static String couponMemberLock(Long eventId, Long memberId) {
        return COUPON_EVENT + ":" + eventId + ":member:" + memberId;
    }

    public static String attendance(Long memberId, LocalDate date) {
        return ATTENDANCE + ":" + memberId + ":" + date;
    }

}