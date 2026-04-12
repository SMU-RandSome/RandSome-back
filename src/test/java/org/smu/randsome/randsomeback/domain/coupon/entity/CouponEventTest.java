package org.smu.randsome.randsomeback.domain.coupon.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.fixture.CuponFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class CouponEventTest {

    @Test
    void 이벤트를_생성한다() {
        // when
        var event = CuponFixture.createCuponEvent();

        // then
        assertThat(event).extracting(
                CouponEvent::getName,
                CouponEvent::getDescription,
                CouponEvent::getType,
                CouponEvent::getTotalQuantity,
                CouponEvent::getRewardTicketType,
                CouponEvent::getRewardTicketAmount,
                CouponEvent::getStartsAt,
                CouponEvent::getExpiresAt,
                CouponEvent::getEventStatus
        ).containsExactly(
                CuponFixture.CUPON_NAME,
                CuponFixture.CUPON_DESCRIPTION,
                CuponFixture.Coupon_EVENT_TYPE,
                CuponFixture.CUPON_QUANTITY,
                CuponFixture.REWARD_TICKET_TYPE,
                CuponFixture.REWARD_TICKET_QUANTITY,
                CuponFixture.STARTED_AT,
                CuponFixture.ENDED_AT,
                CouponEventStatus.DRAFT
        );
    }

    @Test
    void 이벤트를_활성화한다() {
        // given
        var event = CuponFixture.createCuponEvent();

        // when
        event.activate();

        // then
        assertThat(event.getEventStatus()).isEqualTo(CouponEventStatus.ACTIVE);
    }

    @Test
    void 이벤트가_비활성화된_상태일때만_활성화_가능하다() {
        // given
        var event = CuponFixture.createCuponEvent();
        event.activate();

        // when & then
        // 이미 활성화된 이벤트는 다시 활성화할 수 없다.
        assertThatThrownBy(event::activate)
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_INVALID_STATUS.getMessage());

        // 이미 종료된 이벤트는 다시 활성화할 수 없다.
        event.end();
        assertThatThrownBy(event::activate)
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_INVALID_STATUS.getMessage());
    }

    @Test
    void 이벤트를_종료시킨다() {
        // given
        var event = CuponFixture.createCuponEvent();

        event.activate();
        // when
        event.end();

        // then
        assertThat(event.getEventStatus()).isEqualTo(CouponEventStatus.ENDED);
    }

    @Test
    void 이벤트가_활성화_상태일떄만_종료_가능하다() {
        // given
        var event = CuponFixture.createCuponEvent();

        // when & then
        //  아직 활성화되지 않은 이벤트는 종료할 수 없다.
        assertThatThrownBy(event::end)
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_INVALID_STATUS.getMessage());

        // 이미 종료된 이벤트는 다시 종료할 수 없다.
        event.activate();
        event.end();
        assertThatThrownBy(event::end)
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_INVALID_STATUS.getMessage());
    }

    @Test
    void 이벤트가_활성화된_시각인지_확인한다() {
        // given
        var event = CuponFixture.createCuponEvent();
        event.activate();

        // when & then
        assertThat(event.isIssuable(CuponFixture.STARTED_AT.plusSeconds(1))).isTrue();
        assertThat(event.isIssuable(CuponFixture.STARTED_AT.minusSeconds(1))).isFalse();
        assertThat(event.isIssuable(CuponFixture.STARTED_AT.plusWeeks(1))).isFalse();
    }
}