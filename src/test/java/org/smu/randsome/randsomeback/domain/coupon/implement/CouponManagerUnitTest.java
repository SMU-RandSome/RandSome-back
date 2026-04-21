package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventSoldOutEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

class CouponManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponManager couponManager;

    @Mock
    CouponReader couponReader;

    @Mock
    CouponEventReader couponEventReader;

    @Mock
    MemberReader memberReader;

    @Mock
    CouponCacheManager couponCacheManager;

    @Mock
    CouponRepository couponRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    // ── useCoupon ────────────────────────────────────────────────

    @Test
    void 쿠폰_사용에_성공하면_USED_상태의_쿠폰을_반환한다() {
        // given
        var couponId = 1L;
        var memberId = 42L;
        var owner = MemberFixture.create();
        ReflectionTestUtils.setField(owner, "id", memberId);
        var event = CuponFixture.createActiveCuponEvent();
        var coupon = Coupon.issue(event, owner);

        given(couponReader.findWithEvent(couponId)).willReturn(coupon);

        // when
        Coupon result = couponManager.useCoupon(couponId, memberId);

        // then
        assertThat(result.getCouponStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    void 존재하지_않는_쿠폰을_사용하면_NOT_FOUND_COUPON_예외가_발생한다() {
        // given
        var couponId = 999L;
        var memberId = 42L;

        given(couponReader.findWithEvent(couponId))
                .willThrow(new CoreException(ErrorType.NOT_FOUND_COUPON));

        // when & then
        assertThatThrownBy(() -> couponManager.useCoupon(couponId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_COUPON.getMessage());
    }

    @Test
    void 타인_쿠폰을_사용하면_NOT_FOUND_COUPON_예외가_발생한다() {
        // given
        var couponId = 1L;
        var ownerId = 99L;
        var requesterId = 42L;
        var owner = MemberFixture.create();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        var event = CuponFixture.createActiveCuponEvent();
        var coupon = Coupon.issue(event, owner);

        given(couponReader.findWithEvent(couponId)).willReturn(coupon);

        // when & then
        assertThatThrownBy(() -> couponManager.useCoupon(couponId, requesterId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_COUPON.getMessage());
    }

    @Test
    void 이미_사용된_쿠폰을_재사용하면_COUPON_NOT_USABLE_예외가_발생한다() {
        // given
        var couponId = 1L;
        var memberId = 42L;
        var owner = MemberFixture.create();
        ReflectionTestUtils.setField(owner, "id", memberId);
        var event = CuponFixture.createActiveCuponEvent();
        var coupon = Coupon.issue(event, owner);
        coupon.use(); // 이미 사용

        given(couponReader.findWithEvent(couponId)).willReturn(coupon);

        // when & then
        assertThatThrownBy(() -> couponManager.useCoupon(couponId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }

    @Test
    void 만료된_쿠폰을_사용하면_COUPON_NOT_USABLE_예외가_발생한다() {
        // given
        var couponId = 1L;
        var memberId = 42L;
        var owner = MemberFixture.create();
        ReflectionTestUtils.setField(owner, "id", memberId);
        var event = CuponFixture.createActiveCuponEvent();
        var coupon = Coupon.issue(event, owner);
        coupon.expire(); // 만료

        given(couponReader.findWithEvent(couponId)).willReturn(coupon);

        // when & then
        assertThatThrownBy(() -> couponManager.useCoupon(couponId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_NOT_USABLE.getMessage());
    }

    // ── issueCoupon ──────────────────────────────────────────────

    @Test
    void 쿠폰_발급에_성공한다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = CuponFixture.STARTED_AT.plusSeconds(1);
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        given(couponCacheManager.decrementStockOrThrow(eventId, memberId)).willReturn(1L); // 재고 남음
        given(couponRepository.saveAndFlush(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        couponManager.issueCoupon(eventId, memberId, now);

        // then
        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).saveAndFlush(captor.capture());

        Coupon capturedCoupon = captor.getValue();
        assertThat(capturedCoupon.getCouponEvent()).isEqualTo(couponEvent);
        assertThat(capturedCoupon.getMember()).isEqualTo(member);
        assertThat(capturedCoupon.isAvailable()).isTrue();
    }

    @Test
    void 쿠폰_발급_시_Redis_동시성_제어가_수행된다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = LocalDateTime.now();
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        given(couponCacheManager.decrementStockOrThrow(eventId, memberId)).willReturn(1L);
        given(couponRepository.saveAndFlush(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        couponManager.issueCoupon(eventId, memberId, now);

        // then
        verify(couponCacheManager).acquireMemberLockOrThrow(anyLong(), anyLong(), any());
        verify(couponCacheManager).decrementStockOrThrow(eventId, memberId);
    }

    @Test
    void 마지막_재고_발급_시_이벤트가_SOLD_OUT_상태로_전환되고_도메인_이벤트가_발행된다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = CuponFixture.STARTED_AT.plusSeconds(1);
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        given(couponCacheManager.decrementStockOrThrow(eventId, memberId)).willReturn(0L); // 마지막 재고
        given(couponRepository.saveAndFlush(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        couponManager.issueCoupon(eventId, memberId, now);

        // then
        assertThat(couponEvent.getEventStatus()).isEqualTo(CouponEventStatus.SOLD_OUT);
        ArgumentCaptor<CouponEventSoldOutEvent> eventCaptor = ArgumentCaptor.forClass(CouponEventSoldOutEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().couponEventId()).isEqualTo(eventId);
    }

    @Test
    void 재고가_남아있으면_이벤트_상태가_ACTIVE로_유지된다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = CuponFixture.STARTED_AT.plusSeconds(1);
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        given(couponCacheManager.decrementStockOrThrow(eventId, memberId)).willReturn(5L); // 재고 남음
        given(couponRepository.saveAndFlush(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        couponManager.issueCoupon(eventId, memberId, now);

        // then
        assertThat(couponEvent.getEventStatus()).isEqualTo(CouponEventStatus.ACTIVE);
        verify(eventPublisher, never()).publishEvent(any(CouponEventSoldOutEvent.class));
    }

    @Test
    void 존재하지_않는_이벤트로_쿠폰_발급을_시도하면_예외가_발생한다() {
        // given
        var eventId = 999L;
        var memberId = 42L;
        var now = LocalDateTime.now();

        given(couponEventReader.find(eventId))
                .willThrow(new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        // when & then
        assertThatThrownBy(() -> couponManager.issueCoupon(eventId, memberId, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_COUPON_EVENT.getMessage());
    }

    @Test
    void 비활성_이벤트로_쿠폰_발급을_시도하면_예외가_발생한다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = LocalDateTime.now();
        var draftEvent = CuponFixture.createCuponEvent(); // DRAFT 상태 → isIssuable() = false

        given(couponEventReader.find(eventId)).willReturn(draftEvent);

        // when & then
        assertThatThrownBy(() -> couponManager.issueCoupon(eventId, memberId, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_NOT_ACTIVE.getMessage());
    }

    @Test
    void 존재하지_않는_회원으로_쿠폰_발급을_시도하면_예외가_발생한다() {
        // given
        var eventId = 1L;
        var memberId = 999L;
        var now = LocalDateTime.now();
        var couponEvent = CuponFixture.createActiveCuponEvent();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId))
                .willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> couponManager.issueCoupon(eventId, memberId, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 중복_발급_시도_시_ALREADY_ISSUED_COUPON_예외가_발생한다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = LocalDateTime.now();
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        doThrow(new CoreException(ErrorType.ALREADY_ISSUED_COUPON))
                .when(couponCacheManager)
                .acquireMemberLockOrThrow(anyLong(), anyLong(), any());

        // when & then
        assertThatThrownBy(() -> couponManager.issueCoupon(eventId, memberId, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ALREADY_ISSUED_COUPON.getMessage());
    }

    @Test
    void 재고_소진_시_COUPON_SOLD_OUT_예외가_발생한다() {
        // given
        var eventId = 1L;
        var memberId = 42L;
        var now = LocalDateTime.now();
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        doThrow(new CoreException(ErrorType.COUPON_SOLD_OUT))
                .when(couponCacheManager)
                .decrementStockOrThrow(eventId, memberId);

        // when & then
        assertThatThrownBy(() -> couponManager.issueCoupon(eventId, memberId, now))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_SOLD_OUT.getMessage());
    }

    @Test
    void 쿠폰을_배치로_만료시킨다() {
        // given
        var coupon1 = Coupon.issue(CuponFixture.createCuponEvent(), MemberFixture.create());
        var coupon2 = Coupon.issue(CuponFixture.createCuponEvent(), MemberFixture.create());
        var coupons = java.util.List.of(coupon1, coupon2);

        // when
        couponManager.expireBatch(coupons);

        // then
        assertThat(coupon1.getCouponStatus()).isEqualTo(org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus.EXPIRED);
        assertThat(coupon2.getCouponStatus()).isEqualTo(org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus.EXPIRED);
        verify(couponRepository).saveAll(coupons);
    }

}