package org.smu.randsome.randsomeback.admin.coupon.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.domain.scheduler.CouponEventScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.admin.coupon.service.CouponEventAdminService;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.smu.randsome.randsomeback.fixture.CuponFixture;

class CouponEventSchedulerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventScheduler couponEventScheduler;

    @Mock
    CouponEventReader couponEventReader;

    @Mock
    CouponEventAdminService couponEventAdminService;

    @Test
    void 활성화_대기_중인_이벤트가_있으면_모두_활성화한다() {
        // given
        CouponEvent event1 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event1, "id", 1L);
        CouponEvent event2 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event2, "id", 2L);

        List<CouponEvent> readyEvents = List.of(event1, event2);
        given(couponEventReader.findDraftEventsReadyToActivate(any(LocalDateTime.class)))
                .willReturn(readyEvents);

        // when
        couponEventScheduler.activateDueEvents();

        // then
        verify(couponEventReader).findDraftEventsReadyToActivate(any(LocalDateTime.class));
        verify(couponEventAdminService).activateCouponEvent(1L);
        verify(couponEventAdminService).activateCouponEvent(2L);
    }

    @Test
    void 활성화_대기_중인_이벤트가_없으면_아무것도_하지_않는다() {
        // given
        given(couponEventReader.findDraftEventsReadyToActivate(any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        couponEventScheduler.activateDueEvents();

        // then
        verify(couponEventReader).findDraftEventsReadyToActivate(any(LocalDateTime.class));
        verify(couponEventAdminService, never()).activateCouponEvent(any(Long.class));
    }

}
