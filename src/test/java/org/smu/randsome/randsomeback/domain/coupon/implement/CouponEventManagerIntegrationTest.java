package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class CouponEventManagerIntegrationTest extends IntegrationTestSupport {

    final CouponEventManager couponEventManager;
    @Test
    void 관리자가_생성한다() {
        // given
        var newCouponEvent = new NewCouponEvent(
                "이벤트명",
                "이벤트 설명",
                CouponEventType.HAPPY_HOUR,
                100,
                TicketType.RANDOM,
                10,
                TestDateTimeUtils.now(),
                TestDateTimeUtils.now().plusDays(7)
        );
        // when
        var event = couponEventManager.register(newCouponEvent);

        // then
        assertThat(event).extracting(
                CouponEvent::getName,
                CouponEvent::getDescription,
                CouponEvent::getType,
                CouponEvent::getTotalQuantity,
                CouponEvent::getRewardTicketType,
                CouponEvent::getRewardTicketAmount,
                CouponEvent::getStartsAt,
                CouponEvent::getExpiresAt
        ).containsExactly(
                newCouponEvent.name(),
                newCouponEvent.description(),
                newCouponEvent.type(),
                newCouponEvent.totalQuantity(),
                newCouponEvent.rewardTicketType(),
                newCouponEvent.rewardTicketAmount(),
                newCouponEvent.startsAt(),
                newCouponEvent.expiresAt()
        );
    }

}