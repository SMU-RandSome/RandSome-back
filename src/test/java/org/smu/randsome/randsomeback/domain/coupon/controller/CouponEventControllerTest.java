package org.smu.randsome.randsomeback.domain.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class CouponEventControllerTest extends ControllerTestSupport {

    @TestMember
    @Test
    void 회원이_쿠폰_발급_요청을_한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/coupon-events/{couponEventId}/issue", 1L))
                .apply(print())
                .hasStatus(HttpStatus.CREATED.value());
    }

}