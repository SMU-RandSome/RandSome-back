package org.smu.randsome.randsomeback.domain.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
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

    @TestMember
    @Test
    void 이벤트_상세를_조회한다() {
        // given
        CouponEvent event = CouponEvent.create(
                "이벤트 1",
                "이벤트 1 설명",
                CouponEventType.HAPPY_HOUR,
                100,
                TicketType.RANDOM,
                10,
                TestDateTimeUtils.now(),
                TestDateTimeUtils.now().plusDays(7),
                TestDateTimeUtils.now().plusDays(30)
        );

        given(couponEventService.findCouponEvent(1L)).willReturn(event);
        given(couponService.isIssuable(1L, 1L)).willReturn(true);

        // when & then
        assertThat(mvcTester.get().uri("/v1/coupon-events/{couponEventId}", 1L))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.name", v -> v.assertThat().isEqualTo("이벤트 1"))
                .hasPathSatisfying("$.data.eventType", v -> v.assertThat().isEqualTo("HAPPY_HOUR"))
                .hasPathSatisfying("$.data.totalQuantity", v -> v.assertThat().isEqualTo(100))
                .hasPathSatisfying("$.data.rewardTicketType", v -> v.assertThat().isEqualTo("RANDOM"))
                .hasPathSatisfying("$.data.rewardTicketAmount", v -> v.assertThat().isEqualTo(10))
                .hasPathSatisfying("$.data.isIssuable", v -> v.assertThat().isEqualTo(true));
    }

    @TestMember
    @Test
    void 이미_쿠폰을_발급받은_회원은_발급_불가능_상태로_응답된다() {
        // given
        CouponEvent event = CouponEvent.create(
                "이벤트 1",
                "이벤트 1 설명",
                CouponEventType.HAPPY_HOUR,
                100,
                TicketType.RANDOM,
                10,
                TestDateTimeUtils.now(),
                TestDateTimeUtils.now().plusDays(7),
                TestDateTimeUtils.now().plusDays(30)
        );

        given(couponEventService.findCouponEvent(1L)).willReturn(event);
        given(couponService.isIssuable(1L, 1L)).willReturn(false);

        // when & then
        assertThat(mvcTester.get().uri("/v1/coupon-events/{couponEventId}", 1L))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.data.isIssuable", v -> v.assertThat().isEqualTo(false));
    }

    @TestMember
    @Test
    void 이벤트_목록을_조회한다() {
        // given
        List<CouponEvent> events = createCouponEvents();

        given(couponEventService.findCouponEvents())
                .willReturn(events);

        // when & then
        assertThat(mvcTester.get().uri("/v1/coupon-events"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                // 공통 응답 구조 검증
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.length()", v -> v.assertThat().isEqualTo(2))

                // 첫 번째 이벤트 (Controller 변환 결과)
                .hasPathSatisfying("$.data[0].name", v -> v.assertThat().isEqualTo("이벤트 1"))
                .hasPathSatisfying("$.data[0].eventType", v -> v.assertThat().isEqualTo("HAPPY_HOUR"))
                .hasPathSatisfying("$.data[0].totalQuantity", v -> v.assertThat().isEqualTo(100))

                // 두 번째 이벤트
                .hasPathSatisfying("$.data[1].name", v -> v.assertThat().isEqualTo("이벤트 2"))
                .hasPathSatisfying("$.data[1].eventType", v -> v.assertThat().isEqualTo("HAPPY_HOUR"))
                .hasPathSatisfying("$.data[1].totalQuantity", v -> v.assertThat().isEqualTo(200));
    }

    private List<CouponEvent> createCouponEvents() {
        return List.of(
                CouponEvent.create(
                        "이벤트 1",
                        "이벤트 1 설명",
                        CouponEventType.HAPPY_HOUR,
                        100,
                        TicketType.RANDOM,
                        10,
                        TestDateTimeUtils.now(),
                        TestDateTimeUtils.now().plusDays(7),
                        TestDateTimeUtils.now().plusDays(30)
                ),
                CouponEvent.create(
                        "이벤트 2",
                        "이벤트 2 설명",
                        CouponEventType.HAPPY_HOUR,
                        200,
                        TicketType.RANDOM,
                        20,
                        TestDateTimeUtils.now(),
                        TestDateTimeUtils.now().plusDays(14),
                        TestDateTimeUtils.now().plusDays(30)
                )
        );
    }

}