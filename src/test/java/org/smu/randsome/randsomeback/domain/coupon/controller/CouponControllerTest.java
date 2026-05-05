package org.smu.randsome.randsomeback.domain.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.CouponSearchCondition;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class CouponControllerTest extends ControllerTestSupport {

    @TestMember
    @Test
    void 쿠폰을_사용하면_200을_반환한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/coupons/1/use"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value());
    }

    @TestMember
    @Test
    void 회원의_쿠폰_목록을_조회한다() {
        // given
        given(couponService.findCoupons(eq(1L), any(CouponSearchCondition.class)))
                .willReturn(CursorSlice.of(List.of(), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/coupons")
                .param("filter", "ALL")
                .param("size", "20"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value());
    }

}