package org.smu.randsome.randsomeback.admin.coupon.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponCacheManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.springframework.test.util.ReflectionTestUtils;

class CouponEventAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventAdminService couponEventAdminService;

    @Mock
    CouponEventManager couponEventManager;

    @Mock
    CouponCacheManager couponCacheManager;

    @Test
    void 쿠폰_이벤트를_활성화하면_Redis에_재고를_초기화한다() {
        // given
        Long eventId = 1L;
        CouponEvent event = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event, "id", eventId);
        given(couponEventManager.activate(eventId)).willReturn(event);

        // when
        couponEventAdminService.activateCouponEvent(eventId);

        // then
        verify(couponEventManager).activate(eventId);
        verify(couponCacheManager).initializeStock(
                eq(eventId),
                eq(event.getTotalQuantity()),
                any(Duration.class)
        );
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
