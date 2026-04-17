package org.smu.randsome.randsomeback.admin.coupon.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.admin.coupon.service.CouponEventAdminService;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.smu.randsome.randsomeback.domain.scheduler.CouponEventScheduler;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.springframework.test.util.ReflectionTestUtils;

class CouponEventSchedulerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventScheduler couponEventScheduler;

    @Mock
    CouponEventReader couponEventReader;

    @Mock
    CouponEventManager couponEventManager;

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
        verify(couponEventManager).activate(1L);
        verify(couponEventManager).activate(2L);
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
        verify(couponEventManager, never()).activate(any(Long.class));
    }

    @Test
    void 활성화_처리_중_일부_이벤트가_실패해도_나머지는_처리된다() {
        // given
        CouponEvent event1 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event1, "id", 1L);
        CouponEvent event2 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event2, "id", 2L);

        given(couponEventReader.findDraftEventsReadyToActivate(any(LocalDateTime.class)))
                .willReturn(List.of(event1, event2));
        willThrow(new RuntimeException("활성화 실패"))
                .given(couponEventManager).activate(1L);

        // when
        couponEventScheduler.activateDueEvents();

        // then
        verify(couponEventManager).activate(1L);
        verify(couponEventManager).activate(2L);
    }

    @Test
    void 종료_대기_중인_이벤트가_있으면_모두_종료한다() {
        // given
        CouponEvent event1 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event1, "id", 1L);
        CouponEvent event2 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event2, "id", 2L);

        List<CouponEvent> readyEvents = List.of(event1, event2);
        given(couponEventReader.findActiveEventsReadyToEnd(any(LocalDateTime.class)))
                .willReturn(readyEvents);

        // when
        couponEventScheduler.deactivateDueEvents();

        // then
        verify(couponEventReader).findActiveEventsReadyToEnd(any(LocalDateTime.class));
        verify(couponEventAdminService).deactivateCouponEvent(1L);
        verify(couponEventAdminService).deactivateCouponEvent(2L);
    }

    @Test
    void 종료_처리_중_일부_이벤트가_실패해도_나머지는_처리된다() {
        // given
        CouponEvent event1 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event1, "id", 1L);
        CouponEvent event2 = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event2, "id", 2L);

        given(couponEventReader.findActiveEventsReadyToEnd(any(LocalDateTime.class)))
                .willReturn(List.of(event1, event2));
        willThrow(new RuntimeException("종료 실패"))
                .given(couponEventAdminService).deactivateCouponEvent(1L);

        // when
        couponEventScheduler.deactivateDueEvents();

        // then
        verify(couponEventAdminService).deactivateCouponEvent(1L);
        verify(couponEventAdminService).deactivateCouponEvent(2L);
    }

    @Test
    void 종료_대기_중인_이벤트가_없으면_아무것도_하지_않는다() {
        // given
        given(couponEventReader.findActiveEventsReadyToEnd(any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        couponEventScheduler.deactivateDueEvents();

        // then
        verify(couponEventReader).findActiveEventsReadyToEnd(any(LocalDateTime.class));
        verify(couponEventAdminService, never()).deactivateCouponEvent(any(Long.class));
    }

}
