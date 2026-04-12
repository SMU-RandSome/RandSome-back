package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class CouponEventManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponEventManager couponEventManager;

    @Mock
    CouponEventJpaRepository couponEventJpaRepository;

    @Test
    void 쿠폰_이벤트를_활성화하면_상태가_ACTIVE로_변경된다() {
        // given
        Long eventId = 1L;
        CouponEvent event = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event, "id", eventId);
        given(couponEventJpaRepository.findByIdAndStatus(eventId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(event));

        // when
        CouponEvent activatedEvent = couponEventManager.activate(eventId);

        // then
        verify(couponEventJpaRepository).findByIdAndStatus(eventId, EntityStatus.ACTIVE);
        assertThat(activatedEvent.getEventStatus()).isEqualTo(CouponEventStatus.ACTIVE);
    }

    @Test
    void 쿠폰_이벤트를_비활성화하면_상태가_ENDED로_변경된다() {
        // given
        Long eventId = 1L;
        CouponEvent event = CuponFixture.createCuponEvent();
        ReflectionTestUtils.setField(event, "id", eventId);
        event.activate(); // DRAFT -> ACTIVE
        given(couponEventJpaRepository.findByIdAndStatus(eventId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(event));

        // when
        couponEventManager.deactivate(eventId);

        // then
        verify(couponEventJpaRepository).findByIdAndStatus(eventId, EntityStatus.ACTIVE);
        assertThat(event.getEventStatus()).isEqualTo(CouponEventStatus.ENDED);
    }

}
