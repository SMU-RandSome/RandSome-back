package org.smu.randsome.randsomeback.admin.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventRegisterRequest;
import org.smu.randsome.randsomeback.admin.coupon.dto.request.CouponEventUpdateRequest;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

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

    @TestAdmin
    @Test
    void 관리자가_이벤트_목록을_조회한다() {
        // given
        CouponEvent event1 = CouponEvent.create(
                "이벤트 1", "설명 1", CouponEventType.HAPPY_HOUR, 100,
                TicketType.RANDOM, 10,
                TestDateTimeUtils.now(), TestDateTimeUtils.now().plusDays(7),
                TestDateTimeUtils.now().plusDays(30)
        );
        ReflectionTestUtils.setField(event1, "id", 1L);

        CouponEvent event2 = CouponEvent.create(
                "이벤트 2", "설명 2", CouponEventType.HAPPY_HOUR, 200,
                TicketType.RANDOM, 20,
                TestDateTimeUtils.now(), TestDateTimeUtils.now().plusDays(14),
                TestDateTimeUtils.now().plusDays(30)
        );
        ReflectionTestUtils.setField(event2, "id", 2L);

        List<CouponEvent> events = List.of(event1, event2);
        Map<Long, Long> stockMap = Map.of(1L, 80L, 2L, 200L);

        given(couponEventAdminService.findCouponEvents()).willReturn(events);
        given(couponEventAdminService.findRemainingStocks(events)).willReturn(stockMap);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/coupon-events"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.length()", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data[0].name", v -> v.assertThat().isEqualTo("이벤트 1"))
                .hasPathSatisfying("$.data[0].totalQuantity", v -> v.assertThat().isEqualTo(100))
                .hasPathSatisfying("$.data[0].remainingQuantity", v -> v.assertThat().isEqualTo(80))
                .hasPathSatisfying("$.data[1].name", v -> v.assertThat().isEqualTo("이벤트 2"))
                .hasPathSatisfying("$.data[1].remainingQuantity", v -> v.assertThat().isEqualTo(200));
    }

    @TestAdmin
    @Test
    void 관리자가_이벤트_상세를_조회한다() {
        // given
        CouponEvent event = CouponEvent.create(
                "이벤트 1", "이벤트 1 설명", CouponEventType.HAPPY_HOUR, 100,
                TicketType.RANDOM, 10,
                TestDateTimeUtils.now(), TestDateTimeUtils.now().plusDays(7),
                TestDateTimeUtils.now().plusDays(30)
        );

        given(couponEventAdminService.findCouponEvent(1L)).willReturn(event);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/coupon-events/{couponEventId}", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.name", v -> v.assertThat().isEqualTo("이벤트 1"))
                .hasPathSatisfying("$.data.description", v -> v.assertThat().isEqualTo("이벤트 1 설명"))
                .hasPathSatisfying("$.data.eventType", v -> v.assertThat().isEqualTo("HAPPY_HOUR"))
                .hasPathSatisfying("$.data.totalQuantity", v -> v.assertThat().isEqualTo(100))
                .hasPathSatisfying("$.data.rewardTicketType", v -> v.assertThat().isEqualTo("RANDOM"))
                .hasPathSatisfying("$.data.rewardTicketAmount", v -> v.assertThat().isEqualTo(10));
    }

    @TestAdmin
    @Test
    void 관리자가_쿠폰_이벤트별_발급_회원_목록을_조회한다() {
        // given
        Member member1 = MemberFixture.createWithLegalName("202310001@sangmyung.kr", "홍길동");
        ReflectionTestUtils.setField(member1, "id", 1L);
        Member member2 = MemberFixture.createWithLegalName("202310002@sangmyung.kr", "김철수");
        ReflectionTestUtils.setField(member2, "id", 2L);

        CouponEvent event = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(event, "id", 1L);

        Coupon coupon1 = Coupon.issue(event, member1);
        ReflectionTestUtils.setField(coupon1, "id", 2L);
        Coupon coupon2 = Coupon.issue(event, member2);
        ReflectionTestUtils.setField(coupon2, "id", 1L);

        given(couponEventAdminService.findCouponEventIssuedMembers(eq(1L), any(Cursor.class)))
                .willReturn(CursorSlice.of(List.of(coupon1, coupon2), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/coupon-events/{couponEventId}/issued-members", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.items.length()", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data.items[0].memberId", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.data.items[0].legalName", v -> v.assertThat().isEqualTo("홍길동"))
                .hasPathSatisfying("$.data.items[1].memberId", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data.items[1].legalName", v -> v.assertThat().isEqualTo("김철수"))
                .hasPathSatisfying("$.data.hasNext", v -> v.assertThat().isEqualTo(false))
                .hasPathSatisfying("$.data.nextCursor", v -> v.assertThat().isNull());
    }

    @TestAdmin
    @Test
    void 다음_페이지가_있으면_nextCursor를_반환한다() {
        // given
        Member member = MemberFixture.create();
        ReflectionTestUtils.setField(member, "id", 1L);

        CouponEvent event = CuponFixture.createActiveCuponEvent();
        ReflectionTestUtils.setField(event, "id", 1L);

        Coupon coupon = Coupon.issue(event, member);
        ReflectionTestUtils.setField(coupon, "id", 10L);

        given(couponEventAdminService.findCouponEventIssuedMembers(eq(1L), any(Cursor.class)))
                .willReturn(CursorSlice.of(List.of(coupon), 10L, true));

        // when & then
        assertThat(mvcTester.get()
                        .uri("/v1/admin/coupon-events/{couponEventId}/issued-members?lastId=20&size=10", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.hasNext", v -> v.assertThat().isEqualTo(true))
                .hasPathSatisfying("$.data.nextCursor", v -> v.assertThat().isEqualTo(10));
    }

    @TestAdmin
    @Test
    void 발급된_쿠폰이_없으면_빈_목록을_반환한다() {
        // given
        given(couponEventAdminService.findCouponEventIssuedMembers(eq(1L), any(Cursor.class)))
                .willReturn(CursorSlice.of(List.of(), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/coupon-events/{couponEventId}/issued-members", 1L))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.items.length()", v -> v.assertThat().isEqualTo(0))
                .hasPathSatisfying("$.data.hasNext", v -> v.assertThat().isEqualTo(false));
    }

}