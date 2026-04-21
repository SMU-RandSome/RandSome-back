package org.smu.randsome.randsomeback.admin.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventRegisterRequest;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventUpdateRequest;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class CouponEventAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_이벤트를_생성한다() throws JsonProcessingException {
        // given
        var request = new CouponEventRegisterRequest(
                "이벤트명",
                "이벤트 설명",
                CouponEventType.HAPPY_HOUR,
                100,
                TicketType.RANDOM,
                10,
                TestDateTimeUtils.now(),
                TestDateTimeUtils.now().plusDays(7),
                TestDateTimeUtils.now().plusDays(30)
        );

        given(couponEventAdminService.registerCouponEvent(request.toNewCouponEvent()))
                .willReturn(1L);

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/coupon-events")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .apply(print())
                .hasStatus(HttpStatus.CREATED.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isEqualTo(1));
    }

    @TestAdmin
    @Test
    void 관리자가_이벤트를_수정한다() throws JsonProcessingException {
        // given
        var request = new CouponEventUpdateRequest(
                "수정된 이벤트명",
                "수정된 설명",
                CouponEventType.HAPPY_HOUR,
                200,
                TicketType.RANDOM,
                5,
                TestDateTimeUtils.now(),
                TestDateTimeUtils.now().plusDays(14),
                TestDateTimeUtils.now().plusDays(30)
        );

        willDoNothing().given(couponEventAdminService).updateCouponEvent(eq(1L), any());

        // when & then
        assertThat(mvcTester.patch().uri("/v1/admin/coupon-events/1")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }


    @TestAdmin
    @Test
    void 관리자가_이벤트를_삭제한다() {
        // given
        willDoNothing().given(couponEventAdminService).deleteCouponEvent(1L);

        // when & then
        assertThat(mvcTester.delete().uri("/v1/admin/coupon-events/1"))
                .apply(print())
                .hasStatusOk();
    }

    @TestAdmin
    @Test
    void 관리자가_수동으로_이벤트를_활성화한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/coupon-events/{couponEventId}/activate", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

    @TestAdmin
    @Test
    void 관리자가_수동으로_이벤트를_종료한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/coupon-events/{couponEventId}/deactivate", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

}