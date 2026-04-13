package org.smu.randsome.randsomeback.domain.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponManager;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class CouponEventServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventService couponEventService;

    @Mock
    CouponManager couponManager;

    // ── publishCouponFromEvent ──────────────────────────────────────

    @Test
    void 이벤트에서_쿠폰_발급에_성공한다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;
        Long expectedCouponId = 100L;

        given(couponManager.issueCoupon(anyLong(), anyLong(), any())).willReturn(expectedCouponId);

        // when
        Long couponId = couponEventService.publishCouponFromEvent(eventId, memberId);

        // then
        assertThat(couponId).isEqualTo(expectedCouponId);
        verify(couponManager).issueCoupon(anyLong(), anyLong(), any());
    }

    @Test
    void Manager에서_예외_발생_시_그대로_전파된다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;

        doThrow(new CoreException(ErrorType.COUPON_EVENT_NOT_ACTIVE))
                .when(couponManager).issueCoupon(anyLong(), anyLong(), any());

        // when & then
        assertThatThrownBy(() -> couponEventService.publishCouponFromEvent(eventId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_NOT_ACTIVE.getMessage());
    }

}
