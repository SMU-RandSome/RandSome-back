package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponJpaRepository;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class CouponManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponManager couponManager;

    @Mock
    CouponEventReader couponEventReader;

    @Mock
    MemberReader memberReader;

    @Mock
    CouponCacheManager couponCacheManager;

    @Mock
    CouponJpaRepository couponJpaRepository;

    // ── issueCoupon ──────────────────────────────────────────────

    @Test
    void 쿠폰_발급에_성공한다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;
        LocalDateTime now = LocalDateTime.now();
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        given(couponJpaRepository.save(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        couponManager.issueCoupon(eventId, memberId, now);

        // then
        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponJpaRepository).save(captor.capture());

        Coupon capturedCoupon = captor.getValue();
        assertThat(capturedCoupon.getCouponEvent()).isEqualTo(couponEvent);
        assertThat(capturedCoupon.getMember()).isEqualTo(member);
        assertThat(capturedCoupon.isAvailable()).isTrue();
    }

    @Test
    void 쿠폰_발급_시_Redis_동시성_제어가_수행된다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;
        LocalDateTime now = LocalDateTime.now();
        var couponEvent = CuponFixture.createActiveCuponEvent();
        var member = MemberFixture.create();

        given(couponEventReader.find(eventId)).willReturn(couponEvent);
        given(memberReader.getReference(memberId)).willReturn(member);
        given(couponJpaRepository.save(any(Coupon.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        couponManager.issueCoupon(eventId, memberId, now);

        // then
        verify(couponCacheManager).acquireMemberLockOrThrow(anyLong(), anyLong(), any());
        verify(couponCacheManager).decrementStockOrThrow(eventId, memberId);
    }

    @Test
    void 존재하지_않는_이벤트로_쿠폰_발급을_시도하면_예외가_발생한다() {
        // given
        Long eventId = 999L;
        Long memberId = 42L;
        LocalDateTime now = LocalDateTime.now();

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
        Long eventId = 1L;
        Long memberId = 42L;
        LocalDateTime now = LocalDateTime.now();
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
        Long eventId = 1L;
        Long memberId = 999L;
        LocalDateTime now = LocalDateTime.now();
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
        Long eventId = 1L;
        Long memberId = 42L;
        LocalDateTime now = LocalDateTime.now();
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
        Long eventId = 1L;
        Long memberId = 42L;
        LocalDateTime now = LocalDateTime.now();
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

}