package org.smu.randsome.randsomeback.admin.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponReader;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.springframework.test.util.ReflectionTestUtils;

class CouponEventAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventAdminService couponEventAdminService;

    @Mock
    CouponEventManager couponEventManager;

    @Mock
    CouponEventReader couponEventReader;

    @Mock
    CouponReader couponReader;

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
    void 발급_회원_목록_조회를_CouponReader에_위임한다() {
        // given
        Long eventId = 1L;
        Cursor cursor = Cursor.of(null, 20);
        CursorSlice<Coupon> expected = CursorSlice.of(List.of(), null, false);
        given(couponReader.findIssuedCoupons(eq(eventId), any(Cursor.class))).willReturn(expected);

        // when
        CursorSlice<Coupon> result = couponEventAdminService.findCouponEventIssuedMembers(eventId, cursor);

        // then
        assertThat(result).isEqualTo(expected);
        verify(couponReader).findIssuedCoupons(eq(eventId), any(Cursor.class));
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
