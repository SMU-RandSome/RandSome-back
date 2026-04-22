package org.smu.randsome.randsomeback.admin.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.springframework.test.util.ReflectionTestUtils;

class CouponEventAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventAdminService couponEventAdminService;

    @Mock
    CouponEventManager couponEventManager;

    @Mock
    CouponEventReader couponEventReader;

    @Test
    void 쿠폰_이벤트를_상세_조회한다() {
        // given
        Long eventId = 1L;
        CouponEvent event = CuponFixture.createCuponEvent();
        given(couponEventReader.find(eventId)).willReturn(event);

        // when
        CouponEvent result = couponEventAdminService.findCouponEvent(eventId);

        // then
        assertThat(result).isEqualTo(event);
    }

    @Test
    void 남은_수량_조회를_Reader에_위임한다() {
        // given
        CouponEvent draftEvent = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(draftEvent, "id", 1L);

        CouponEvent activeEvent = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(activeEvent, "id", 2L);

        List<CouponEvent> events = List.of(draftEvent, activeEvent);
        Map<Long, Long> expectedStockMap = Map.of(1L, 10L, 2L, 5L);

        given(couponEventReader.findRemainingStocks(events)).willReturn(expectedStockMap);

        // when
        Map<Long, Long> stockMap = couponEventAdminService.findRemainingStocks(events);

        // then
        assertThat(stockMap).isEqualTo(expectedStockMap);
        verify(couponEventReader).findRemainingStocks(events);
    }

}
