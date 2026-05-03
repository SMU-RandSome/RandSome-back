package org.smu.randsome.randsomeback.global.config;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheKeys {

    public static final String ANNOUNCEMENTS = "announcements";
    private static final String COUPON_EVENT = "coupon:event";
    private static final String ATTENDANCE = "attendance";
    private static final String SUSPENSION = "suspend:member";
    private static final String MATCHING_IDEMPOTENCY = "matching:idempotency";
    private static final String MEMBER_PROFILE_TAG = "member:profile-tag";
    private static final String MATCHING_STATS = "matching:stats";
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";
    private static final String VERIFICATION_CODE_FAIL_COUNT_PREFIX = "verification_fail:";
    private static final String VERIFICATION_CODE_SEND_COOLDOWN_PREFIX = "verification_cooldown:";

    public static final String MATCHING_TOTAL_COUNT = MATCHING_STATS + ":total-count";

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

    public static String suspension(Long memberId) {
        return SUSPENSION + ":" + memberId;
    }

    public static String memberProfileTag(Long memberId) {
        return MEMBER_PROFILE_TAG + ":" + memberId;
    }

    public static String matchingIdempotency(Long memberId, MatchingType matchingType, int applicationCount) {
        return MATCHING_IDEMPOTENCY + ":" + memberId + ":" + matchingType + ":" + applicationCount;
    }

    public static String verificationCode(String email) {
        return VERIFICATION_CODE_KEY_PREFIX + email;
    }

    public static String verificationCodeFail(String email) {
        return VERIFICATION_CODE_FAIL_COUNT_PREFIX + email;
    }

    public static String verificationCodeSendCooldown(String email) {
        return VERIFICATION_CODE_SEND_COOLDOWN_PREFIX + email;
    }

}