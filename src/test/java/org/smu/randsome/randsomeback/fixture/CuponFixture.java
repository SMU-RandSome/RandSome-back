package org.smu.randsome.randsomeback.fixture;

import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;

public class CuponFixture {

    public static final String CUPON_NAME = "CUPON_NAME";
    public static final String CUPON_DESCRIPTION = "CUPON_DESCRIPTION";
    public static final int CUPON_QUANTITY = 10;
    public static final int REWARD_TICKET_QUANTITY = 1;
    public static final CouponEventType Coupon_EVENT_TYPE = CouponEventType.HAPPY_HOUR;
    public static final TicketType REWARD_TICKET_TYPE = TicketType.RANDOM;
    public static final LocalDateTime STARTED_AT = TestDateTimeUtils.now();
    public static final LocalDateTime ENDED_AT = TestDateTimeUtils.now().plusDays(1);
    public static final LocalDateTime COUPON_EXPIRED_AT = TestDateTimeUtils.now().plusDays(30);

    public static CouponEvent createCuponEvent() {
        return CouponEvent.create(
                CUPON_NAME,
                CUPON_DESCRIPTION,
                Coupon_EVENT_TYPE,
                CUPON_QUANTITY,
                REWARD_TICKET_TYPE,
                REWARD_TICKET_QUANTITY,
                STARTED_AT,
                ENDED_AT,
                COUPON_EXPIRED_AT
        );
    }

    public static CouponEvent createActiveCuponEvent() {
        CouponEvent event = createCuponEvent();
        event.activate(LocalDateTime.now());
        return event;
    }

}
