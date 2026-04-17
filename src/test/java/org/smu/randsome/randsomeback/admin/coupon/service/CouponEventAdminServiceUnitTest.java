package org.smu.randsome.randsomeback.admin.coupon.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponCacheManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;

class CouponEventAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventAdminService couponEventAdminService;

    @Mock
    CouponEventManager couponEventManager;

    @Mock
    CouponEventReader couponEventReader;

    @Mock
    CouponCacheManager couponCacheManager;

    @Test
    void 쿠폰_이벤트를_활성화하면_Manager의_activate를_호출한다() {
        // given
        Long eventId = 1L;

        // when
        couponEventAdminService.activateCouponEvent(eventId);

        // then: Redis 초기화는 AFTER_COMMIT 이벤트 리스너에 위임되므로 Manager 호출만 검증
        verify(couponEventManager).activate(eventId);
    }

    @Test
    void 쿠폰_이벤트를_비활성화하면_Redis에_재고를_삭제한다() {
        // given
        Long eventId = 1L;

        // when
        couponEventAdminService.deactivateCouponEvent(eventId);

        // then
        verify(couponEventManager).deactivate(eventId);
        verify(couponCacheManager).deleteStock(eventId);
    }

}